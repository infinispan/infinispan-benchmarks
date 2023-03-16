package org.infinispan.hotrod;

import java.nio.charset.StandardCharsets;

import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.marshall.StringMarshaller;
import org.infinispan.hotrod.impl.DataFormat;
import org.infinispan.hotrod.impl.protocol.Codec;
import org.infinispan.hotrod.impl.protocol.Codec40;

public class Constants {
   private Constants() { }

   public static final String CACHE_NAME = "default";
   public static final byte[] CACHE_NAME_BYTES = CACHE_NAME.getBytes(StandardCharsets.UTF_8);
   public static final String KEY = "key";
   public static final byte[] KEY_BYTES = KEY.getBytes(StandardCharsets.UTF_8);
   public static final String VALUE = "current-value";
   public static final byte[] VALUE_BYTES = VALUE.getBytes(StandardCharsets.UTF_8);
   public static final Codec DEFAULT_CODEC = new Codec40();

   public static final DataFormat DATA_FORMAT = DataFormat.builder()
         .keyType(MediaType.TEXT_PLAIN).keyMarshaller(new StringMarshaller(StandardCharsets.UTF_8))
         .valueType(MediaType.TEXT_PLAIN).valueMarshaller(new StringMarshaller(StandardCharsets.UTF_8))
         .build();
}
