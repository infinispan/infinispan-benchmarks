package org.infinispan.protostream.userclasses;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

/**
 * TODO! document this
 *
 * @author Pedro Ruivo
 */
@AutoProtoSchemaBuilder(includeClasses = {
      Address.class,
      User.class,
      IracMetadata.class,
      IracEntryVersion.class,
      IracEntryVersion.MapEntry.class,
      TopologyIracVersion.class
})
public interface BenchmarkSerializationContextInitializer extends SerializationContextInitializer {

   SerializationContextInitializer INSTANCE = new org.infinispan.protostream.userclasses.BenchmarkSerializationContextInitializerImpl();

}
