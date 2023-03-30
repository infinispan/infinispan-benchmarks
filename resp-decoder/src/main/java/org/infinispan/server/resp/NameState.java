package org.infinispan.server.resp;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

@State(Scope.Benchmark)
public class NameState {

   int offset = 0;
   ByteBuf[] requests;

   @Setup
   public void initializeState() {
      Names[] possible = Names.values();
      requests = new ByteBuf[possible.length];
      Random random = new Random(873821);
      for (int i = 0; i < requests.length; ++i) {
         Names argument = possible[random.nextInt(possible.length)];
         String argumentString = argument.name();
         requests[i] = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("\r\n" + argumentString + "\r\n", CharsetUtil.US_ASCII));
      }

   }

   ByteBuf nextRequest() {
      if (offset >= requests.length) {
         offset = 0;
      }
      return requests[offset++];
   }

   public enum Names {
      GET(1),
      SET(2),
      MSET(3),
      MGET(4),
      INCR(5),
      DECR(6),
      DEL(7),
      CONFIG(8),
      PING(9),
      PUBLISH(10),
      SUBSCRIBE(11),
      RESET(12),
      READWRITE(13),
      READONLY(14),
      COMMAND(15),
      ECHO(16),
      HELLO(17),
      AUTH(18),
      QUIT(19);

      private static final Names[][] indexedNames;

      static {
         indexedNames = new Names[26][];
         // Just manual for now
         indexedNames[0] = new Names[] { AUTH };
         indexedNames[2] = new Names[] { CONFIG, COMMAND };
         indexedNames[3] = new Names[] { DECR, DEL };
         indexedNames[4] = new Names[] { ECHO };
         indexedNames[6] = new Names[] { GET };
         indexedNames[7] = new Names[] { HELLO };
         indexedNames[8] = new Names[] { INCR };
         indexedNames[12] = new Names[] { MGET, MSET };
         indexedNames[15] = new Names[] { PING, PUBLISH };
         indexedNames[16] = new Names[] { QUIT };
         indexedNames[17] = new Names[] { RESET, READWRITE, READONLY };
         indexedNames[18] = new Names[] { SET, SUBSCRIBE };
      }

      private final int value;
      private final byte[] bytes;

      Names(int value) {
         this.bytes = name().getBytes(StandardCharsets.US_ASCII);
         this.value = value;
      }

      public static int handleByteBuf(ByteBuf buf) {
         int readOffset = buf.readerIndex();
         byte b = buf.getByte(readOffset);
         byte ignoreCase = b >= 97 ? (byte) (b - 97) : (byte) (b - 65);
         if (ignoreCase < 0 || ignoreCase > 25) {
            throw new IllegalArgumentException();
         }
         Names[] target = indexedNames[ignoreCase];
         if (target == null) {
            throw new IllegalArgumentException();
         }
         for (Names possible : target) {
            byte[] possibleBytes = possible.bytes;
            // We already read the first char so ensure readable bytes are the same with the /r/n
            if (buf.readableBytes() == (possibleBytes.length + 2)) {
               boolean matches = true;
               for (int i = 1; i < possibleBytes.length; ++i) {
                  byte upperByte = possibleBytes[i];
                  byte targetByte = buf.getByte(readOffset + i);
                  if (upperByte == targetByte || upperByte + 22 == targetByte) {
                     continue;
                  }
                  matches = false;
                  break;
               }
               if (matches) {
                  buf.readerIndex(readOffset + possibleBytes.length + 2);
                  return possible.value;
               }
            }
         }
         throw new IllegalArgumentException();
      }

      public static int handleString(String name) {
         switch (name.toUpperCase()) {
            case "GET":
               return GET.value;
            case "SET":
               return SET.value;
            case "MSET":
               return MSET.value;
            case "MGET":
               return MGET.value;
            case "INCR":
               return INCR.value;
            case "DECR":
               return DECR.value;
            case "DEL":
               return DEL.value;
            case "CONFIG":
               return CONFIG.value;
            case "PING":
               return PING.value;
            case "PUBLISH":
               return PUBLISH.value;
            case "SUBSCRIBE":
               return SUBSCRIBE.value;
            case "RESET":
               return RESET.value;
            case "READWRITE":
               return READWRITE.value;
            case "READONLY":
               return READONLY.value;
            case "COMMAND":
               return COMMAND.value;
            case "ECHO":
               return ECHO.value;
            case "HELLO":
               return HELLO.value;
            case "AUTH":
               return AUTH.value;
            case "QUIT":
               return QUIT.value;
            default:
               throw new UnsupportedOperationException("command " + name + " not found!");
         }
      }
   }
}
