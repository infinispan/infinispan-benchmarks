package org.infinispan.jmhbenchmarks;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.infinispan.commons.io.UnsignedNumeric;
import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.commons.util.Util;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Generates keys and values to be used during the cache.
 */
@State(Scope.Benchmark)
public class KeySequenceGenerator {

   private static final int randomSeed = 17;
   @Param("2000")
   private int keyObjectSize;
   @Param({"10"})
   private int valueObjectSize;

   private Random generator;

   @Setup
   public void createSpace() {
      generator = new Random(randomSeed);
   }

   public Object getNextKey() {
      return getObject(keyObjectSize);
   }

   public Object getNextValue() {
      return getObject(valueObjectSize);
   }

   private Object getObject(int valueObjectSize) {
      byte[] bytes = new byte[valueObjectSize];
      generator.nextBytes(bytes);
      return bytes;
   }

   public int getKeyObjectSize() {
      return keyObjectSize;
   }

   public int getValueObjectSize() {
      return valueObjectSize;
   }
}
