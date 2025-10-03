package org.infinispan.jmhbenchmarks;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.infinispan.jmhbenchmarks.state.PutKeyValueCommandState;
import org.infinispan.jmhbenchmarks.state.UserState;
import org.infinispan.jmhbenchmarks.util.ResettableInputStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.BenchmarkException;

@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 3)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MarshallerBenchmark {

    @Benchmark
    public byte[] testUserToByteBuffer(InfinispanHolder holder, UserState userState) throws Exception {
        return holder.getMarshaller().objectToByteBuffer(userState.getUser());
    }

    @Benchmark
    public Object testUserFromByteBuffer(InfinispanHolder holder, UserState userState) throws Exception {
        return holder.getMarshaller().objectFromByteBuffer(userState.getUserBytes());
    }

//    @Benchmark
//    public byte[] testPutKeyValueCommandToByteBuffer(InfinispanHolder holder, PutKeyValueCommandState putKeyValueCommandState) throws Exception {
//        return holder.getMarshaller().objectToByteBuffer(putKeyValueCommandState.getPutKeyValueCommand());
//    }

//    @Benchmark
//    public Object testPutKeyValueCommandFromByteBuffer(InfinispanHolder holder, PutKeyValueCommandState putKeyValueCommandState) throws Exception {
//        return holder.getMarshaller().objectFromByteBuffer(putKeyValueCommandState.getPutKeyValueCommandBytes());
//    }

//   @Benchmark
//   public Object testUserFromStream(InfinispanHolder holder, UserState userState) throws Exception {
//      ResettableInputStream ris = userState.getUserStream();
//      return holder.getStreamAwareMarshaller().readObject(userState.getUserStream(), ris.length());
//   }

//   @Benchmark
//   public Object testPutKeyValueCommandFromStream(InfinispanHolder holder, PutKeyValueCommandState putKeyValueCommandState) throws Exception {
//      ResettableInputStream ris = putKeyValueCommandState.getPutKeyValuecommandStream();
//      return holder.getStreamAwareMarshaller().readObject(ris, ris.length());
//   }

//   @Benchmark
//   public Object testPutKeyValueCommandFromStreamOld(InfinispanHolder holder, PutKeyValueCommandState putKeyValueCommandState) throws Exception {
//      return holder.getStreamAwareMarshaller().readObject(putKeyValueCommandState.getPutKeyValuecommandStream());
//   }

    public static void main(String[] args) throws Exception {
       // Force the main method to not have forks so we can remotely debug
       args = Arrays.copyOf(args, args.length + 1);
       args[args.length - 1] = "-f0";
       args = Arrays.copyOf(args, args.length + 2);
       args[args.length - 2] = "-puseUserObjectValue=true";
       args[args.length - 1] = "-pmarshallerType=protostream";
       org.openjdk.jmh.Main.main(args);
    }
}
