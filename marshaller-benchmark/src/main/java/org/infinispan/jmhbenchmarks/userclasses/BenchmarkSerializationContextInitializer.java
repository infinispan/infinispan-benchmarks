package org.infinispan.jmhbenchmarks.userclasses;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

/**
 * TODO! document this
 *
 * @author Pedro Ruivo
 */
@AutoProtoSchemaBuilder(
      schemaPackageName = "org.infinispan.jmhbenchmarks.userclasses",
      schemaFilePath = "org/infinispan/jmhbenchmarks/userclasses/userclasses.proto",
      includeClasses = {
            Address.class,
            User.class,
            IracMetadata.class,
            IracEntryVersion.class,
            IracEntryVersion.MapEntry.class,
            TopologyIracVersion.class,
      }
)
public interface BenchmarkSerializationContextInitializer extends SerializationContextInitializer {

   SerializationContextInitializer INSTANCE = new org.infinispan.jmhbenchmarks.userclasses.BenchmarkSerializationContextInitializerImpl();

}
