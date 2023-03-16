package org.infinispan.hotrod.operation;

import static org.infinispan.hotrod.Constants.DATA_FORMAT;
import static org.infinispan.hotrod.Constants.KEY;
import static org.infinispan.hotrod.Constants.KEY_BYTES;
import static org.infinispan.hotrod.Constants.VALUE;
import static org.infinispan.hotrod.Constants.VALUE_BYTES;

import org.infinispan.api.common.CacheEntry;
import org.infinispan.api.common.CacheWriteOptions;
import org.infinispan.hotrod.exceptions.InvalidResponseException;
import org.infinispan.hotrod.impl.operations.OperationContext;
import org.infinispan.hotrod.impl.operations.PutOperation;
import org.infinispan.hotrod.impl.protocol.HeaderParams;
import org.infinispan.hotrod.impl.protocol.HotRodConstants;
import org.infinispan.hotrod.impl.transport.netty.ByteBufUtil;
import org.infinispan.hotrod.impl.transport.netty.HeaderDecoder;
import org.infinispan.server.hotrod.MetadataUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

public class TestPutOperation extends AbstractOperation {

   private static final long PUT_MESSAGE_ID = 142;
   private final HeaderParams PUT_HEADERS = new HeaderParams(HotRodConstants.PUT_REQUEST, HotRodConstants.PUT_RESPONSE, 0, (byte) 0, PUT_MESSAGE_ID, DATA_FORMAT, null);

   public TestPutOperation(OperationContext ctx) {
      super(ctx);
   }

   @Override
   public void setup() {
      response = Unpooled.unreleasableBuffer(writePutResponse());
      request = new PutOperation(ctx, KEY, KEY_BYTES, VALUE_BYTES, CacheWriteOptions.writeOptions().build(), DATA_FORMAT) {

         @Override
         public HeaderParams header() {
            return PUT_HEADERS;
         }

         @Override
         public void acceptResponse(ByteBuf buf, short status, HeaderDecoder decoder) {
            if (HotRodConstants.isSuccess(status)) {
               statsDataStore();
               if (HotRodConstants.hasPrevious(status)) {
                  statsDataRead(true);
               }
               completeResponse(buf, status);
            } else {
               throw new InvalidResponseException("Unexpected response status: " + Integer.toHexString(status));
            }
         }

         void completeResponse(ByteBuf buf, short status) {
            complete(returnPossiblePrevValue(buf, status));
         }

         @Override
         public boolean complete(Object value) {
            assert value instanceof CacheEntry: "Response is not cache entry!";
            CacheEntry<String, String> ce = (CacheEntry<String, String>) value;
            if (!VALUE.equals(ce.value())) throw new IllegalStateException("Unexpected value: " + ce.value());
            return super.complete(ce);
         }

         @Override
         public void scheduleTimeout(Channel channel) {
            // no-op
         }
      };
   }

   @Override
   public boolean isResultCorrect(String result) {
      CacheEntry<String, String> ce = (CacheEntry<String, String>) ((Object) request.join());
      return result.equals(ce.value());
   }

   private ByteBuf writePutResponse() {
      ByteBuf buf = Unpooled.buffer();
      buf.writeByte(HotRodConstants.RESPONSE_MAGIC);

      // Write header info.
      ByteBufUtil.writeVLong(buf, PUT_MESSAGE_ID);
      buf.writeByte(HotRodConstants.PUT_RESPONSE);
      buf.writeByte(HotRodConstants.SUCCESS_WITH_PREVIOUS);
      buf.writeByte(0);

      // Write the payload, this includes a previous value with metadata.
      MetadataUtils.writeMetadata(1000, 1000, System.currentTimeMillis(), System.currentTimeMillis(), 3, buf);
      ByteBufUtil.writeVInt(buf, VALUE_BYTES.length);
      buf.writeBytes(VALUE_BYTES);

      return buf;
   }
}
