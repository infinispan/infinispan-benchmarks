/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.infinispan.server.resp;

import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import io.netty.buffer.ByteBuf;

@Warmup(time = 5)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class MyBenchmark {

    @Benchmark
    public ByteBuf testGetPerf(GetSetState state) {
        state.channel.writeInbound(state.GET_REQUEST.duplicate());
        state.channel.checkException();

        // Should return immediately
        ByteBuf buf = state.channel.readOutbound();
        // $ + bytes length + \r\n + (bytes) + \r\n
        int writtenAmount = buf.writerIndex() - buf.readerIndex();
        if (writtenAmount != 1 + 1 + 2 + 2 + 2) {
            throw new AssertionError("Set should have returned 5 bytes, but was " + writtenAmount);
        }
        buf.release();
        return buf;
    }

    @Benchmark
    public ByteBuf testSetPerf(GetSetState state) {
        state.channel.writeInbound(state.SET_REQUEST.duplicate());
        state.channel.checkException();

        // Should return immediately
        ByteBuf buf = state.channel.readOutbound();
        // + + OK + \r\n
        int writtenAmount = buf.writerIndex() - buf.readerIndex();
        if (writtenAmount != 1 + 2 + 2) {
            throw new AssertionError("Set should have returned 5 bytes, but was " + writtenAmount);
        }
        buf.release();
        return buf;
    }

    @Benchmark
    public void testGetPipelinePerf(PipelineState state) {
        state.channel.writeInbound(state.GET_REQUEST.writerIndex(state.getWriteIndex).readerIndex(0));
        state.channel.checkException();

        // Should return immediately
        Queue<Object> queue = state.channel.outboundMessages();
        ByteBuf outbound;

        int writtenAmount = 0;
        while ((outbound = (ByteBuf) queue.poll()) != null) {
            writtenAmount += outbound.writerIndex() - outbound.readerIndex();
            outbound.release();
        }
        // $ + bytes length + \r\n + (bytes) + \r\n
        if (writtenAmount != 8 * state.messageCount) {
            throw new AssertionError("Set should have returned " + 8 * state.messageCount + " bytes, but was " + writtenAmount);
        }
    }

    @Benchmark
    public void testSetPipelinePerf(PipelineState state) {
        state.channel.writeInbound(state.SET_REQUEST.writerIndex(state.setWriteIndex).readerIndex(0));
        state.channel.checkException();

        // Should return immediately
        Queue<Object> queue = state.channel.outboundMessages();
        ByteBuf outbound;

        int writtenAmount = 0;
        while ((outbound = (ByteBuf) queue.poll()) != null) {
            writtenAmount += outbound.writerIndex() - outbound.readerIndex();
            outbound.release();
        }
        if (writtenAmount != 5 * state.messageCount) {
            throw new AssertionError("Set should have returned " + 5 * state.messageCount + " bytes, but was " + writtenAmount);
        }
    }

    @Benchmark
    public int testSimpleStringNameParsing(NameState nameState) {
        ByteBuf buf = nameState.nextRequest().duplicate();
        buf.readerIndex(buf.readerIndex() + 2);
        String name = Intrinsics.simpleString(buf);
        return NameState.Names.handleString(name);
    }

    @Benchmark
    public int testStringNameParsing(NameState nameState) {
        ByteBuf buf = nameState.nextRequest().duplicate();
        String name = Intrinsics.bulkString(buf, LongProcessorOverride.INSTANCE);
        return NameState.Names.handleString(name);
    }

    @Benchmark
    public int testNewNameParsing(NameState nameState) {
        ByteBuf buf = nameState.nextRequest().duplicate();
        buf.readerIndex(buf.readerIndex() + 2);
        return NameState.Names.handleByteBuf(buf);
    }
}
