package org.infinispan.protostream.state;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.infinispan.protostream.ProtobufUtil;
import org.infinispan.protostream.userclasses.IracEntryVersion;
import org.infinispan.protostream.userclasses.IracMetadata;
import org.infinispan.protostream.userclasses.TopologyIracVersion;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

@State(Scope.Benchmark)
public class MetadataState {
   private IracMetadata metadata;

   byte[] metadataBytes;
   private ByteBuf metadataByteBuf;
   @Setup
   public void setup(ContextState contextState) throws IOException {
      Map<String, TopologyIracVersion> versions = new HashMap<>();
      versions.put("site_1", TopologyIracVersion.newVersion(10));
      versions.put("site_2", TopologyIracVersion.newVersion(15).increment(15));
      IracEntryVersion version = new IracEntryVersion(versions);
      metadata = new IracMetadata("site_1", version);

      metadataBytes = ProtobufUtil.toWrappedByteArray(contextState.getCtx(), metadata);
      metadataByteBuf = Unpooled.wrappedBuffer(metadataBytes);
   }

   public IracMetadata getMetadata() {
      return metadata;
   }

   public byte[] getMetadataBytes() {
      return metadataBytes;
   }

   public ByteBuf getMetadataByteBuf() {
      return metadataByteBuf.slice();
   }
}
