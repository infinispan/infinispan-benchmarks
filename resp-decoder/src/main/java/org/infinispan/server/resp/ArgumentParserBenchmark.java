package org.infinispan.server.resp;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Warmup(time = 5)
@Fork(1)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ArgumentParserBenchmark {
   private static final byte[] ALL_NUMBERS = new byte[] {
         0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30
   };

   private static long toLong(byte[] argument) {
      if (argument == null || argument.length == 0)
         throw new NumberFormatException("Empty argument");

      boolean negative = false;
      int i = 0;
      if (argument[0] < '0') {
         if ((argument[0] != '-' && argument[0] != '+') || argument.length == 1)
            throw new NumberFormatException("Invalid character: " + argument[0]);

         negative = true;
         i = 1;
      }

      long result;
      byte b = argument[i++];
      if (b < '0' || b > '9')
         throw new NumberFormatException("Invalid character: " + b);
      result = (b - 48);

      for (; i < argument.length; i++) {
         b = argument[i];
         if (b < '0' || b > '9')
            throw new NumberFormatException("Invalid character: " + b);
         result = (result << 3) + (result << 1) + (b - 48);
      }
      return negative ? -result : result;
   }

   @Benchmark
   public void testOldParse(ArgumentGeneratorState state, Blackhole blackhole) {
      blackhole.consume(Long.parseLong(new String(state.value, StandardCharsets.US_ASCII)));
   }

   @Benchmark
   public void testNewParse(ArgumentGeneratorState state, Blackhole blackhole) {
      blackhole.consume(toLong(state.value));
   }

   @State(Scope.Benchmark)
   public static class ArgumentGeneratorState {
      @Param({"3", "10", "15"})
      public int numDigits;

      @Param({"true", "false"})
      public boolean negative;

      private byte[] value;

      @Setup
      public void initializeState() {
         int i;
         byte[] value;
         if (negative) {
            value = new byte[numDigits + 1];
            value[0] = '-';
            i = 1;
         } else {
            value = new byte[numDigits];
            i = 0;
         }

         for (; i < value.length; i++) {
            value[i] = ALL_NUMBERS[i % ALL_NUMBERS.length];
         }

         this.value = value;
      }
   }
}
