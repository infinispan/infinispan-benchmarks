package org.infinispan.jmhbenchmarks;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.infinispan.jmhbenchmarks.state.PutKeyValueCommandState;
import org.infinispan.jmhbenchmarks.state.UserState;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 3)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MarshallerBenchmark {

    @Benchmark
    public byte[] testObjectToByteBuffer(InfinispanHolder holder, UserState userState) throws Exception {
        return holder.getMarshaller().objectToByteBuffer(userState.getUser());
    }

    @Benchmark
    public Object testObjectFromByteBuffer(InfinispanHolder holder, UserState userState) throws Exception {
        return holder.getMarshaller().objectFromByteBuffer(userState.getUserBytes());
    }

    @Benchmark
    public byte[] testPutKeyValueCommandToByteBuffer(InfinispanHolder holder, PutKeyValueCommandState putKeyValueCommandState) throws Exception {
        return holder.getMarshaller().objectToByteBuffer(putKeyValueCommandState.getPutKeyValueCommand());
    }

    @Benchmark
    public Object testPutKeyValueCommandFromByteBuffer(InfinispanHolder holder, PutKeyValueCommandState putKeyValueCommandState) throws Exception {
        return holder.getMarshaller().objectFromByteBuffer(putKeyValueCommandState.getPutKeyValueCommandBytes());
    }

    public static void main(String[] args) throws Exception {
        // Force the main method to not have forks so we can remotely debug
        args = Arrays.copyOf(args, args.length + 1);
        args[args.length - 1] = "-f0";
        org.openjdk.jmh.Main.main(args);
    }
}
