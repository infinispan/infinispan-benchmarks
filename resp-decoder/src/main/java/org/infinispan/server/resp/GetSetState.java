package org.infinispan.server.resp;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

@State(Scope.Benchmark)
public class GetSetState extends NettyChannelState {
   public ByteBuf GET_REQUEST;
   public ByteBuf SET_REQUEST;

   @Setup
   public void initializeState() {
      super.initializeState();

      GET_REQUEST = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n", CharsetUtil.US_ASCII));
      SET_REQUEST = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("*3\r\n$3\r\nSET\r\n$3\r\nfoo\r\n$3\r\nbar\r\n", CharsetUtil.US_ASCII));
   }
}
