package org.infinispan.protostream;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.infinispan.protostream.impl.TagWriterImpl;

import io.netty.buffer.ByteBuf;

public class ByteBufEncoder extends TagWriterImpl.FixedVarintEncoder {
   private final ByteBuf buf;

   public ByteBufEncoder(ByteBuf buf) {
      this.buf = buf;
   }

   private IOException outOfWriteBufferSpace(Throwable t) {
      return new IOException("Ran out of buffer space", t);
   }

   @Override
   protected void writeVarint32(int value) throws IOException {
      try {
         while (true) {
            if ((value & 0xFFFFFF80) == 0) {
               buf.writeByte((byte) value);
               break;
            } else {
               buf.writeByte((byte) (value & 0x7F | 0x80));
               value >>>= 7;
            }
         }
      } catch (IndexOutOfBoundsException e) {
         throw outOfWriteBufferSpace(e);
      }
   }

   @Override
   protected void writeVarint64(long value) throws IOException {
      try {
         while (true) {
            if ((value & 0xFFFFFFFFFFFFFF80L) == 0) {
               buf.writeByte((byte) value);
               break;
            } else {
               buf.writeByte((byte) ((int) value & 0x7F | 0x80));
               value >>>= 7;
            }
         }
      } catch (IndexOutOfBoundsException e) {
         throw outOfWriteBufferSpace(e);
      }
   }

   @Override
   protected void writeFixed32(int value) throws IOException {
      try {
         buf.writeInt(value);
      } catch (IndexOutOfBoundsException e) {
         throw outOfWriteBufferSpace(e);
      }
   }

   @Override
   protected void writeFixed64(long value) throws IOException {
      try {
         buf.writeLong(value);
      } catch (IndexOutOfBoundsException e) {
         throw outOfWriteBufferSpace(e);
      }
   }

   @Override
   protected void writeByte(byte value) throws IOException {
      try {
         buf.writeByte(value);
      } catch (IndexOutOfBoundsException e) {
         throw outOfWriteBufferSpace(e);
      }
   }

   @Override
   protected void writeBytes(byte[] value, int offset, int length) throws IOException {
      try {
         buf.writeBytes(value, offset, length);
      } catch (IndexOutOfBoundsException e) {
         throw outOfWriteBufferSpace(e);
      }
   }

   @Override
   protected void writeBytes(ByteBuffer value) throws IOException {
      try {
         buf.writeBytes(value);
      } catch (IndexOutOfBoundsException e) {
         throw outOfWriteBufferSpace(e);
      }
   }

   @Override
   protected int skipFixedVarint() {
      int prev = buf.writerIndex();
      buf.skipBytes(5);
      return prev;
   }

   @Override
   protected void writePositiveFixedVarint(int pos) {
      int length = buf.writerIndex() - pos - 5;
      buf.setByte(pos++, (byte) (length & 0x7F | 0x80));
      buf.setByte(pos++, (byte) ((length >>> 7) & 0x7F | 0x80));
      buf.setByte(pos++, (byte) ((length >>> 14) & 0x7F | 0x80));
      buf.setByte(pos++, (byte) ((length >>> 21) & 0x7F | 0x80));
      buf.setByte(pos, (byte) ((length >>> 28) & 0x7F));
   }
}
