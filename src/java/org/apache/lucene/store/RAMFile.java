package org.apache.lucene.store;


import java.util.Vector;

class RAMFile {
  Vector buffers = new Vector();
  long length;
  long lastModified = System.currentTimeMillis();
}
