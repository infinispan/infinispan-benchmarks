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

package org.infinispan;

import org.infinispan.commons.api.query.Query;
import org.infinispan.commons.util.CloseableIterator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.infra.Blackhole;

@Fork(value=2, jvmArgs = {
      "-Xmx15G",
      "-Xms15G",
      "-XX:+HeapDumpOnOutOfMemoryError",
      "-Xss512k",
})
public class MyBenchmark {

    private static final String QUERY_STRING = "SELECT Type_of_SFT, SUM(Count_of_UTI) FROM org.infinispan.CacheValue WHERE Received_Report_Date BETWEEN :start AND :end GROUP BY Type_of_SFT ORDER BY Type_of_SFT";

    @Benchmark
    public int testMethod(InfinispanHolder holder, Blackhole blackhole) {
        // This is a demo/sample template for building your JMH benchmarks. Edit as needed.
        // Put your benchmark code here.

        Query<CacheValue> query = holder.getCache().query(QUERY_STRING);
        query.setParameter("start", holder.startInterval());
        query.setParameter("end", holder.endInterval());

        int counter = 0;
        CloseableIterator<CacheValue> it = query.iterator();
        while (it.hasNext()) {
            counter++;
            blackhole.consume(it.next());
        }

        it.close();
        return counter;
    }
}
