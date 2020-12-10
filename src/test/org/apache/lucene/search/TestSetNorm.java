package org.apache.lucene.search;



import junit.framework.TestCase;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/** Document boost unit test.
 *
 * @author Doug Cutting
 * @version $Revision: 1.2 $
 */
public class TestSetNorm extends TestCase {
  public TestSetNorm(String name) {
    super(name);
  }
  
  public void testSetNorm() throws Exception {
    RAMDirectory store = new RAMDirectory();
    IndexWriter writer = new IndexWriter(store, new SimpleAnalyzer(), true);
    
    // add the same document four times
    Field f1 = Field.Text("field", "word");
    Document d1 = new Document();
    d1.add(f1);
    writer.addDocument(d1);
    writer.addDocument(d1);
    writer.addDocument(d1);
    writer.addDocument(d1);
    writer.close();

    // reset the boost of each instance of this document
    IndexReader reader = IndexReader.open(store);
    reader.setNorm(0, "field", 1.0f);
    reader.setNorm(1, "field", 2.0f);
    reader.setNorm(2, "field", 4.0f);
    reader.setNorm(3, "field", 16.0f);
    reader.close();

    // check that searches are ordered by this boost
    final float[] scores = new float[4];

    new IndexSearcher(store).search
      (new TermQuery(new Term("field", "word")),
       new HitCollector() {
         public final void collect(int doc, float score) {
           scores[doc] = score;
         }
       });
    
    float lastScore = 0.0f;

    for (int i = 0; i < 4; i++) {
      assertTrue(scores[i] > lastScore);
      lastScore = scores[i];
    }
  }
}
