package org.infinispan.hotrod;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Warmup;

@Warmup(time = 5)
@Fork(value = 1, jvmArgs = {"-Xmx10G"})
public class ParsingBenchmark {

   @Benchmark
   public void runParser(BenchmarkSetup setup) {
      setup.channel.writeInbound(setup.prepareGetOperation());
   }

   public static void main(String[] args) {
      BenchmarkSetup setup = new BenchmarkSetup();
      setup.decoder = BenchmarkSetup.Decoder.MANUAL;
      setup.operation = BenchmarkSetup.Operation.GET;
      setup.initializeState();
      ParsingBenchmark bench = new ParsingBenchmark();
      int i = 0;
      while (i++ < 3) {
         System.out.println("WRITE: " + i);
         bench.runParser(setup);
      }
      setup.teardown();
   }
}
