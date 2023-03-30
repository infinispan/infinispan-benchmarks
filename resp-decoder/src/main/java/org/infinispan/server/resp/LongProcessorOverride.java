package org.infinispan.server.resp;

import io.netty.buffer.ByteBuf;

public class LongProcessorOverride extends NewIntrinsics.Resp2LongProcessor {
   static LongProcessorOverride INSTANCE = new LongProcessorOverride();

   private LongProcessorOverride() {
      complete = true;
      // Always 0 until later?
      bytesRead = 0;
   }
   @Override
   public long getValue(ByteBuf buffer) {
      return buffer.readableBytes() - 4;
   }

}
