package org.infinispan.server.resp;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
public class EnumParserBenchmark {

   private static final ClientEnumOptions[] OPTIONS = ClientEnumOptions.values();


   private static <T extends Enum<T>> T parseEnumNew(byte[] bytes, T[] options) {
      for (T e: options) {
         String name = e.name();
         if (name.length() != bytes.length) continue;
         if (match(bytes, name)) return e;
      }

      throw new IllegalArgumentException("Not found enum with name " + new String(bytes, StandardCharsets.US_ASCII));
   }

   private static boolean match(byte[] bytes, String name) {
      for (int i = 0; i < name.length(); i++) {
         char c = name.charAt(i);
         if (c == '-' && bytes[i] != '_' && bytes[i] != '-') return false;
         if (!Util.caseInsensitiveAsciiCheck(c, bytes[i])) return false;
      }

      return true;
   }

   @Benchmark
   public void testNewParser(ArgumentGeneratorState state, Blackhole blackhole) {
      for (int i = 0; i < state.numberOfArguments; i++) {
         ClientEnumOptions e = parseEnumNew(state.arguments[i], OPTIONS);
         switch (e) {
            case CACHING:
            case UNPAUSE:
            case PAUSE:
            case NO_EVICT:
            case GETREDIR:
            case UNBLOCK:
            case REPLY:
            case SETINFO:
            case SETNAME:
            case NO_TOUCH:
            case GETNAME:
            case ID:
            case INFO:
            case LIST:
            case TRACKING:
            case TRACKINGINFO:
               blackhole.consume(e);
               break;
            default:
               throw new IllegalArgumentException("Should not be default");
         }
      }
   }

   @Benchmark
   public void testOldParsing(ArgumentGeneratorState state,  Blackhole blackhole) {
      for (int i = 0; i < state.numberOfArguments; i++) {
         String e = new String(state.arguments[i], StandardCharsets.US_ASCII).toUpperCase();
         switch (e) {
            case "CACHING":
            case "UNPAUSE":
            case "PAUSE":
            case "NO_EVICT":
            case "GETREDIR":
            case "UNBLOCK":
            case "REPLY":
            case "SETINFO":
            case "SETNAME":
            case "GETNAME":
            case "NO_TOUCH":
            case "ID":
            case "INFO":
            case "LIST":
            case "TRACKING":
            case "TRACKINGINFO":
               blackhole.consume(e);
               break;
            default:
               throw new IllegalArgumentException("Should not be default");
         }
      }
   }

   @State(Scope.Benchmark)
   public static class ArgumentGeneratorState {

      @Param({"1", "5", "10"})
      public int numberOfArguments;

      private byte[][] arguments;

      @Setup
      public void initializeState() {
         List<byte[]> v = new ArrayList<>();
         int i = 0;
         for (ClientEnumOptions value : ClientEnumOptions.values()) {
            if (i++ >= numberOfArguments) {
               break;
            }
            v.add(value.name().getBytes(StandardCharsets.US_ASCII));
         }
         arguments = v.toArray(new byte[0][]);
      }
   }

   private enum ClientEnumOptions {
      CACHING,
      UNPAUSE,
      PAUSE,
      NO_EVICT {
         @Override
         public String toString() {
            return "no-evict";
         }
      },
      GETREDIR,
      UNBLOCK,
      REPLY,
      NO_TOUCH {
         @Override
         public String toString() {
            return "no-touch";
         }
      },
      SETINFO,
      SETNAME,
      GETNAME,
      ID,
      INFO,
      LIST,
      TRACKING,
      TRACKINGINFO,
   }
}
