package org.infinispan.protostream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.infinispan.protostream.impl.TagReaderImpl.Decoder;

import io.netty.buffer.ByteBuf;

public class ByteBufDecoder extends Decoder {
   private final ByteBuf buf;
   private final int initialOffset;

   public ByteBufDecoder(ByteBuf buf) {
      this.buf = buf;
      this.initialOffset = buf.readerIndex();
   }

   MalformedProtobufException truncated() {
      // TODO: add this from log eventually
      return new MalformedProtobufException("Input data ended unexpectedly in the middle of a field. The message is corrupt.");
   }

   MalformedProtobufException malformedVarint() {
      // TODO: add this from log eventually
      return new MalformedProtobufException("Encountered a malformed varint.");
   }

   @Override
   protected int getEnd() {
      return buf.writerIndex() - initialOffset;
   }

   @Override
   protected int getPos() {
      return buf.readableBytes() - initialOffset;
   }

   @Override
   protected byte[] getBufferArray() {
      byte[] bytes = new byte[buf.readableBytes()];
      buf.readBytes(bytes);
      return bytes;
   }

   @Override
   protected boolean isAtEnd() {
      return buf.readableBytes() == 0;
   }

   @Override
   protected void skipVarint() throws IOException {
      for (int i = 0; i < 5; ++i) {
         if (readRawByte() >= 0) {
            return;
         }
      }
      throw malformedVarint();
   }

   @Override
   protected void skipRawBytes(int length) throws IOException {
      try {
         buf.skipBytes(length);
      } catch (IndexOutOfBoundsException e) {
         throw truncated();
      }
   }

   @Override
   protected String readString() throws IOException {
      int length = readVarint32();
      if (length > buf.readableBytes()) {
         throw truncated();
      }
      String string = buf.toString(buf.readerIndex(), length, StandardCharsets.UTF_8);
      buf.skipBytes(length);
      return string;
   }

   @Override
   protected byte readRawByte() throws IOException {
      try {
         return buf.readByte();
      } catch (IndexOutOfBoundsException e) {
         throw truncated();
      }
   }

   @Override
   protected byte[] readRawByteArray(int length) throws IOException {
      if (length > buf.readableBytes()) {
         throw truncated();
      }
      byte[] bytes = new byte[length];
      buf.readBytes(bytes);
      return bytes;
   }

   @Override
   protected ByteBuffer readRawByteBuffer(int length) throws IOException {
      if (length > buf.readableBytes()) {
         throw truncated();
      }
      return buf.nioBuffer(buf.readerIndex(), length);
   }

   @Override
   protected long readVarint64() throws IOException {
      long value = 0;
      for (int i = 0; i < 64; i += 7) {
         byte b = readRawByte();
         value |= (long) (b & 0x7F) << i;
         if (b >= 0) {
            return value;
         }
      }
      throw malformedVarint();
   }

   @Override
   protected int readFixed32() throws IOException {
      try {
         return buf.readInt();
      } catch (IndexOutOfBoundsException e) {
         throw truncated();
      }
   }

   @Override
   protected long readFixed64() throws IOException {
      try {
         return buf.readLong();
      } catch (IndexOutOfBoundsException e) {
         throw truncated();
      }
   }

   @Override
   protected int pushLimit(int newLimit) throws IOException {
      int prev = buf.writerIndex() - initialOffset;
      if (prev < newLimit) {
         throw truncated();
      }
      buf.writerIndex(buf.readerIndex() + newLimit);
      return prev;
   }

   @Override
   protected void popLimit(int oldLimit) {
      buf.writerIndex(oldLimit + initialOffset);
   }

   @Override
   protected Decoder decoderFromLength(int length) {
      ByteBuf slicedBuf = buf.readSlice(length);
      return new ByteBufDecoder(slicedBuf);
   }

   @Override
   protected int setGlobalLimit(int globalLimit) {
      return Integer.MAX_VALUE;
   }
}
