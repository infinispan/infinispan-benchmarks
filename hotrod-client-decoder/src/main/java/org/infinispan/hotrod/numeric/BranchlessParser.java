package org.infinispan.hotrod.numeric;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;

public class BranchlessParser {

   private static int readableBytes(ByteBuf buffer) {
      return buffer.writerIndex() - buffer.readerIndex();
   }

   public static int readRawVarint32(ByteBuf buffer) {
      if (readableBytes(buffer) < 4)
         return readRawVarint24(buffer);

      int wholeOrMore = buffer.getIntLE(buffer.readerIndex());
      int firstOneOnStop = ~wholeOrMore & 0x80808080;
      if (firstOneOnStop == 0) {
         return readRawVarInt40(buffer, wholeOrMore);
      }
      int bitsToKeep = Integer.numberOfTrailingZeros(firstOneOnStop) + 1;
      buffer.skipBytes(bitsToKeep >> 3);
      int mask = firstOneOnStop ^ (firstOneOnStop - 1);
      return readInt(wholeOrMore & mask);
   }

   private static int readRawVarInt40(ByteBuf buffer, int wholeOrMore) {
      byte lastByte;
      if (readableBytes(buffer) <= 4 || (lastByte = buffer.getByte(buffer.readerIndex() + 4)) < 0) {
         throw new CorruptedFrameException("malformed varint.");
      }
      buffer.skipBytes(5);
      return lastByte << 28 | readInt(wholeOrMore);
   }

   private static int readInt(int continuation) {
      // mix them up as per varint spec while dropping the continuation bits:
      // 0x7F007F isolate the first byte and the third byte dropping the continuation bits
      // 0x7F007F00 isolate the second byte and the fourth byte dropping the continuation bits
      // the second and fourth byte are shifted to the right by 1, filling the gaps left by the first and third byte
      // it means that the first and second bytes now occupy the first 14 bits (7 bits each)
      // and the third and fourth bytes occupy the next 14 bits (7 bits each), with a gap between the 2s of 2 bytes
      // and another gap of 2 bytes after the forth and third.
      continuation = (continuation & 0x7F007F) | ((continuation & 0x7F007F00) >> 1);
      // 0x3FFF isolate the first 14 bits i.e. the first and second bytes
      // 0x3FFF0000 isolate the next 14 bits i.e. the third and forth bytes
      // the third and forth bytes are shifted to the right by 2, filling the gaps left by the first and second bytes
      return (continuation & 0x3FFF) | ((continuation & 0x3FFF0000) >> 2);
   }

   public static long readRawVarint64(ByteBuf buffer) {
      if (readableBytes(buffer) <= 4)
         return readRawVarint32(buffer);

      long wholeOrMore = buffer.getLongLE(buffer.readerIndex());
      long firstOneOnStop = ~wholeOrMore & 0x8080808080808080L;

      // The value occupies the 9 bytes. We just unroll it and consume the bytes.
      if (firstOneOnStop == 0) {
         return readRawVarInt72(buffer, wholeOrMore);
      }

      // Consume the bytes containing the long.
      int bitsToKeep = Long.numberOfTrailingZeros(firstOneOnStop) + 1;
      buffer.skipBytes(bitsToKeep >> 3);

      // Create a mask and create the continuation bytes for decoding.
      long mask = firstOneOnStop ^ (firstOneOnStop - 1);
      return readLong(wholeOrMore & mask);
   }

   private static long readRawVarInt72(ByteBuf buffer, long wholeOrMore) {
      byte lastByte;
      int skip = 9;
      long msb;
      if (readableBytes(buffer) <= 8 || (lastByte = buffer.getByte(buffer.readerIndex() + 8)) < 0) {
         if (readableBytes(buffer) >= 9) {
            if ((lastByte = buffer.getByte(buffer.readerIndex() + 9)) < 0)
               throw new CorruptedFrameException("malformed varint");
            else {
               skip = 10;
               msb = ((long) lastByte << 63) | (((long) buffer.getByte(8) & 0x7F) << 56);
            }
         } else {
            throw new CorruptedFrameException("malformed varint");
         }
      } else {
         msb = (long) lastByte << 56;
      }
      buffer.skipBytes(skip);
      return msb | readLong(wholeOrMore);
   }

   private static long readLong(long continuation) {
      // We parse it as groups of bytes, first bytes 1, 3, 5, and 7.
      // The second group is 2, 4, 6, and 8, which need a shift to right to compensate the gap.
      continuation = (continuation & 0x007F007F007F007FL) | ((continuation & 0x7F007F007F007F00L) >> 1);

      // Now we isolate the bits in sequence. We check 14 bits at a time.
      // The intervals are 0-14 bits, 16-30 (and shift 2), 32-46 (and shift 2 + 2), 48-62 (and shift 2 + 2 + 2).
      return (continuation & 0x3FFF) |
              ((continuation & 0x3FFF0000) >> 2) |
              ((continuation & 0x3FFF00000000L) >> 4) |
              ((continuation & 0x3FFF000000000000L) >> 6);
   }

   public static int readRawVarint24(ByteBuf buffer) {
      if (!buffer.isReadable())
         return 0;

      // Reaching this point, we have at most 3 bytes.
      // It is either a smaller number or the buffer still haven't all the necessary bytes to conclude.
      // The number might occupy 1, 2, or 3 bytes. This way, we have to read as much as we can to check.
      buffer.markReaderIndex();

      // We read the first byte.
      // If it is zero or positive, means we have read the complete value.
      // Otherwise, the number occupies more bytes because the 8 bit is set, marking a continuation.
      byte b = buffer.readByte();
      if (b >= 0)
         return b;

      // The number has the continuation bit set, but we can't read any more bytes.
      // We reset and return.
      if (!buffer.isReadable()) {
         buffer.resetReaderIndex();
         return 0;
      }

      // We get the first 7 bits and drop the continuation.
      // And once again, we read the next byte and check if it is zero or positive.
      // Negative numbers means the continuation bit is set and need to continue reading.
      // If the value is positive we have read a 2 bytes number.
      // We keep the first 7 bits and append new byte as msb.
      int result = b & 127;
      if ((b = buffer.readByte()) >= 0)
         return b << 7 | result;

      // If the continuation bit is set, we accumulate the value and continue reading.
      // The number has the format of [<recent 7 bits>, <first 7 bits>].
      result |= (b & 127) << 7;
      if (!buffer.isReadable()) {
         buffer.resetReaderIndex();
         return 0;
      }

      // The supposed last byte.
      // This value *must* be positive to identify the end of the number with the last 7 bits missing.
      // If the number is negative, the buffer still haven't received all the necessary bytes to read the complete number.
      if ((b = buffer.readByte()) >= 0)
         return result | b << 14;

      buffer.resetReaderIndex();
      return 0;
   }
}
