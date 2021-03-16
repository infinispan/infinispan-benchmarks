package org.infinispan.jmhbenchmarks;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(
      includeClasses = {
            KeySequenceGenerator.ValueWrapper.class,
      },
      schemaFileName = "jmh.proto",
      schemaFilePath = "proto/",
      schemaPackageName = "jmh")
interface JMHInitializer extends SerializationContextInitializer {
}
