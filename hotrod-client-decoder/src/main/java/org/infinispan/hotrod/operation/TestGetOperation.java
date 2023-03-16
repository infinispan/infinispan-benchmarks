package org.infinispan.hotrod.operation;

import static org.infinispan.hotrod.Constants.DATA_FORMAT;
import static org.infinispan.hotrod.Constants.KEY;
import static org.infinispan.hotrod.Constants.KEY_BYTES;
import static org.infinispan.hotrod.Constants.VALUE;
import static org.infinispan.hotrod.Constants.VALUE_BYTES;

import org.infinispan.api.common.CacheOptions;
import org.infinispan.hotrod.impl.operations.GetOperation;
import org.infinispan.hotrod.impl.operations.OperationContext;
import org.infinispan.hotrod.impl.protocol.HeaderParams;
import org.infinispan.hotrod.impl.protocol.HotRodConstants;
import org.infinispan.hotrod.impl.transport.netty.ByteBufUtil;
import org.infinispan.hotrod.impl.transport.netty.HeaderDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

public class TestGetOperation extends AbstractOperation {
   private static final long GET_MESSAGE_ID = 42;
   private final HeaderParams GET_HEADERS = new HeaderParams(HotRodConstants.GET_REQUEST, HotRodConstants.GET_RESPONSE, 0, (byte) 0, GET_MESSAGE_ID, DATA_FORMAT, null);

   public TestGetOperation(OperationContext ctx) {
      super(ctx);
   }

   @Override
   public void setup() {
      response = Unpooled.unreleasableBuffer(writeGetResponse());
      this.request = new GetOperation<>(ctx, KEY, KEY_BYTES, CacheOptions.options().build(), DATA_FORMAT) {

         @Override
         public HeaderParams header() {
            return GET_HEADERS;
         }

         @Override
         public void acceptResponse(ByteBuf buf, short status, HeaderDecoder decoder) {
            // Bellow is copied from main.
            if (!HotRodConstants.isNotExist(status) && HotRodConstants.isSuccess(status)) {
               statsDataRead(true);
               complete(dataFormat().valueToObj(ByteBufUtil.readArray(buf), operationContext.getConfiguration().getClassAllowList()));
            } else {
               statsDataRead(false);
               complete(null);
            }
         }

         @Override
         public boolean complete(String value) {
            if (!VALUE.equals(value)) throw new IllegalStateException("Unexpected value: " + value);
            return super.complete(value);
         }

         @Override
         public void scheduleTimeout(Channel channel) {
            // no-op
         }
      };
   }

   @Override
   public boolean isResultCorrect(String result) {
      return result.equals(getOperation().join());
   }

   private ByteBuf writeGetResponse() {
      ByteBuf buf = Unpooled.buffer();
      buf.writeByte(HotRodConstants.RESPONSE_MAGIC);

      // Write header info.
      ByteBufUtil.writeVLong(buf, GET_MESSAGE_ID);
      buf.writeByte(HotRodConstants.GET_RESPONSE);
      buf.writeByte(HotRodConstants.NO_ERROR_STATUS);
      buf.writeByte(0); // No new topology.

      // Write the value.
      ByteBufUtil.writeVInt(buf, VALUE_BYTES.length);
      buf.writeBytes(VALUE_BYTES);
      return buf;
   }
}
