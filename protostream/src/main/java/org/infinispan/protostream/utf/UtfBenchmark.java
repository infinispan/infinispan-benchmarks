package org.infinispan.protostream.utf;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.AverageTime)
public class UtfBenchmark {
   @Benchmark
   @CompilerControl(CompilerControl.Mode.DONT_INLINE)
   @OutputTimeUnit(TimeUnit.NANOSECONDS)
   public void testUtfWrite(UtfSetup utfSetup, Blackhole bh) throws IOException {
      utfSetup.strWriter.writeUTF(utfSetup.string);
      bh.consume(utfSetup.strWriter);
      utfSetup.reset();
   }
}
