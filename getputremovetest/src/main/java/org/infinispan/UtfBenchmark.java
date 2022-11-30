package org.infinispan;

import java.io.IOException;

import org.openjdk.jmh.annotations.Benchmark;

public class UtfBenchmark {

   @Benchmark
   public void testUtfWrite(UtfSetup utfSetup) throws IOException {
      utfSetup.dataOutput.writeUTF(utfSetup.string);
      utfSetup.reset();
   }
}
