package org.infinispan.hotrod.numeric;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

@BenchmarkMode({ Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(value = 2)
public class NumericParserBenchmark {

    @Benchmark
    public long parseNumberInfinispan(NumericBenchmarkState state) {
        if (state.elementType == NumericBenchmarkState.ElementType.INT) {
            return InfinispanParser.readVInt(state.nextData());
        }
        return InfinispanParser.readVLong(state.nextData());
    }

    @Benchmark
    public long parseNumberBranchless(NumericBenchmarkState state) {
        if (state.elementType == NumericBenchmarkState.ElementType.INT) {
            return BranchlessParser.readRawVarint32(state.nextData());
        }
        return BranchlessParser.readRawVarint64(state.nextData());
    }
}
