package org.infinispan.server.resp;
import java.util.List;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.lang.UnsupportedOperationException;
import java.util.ArrayList;
import java.util.Collections;

public class NewRespDecoder extends NewBaseRespDecoder {
   private int state;
   private int requestBytes;

   private RespCommand resp3x_bulkCommand;
   private byte[] resp3x_readTerminatedBytes;
   private int resp3x_readSizeAndCheckRemainder;
   private long resp3x_readNumber;
   private byte[] resp3x_bulkArray;
   private List< byte[]> resp3x_arguments;
   private String resp3x_simpleString;
   private RespCommand resp3x_simpleCommand;
   private RespCommand resp3x_command;
   private byte[] resp3x_array;
   private String resp3x_bulkString;
   private byte resp3x_singleByte;

   private final List < byte[] > reusedList = new ArrayList < >(16);
   public NewRespDecoder(NewRespRequestHandler initialHandler) {
      this.requestHandler = initialHandler;
   }

   @Override
   public void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
      int pos = buf.readerIndex();
      try {
         if (! ctx.channel().config().isAutoRead()) {
            log.tracef("Auto read was disabled, not reading next bytes");
            return;
         } else {
            log.tracef("Auto read was enabled, reading next bytes");
         }
         while (switch0(ctx, buf));
      } catch (Throwable t) {
         throw t;
      } finally {
         requestBytes += buf.readerIndex() - pos;
      }
   }

   private boolean switch0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
      byte b;
      int pos;
      switch (state) {
         case 0:
            //
            reset();
            state = 1;
            // fallthrough
         case 1:
            //
            pos = buf.readerIndex();
            resp3x_singleByte = NewIntrinsics.singleByte(buf);
            if (buf.readerIndex() == pos) return false;
            state = 2;
            // fallthrough
         case 2:
            // resp3x.request
            if (resp3x_singleByte != org.infinispan.server.resp.RespConstants.ARRAY) throw new UnsupportedOperationException("Only array types are supported, received: " + resp3x_singleByte);
            reusedList.clear();

            state = 3;
            // fallthrough
         case 3:
            // resp3x.request
            pos = buf.readerIndex();
            resp3x_readNumber = NewIntrinsics.readNumber(buf, longProcessor);
            if (buf.readerIndex() == pos) return false;
            state = 5;
            return true;
         case 4:
            // resp3x.request
            pos = buf.readerIndex();
            resp3x_singleByte = NewIntrinsics.singleByte(buf);
            if (buf.readerIndex() == pos) return false;
            state = 6;
            return true;
         case 5:
            // resp3x.request/resp3x.number
            resp3x_readNumber -= 1;

            state = 4;
            return true;
         case 6:
            // resp3x.request
            switch (resp3x_singleByte) {
               case org.infinispan.server.resp.RespConstants.BULK_STRING:
                  state = 8;
                  return true;
               case org.infinispan.server.resp.RespConstants.SIMPLE_STRING:
                  state = 9;
                  return true;
               default:
                  throw new UnsupportedOperationException("Type not supported: " + resp3x_singleByte);

            }
         case 7:
            // resp3x.request
            if (resp3x_readNumber > 16) {
               state = 11;
               return true;
            }
            if (resp3x_readNumber >= 1) {
               state = 12;
               return true;
            }
            resp3x_arguments = Collections.emptyList();
            ;
            state = 10;
            return true;
         case 8:
            // resp3x.request/resp3x.command
            pos = buf.readerIndex();
            resp3x_bulkCommand = NewIntrinsics.bulkCommand(buf, longProcessor);
            if (buf.readerIndex() == pos) return false;
            resp3x_command = resp3x_bulkCommand;
            state = 7;
            return true;
         case 9:
            // resp3x.request/resp3x.command
            pos = buf.readerIndex();
            resp3x_simpleCommand = NewIntrinsics.simpleCommand(buf);
            if (buf.readerIndex() == pos) return false;
            resp3x_command = resp3x_simpleCommand;
            state = 7;
            return true;
         case 10:
            // resp3x.request
            if (resp3x_readNumber == 0) {
               state = 13;
               return true;
            }
            resp3x_readNumber--;
            state = 14;
            return true;
         case 11:
            // resp3x.request/resp3x.arguments
            resp3x_arguments = new ArrayList < >((int) resp3x_readNumber);
            ;
            state = 10;
            return true;
         case 12:
            // resp3x.request/resp3x.arguments
            resp3x_arguments = reusedList;
            ;
            state = 10;
            return true;
         case 13:
            // resp3x.request
            if (! handleCommandAndArguments(ctx, resp3x_command, resp3x_arguments)) {
               state = 0;
               return false;
            }
            ;

            state = 0;
            return true;
         case 14:
            // resp3x.request
            pos = buf.readerIndex();
            resp3x_singleByte = NewIntrinsics.singleByte(buf);
            if (buf.readerIndex() == pos) return false;
            state = 15;
            // fallthrough
         case 15:
            // resp3x.request
            switch (resp3x_singleByte) {
               case org.infinispan.server.resp.RespConstants.BULK_STRING:
                  state = 17;
                  return true;
               case org.infinispan.server.resp.RespConstants.SIMPLE_STRING:
                  state = 18;
                  return true;
               case org.infinispan.server.resp.RespConstants.NUMERIC:
                  state = 19;
                  return true;
               default:
                  throw new UnsupportedOperationException("Type not supported: " + resp3x_singleByte);

            }
         case 16:
            // resp3x.request
            resp3x_arguments.add(resp3x_array);

            state = 10;
            return true;
         case 17:
            // resp3x.request/resp3x.array
            pos = buf.readerIndex();
            resp3x_bulkArray = NewIntrinsics.bulkArray(buf, longProcessor);
            if (buf.readerIndex() == pos) return false;
            resp3x_array = resp3x_bulkArray;
            state = 16;
            return true;
         case 18:
            // resp3x.request/resp3x.array
            pos = buf.readerIndex();
            resp3x_readTerminatedBytes = NewIntrinsics.readTerminatedBytes(buf);
            if (buf.readerIndex() == pos) return false;
            resp3x_array = resp3x_readTerminatedBytes;
            state = 16;
            return true;
         case 19:
            // resp3x.request/resp3x.array
            pos = buf.readerIndex();
            resp3x_readTerminatedBytes = NewIntrinsics.readTerminatedBytes(buf);
            if (buf.readerIndex() == pos) return false;
            resp3x_array = resp3x_readTerminatedBytes;
            state = 16;
            return true;
      }
      return true;
   }

   private void deadEnd() {
      throw new IllegalArgumentException();
   }

   private void reset() {
      requestBytes = 0;
      resp3x_bulkCommand = null;
      resp3x_readTerminatedBytes = null;
      resp3x_readSizeAndCheckRemainder = 0;
      resp3x_readNumber = 0;
      resp3x_bulkArray = null;
      resp3x_arguments = null;
      resp3x_simpleString = null;
      resp3x_simpleCommand = null;
      resp3x_command = null;
      resp3x_array = null;
      resp3x_bulkString = null;
      resp3x_singleByte = 0;
   }

   public int requestBytes() {
      return requestBytes;
   }
}
