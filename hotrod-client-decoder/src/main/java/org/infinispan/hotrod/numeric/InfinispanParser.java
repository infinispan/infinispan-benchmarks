package org.infinispan.hotrod.numeric;

import io.netty.buffer.ByteBuf;

public final class InfinispanParser {

   private InfinispanParser() { }

   public static int readVInt(ByteBuf buf) {
      byte b = buf.readByte();
      int i = b & 0x7F;
      for (int shift = 7; (b & 0x80) != 0; shift += 7) {
         b = buf.readByte();
         i |= (b & 0x7FL) << shift;
      }
      return i;
   }

   public static long readVLong(ByteBuf buf) {
      byte b = buf.readByte();
      long i = b & 0x7F;
      for (int shift = 7; (b & 0x80) != 0; shift += 7) {
         b = buf.readByte();
         i |= (b & 0x7FL) << shift;
      }
      return i;
   }

}
