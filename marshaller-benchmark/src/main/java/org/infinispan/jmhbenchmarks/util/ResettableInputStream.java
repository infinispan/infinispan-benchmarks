package org.infinispan.jmhbenchmarks.util;

import java.io.ByteArrayInputStream;

public class ResettableInputStream extends ByteArrayInputStream {
   private final int length;
   public ResettableInputStream(byte[] buf, int length) {
      super(buf);
      this.length = length;
   }

   public void restart() {
      pos = 0;
   }

   public int length() {
      return length;
   }
}
