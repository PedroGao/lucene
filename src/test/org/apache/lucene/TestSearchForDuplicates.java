package org.apache.lucene;



import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.lucene.store.*;
import org.apache.lucene.document.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.queryParser.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/** JUnit adaptation of an older test case DocTest.
 * @author dmitrys@earthlink.net
 * @version $Id: TestSearchForDuplicates.java,v 1.3 2004/03/29 22:48:05 cutting Exp $
 */
public class TestSearchForDuplicates extends TestCase {

    /** Main for running test case by itself. */
    public static void main(String args[]) {
        TestRunner.run (new TestSuite(TestSearchForDuplicates.class));
    }



  static final String PRIORITY_FIELD ="priority";
  static final String ID_FIELD ="id";
  static final String HIGH_PRIORITY ="high";
  static final String MED_PRIORITY ="medium";
  static final String LOW_PRIORITY ="low";


  /** This test compares search results when using and not using compound
   *  files.
   *
   *  TODO: There is rudimentary search result validation as well, but it is
   *        simply based on asserting the output observed in the old test case,
   *        without really knowing if the output is correct. Someone needs to
   *        validate this output and make any changes to the checkHits method.
   */
  public void testRun() throws Exception {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw, true);
      doTest(pw, false);
      pw.close();
      sw.close();
      String multiFileOutput = sw.getBuffer().toString();
      //System.out.println(multiFileOutput);

      sw = new StringWriter();
      pw = new PrintWriter(sw, true);
      doTest(pw, true);
      pw.close();
      sw.close();
      String singleFileOutput = sw.getBuffer().toString();

      assertEquals(multiFileOutput, singleFileOutput);
  }


  private void doTest(PrintWriter out, boolean useCompoundFiles) throws Exception {
      Directory directory = new RAMDirectory();
      Analyzer analyzer = new SimpleAnalyzer();
      IndexWriter writer = new IndexWriter(directory, analyzer, true);

      writer.setUseCompoundFile(useCompoundFiles);

      final int MAX_DOCS = 225;

      for (int j = 0; j < MAX_DOCS; j++) {
        Document d = new Document();
        d.add(Field.Text(PRIORITY_FIELD, HIGH_PRIORITY));
        d.add(Field.Text(ID_FIELD, Integer.toString(j)));
        writer.addDocument(d);
      }
      writer.close();

      // try a search without OR
      Searcher searcher = new IndexSearcher(directory);
      Hits hits = null;

      QueryParser parser = new QueryParser(PRIORITY_FIELD, analyzer);

      Query query = parser.parse(HIGH_PRIORITY);
      out.println("Query: " + query.toString(PRIORITY_FIELD));

      hits = searcher.search(query);
      printHits(out, hits);
      checkHits(hits, MAX_DOCS);

      searcher.close();

      // try a new search with OR
      searcher = new IndexSearcher(directory);
      hits = null;

      parser = new QueryParser(PRIORITY_FIELD, analyzer);

      query = parser.parse(HIGH_PRIORITY + " OR " + MED_PRIORITY);
      out.println("Query: " + query.toString(PRIORITY_FIELD));

      hits = searcher.search(query);
      printHits(out, hits);
      checkHits(hits, MAX_DOCS);

      searcher.close();
  }


  private void printHits(PrintWriter out, Hits hits ) throws IOException {
    out.println(hits.length() + " total results\n");
    for (int i = 0 ; i < hits.length(); i++) {
      if ( i < 10 || (i > 94 && i < 105) ) {
        Document d = hits.doc(i);
        out.println(i + " " + d.get(ID_FIELD));
      }
    }
  }

  private void checkHits(Hits hits, int expectedCount) throws IOException {
    assertEquals("total results", expectedCount, hits.length());
    for (int i = 0 ; i < hits.length(); i++) {
      if ( i < 10 || (i > 94 && i < 105) ) {
        Document d = hits.doc(i);
        assertEquals("check " + i, String.valueOf(i), d.get(ID_FIELD));
      }
    }
  }

}
