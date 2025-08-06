package org.infinispan.protostream.utf;

import java.io.IOException;

import org.infinispan.protostream.ProtobufUtil;
import org.infinispan.protostream.RandomAccessOutputStream;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.impl.TagWriterImpl;

public class TagWriter implements StringWriter {

   static final SerializationContext SERIALIZATION_CONTEXT = ProtobufUtil.newSerializationContext();

   TagWriterImpl writer;
   RandomAccessOutputStream out;

   public TagWriter(RandomAccessOutputStream out) {
      this.out = out;
      this.writer = TagWriterImpl.newInstance(SERIALIZATION_CONTEXT, out);
   }

   @Override
   public void writeUTF(String string) throws IOException {
      writer.writeString(1, string);
   }
}
