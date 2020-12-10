package org.apache.lucene.search;



import junit.framework.TestCase;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * @version $Id: TestRemoteSearchable.java,v 1.7 2004/03/29 22:48:06 cutting Exp $
 */
public class TestRemoteSearchable extends TestCase {
  public TestRemoteSearchable(String name) {
    super(name);
  }

  private static Searchable getRemote() throws Exception {
    try {
      return lookupRemote();
    } catch (Throwable e) {
      startServer();
      return lookupRemote();
    }
  }

  private static Searchable lookupRemote() throws Exception {
    return (Searchable)Naming.lookup("//localhost/Searchable");
  }

  private static void startServer() throws Exception {
    // construct an index
    RAMDirectory indexStore = new RAMDirectory();
    IndexWriter writer = new IndexWriter(indexStore,new SimpleAnalyzer(),true);
    Document doc = new Document();
    doc.add(Field.Text("test", "test text"));
    writer.addDocument(doc);
    writer.optimize();
    writer.close();

    // publish it
    LocateRegistry.createRegistry(1099);
    Searchable local = new IndexSearcher(indexStore);
    RemoteSearchable impl = new RemoteSearchable(local);
    Naming.rebind("//localhost/Searchable", impl);
  }

  private static void search(Query query) throws Exception {
    // try to search the published index
    Searchable[] searchables = { getRemote() };
    Searcher searcher = new MultiSearcher(searchables);
    Hits result = searcher.search(query);

    assertEquals(1, result.length());
    assertEquals("test text", result.doc(0).get("test"));
  }

  public void testTermQuery() throws Exception {
    search(new TermQuery(new Term("test", "test")));
  }

  public void testBooleanQuery() throws Exception {
    BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(new Term("test", "test")), true, false);
    search(query);
  }

  public void testPhraseQuery() throws Exception {
    PhraseQuery query = new PhraseQuery();
    query.add(new Term("test", "test"));
    query.add(new Term("test", "text"));
    search(query);
  }

  // Tests bug fix at http://nagoya.apache.org/bugzilla/show_bug.cgi?id=20290
  public void testQueryFilter() throws Exception {
    // try to search the published index
    Searchable[] searchables = { getRemote() };
    Searcher searcher = new MultiSearcher(searchables);
    Hits hits = searcher.search(
          new TermQuery(new Term("test", "text")),
          new QueryFilter(new TermQuery(new Term("test", "test"))));
    Hits nohits = searcher.search(
          new TermQuery(new Term("test", "text")),
          new QueryFilter(new TermQuery(new Term("test", "non-existent-term"))));
    assertEquals(0, nohits.length());
  }
}
