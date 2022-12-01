package org.infinispan.json;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Fork(1)
@Warmup(time = 5)
public class JsonBenchmark {

   @Benchmark
   public void testStringEscape(JsonSetup json, Blackhole bh) {
      bh.consume(json.parse());
   }
}
