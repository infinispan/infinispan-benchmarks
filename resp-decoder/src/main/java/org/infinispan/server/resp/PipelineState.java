package org.infinispan.server.resp;

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class PipelineState extends NettyChannelState {
   @Param({"2", "25"})
   public int messageCount;

   public ByteBuf GET_REQUEST;

   public ByteBuf SET_REQUEST;

   public int getWriteIndex;
   public int setWriteIndex;

   @Setup
   public void initializeState() {
      super.initializeState();

      CompositeByteBuf getBuffer = Unpooled.compositeBuffer(messageCount);
      for (int i = 0; i < messageCount; ++i) {
         getBuffer.addComponent(true, Unpooled.copiedBuffer("*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n", CharsetUtil.US_ASCII));
      }

      GET_REQUEST = Unpooled.unreleasableBuffer(getBuffer);

      getWriteIndex = GET_REQUEST.writerIndex();

      CompositeByteBuf setBuffer = Unpooled.compositeBuffer(messageCount);
      for (int i = 0; i < messageCount; ++i) {
         getBuffer.addComponent(true, Unpooled.copiedBuffer("*3\r\n$3\r\nSET\r\n$3\r\nfoo\r\n$3\r\nbar\r\n", CharsetUtil.US_ASCII));
      }

      SET_REQUEST = Unpooled.unreleasableBuffer(setBuffer);

      setWriteIndex = SET_REQUEST.writerIndex();
   }
}
