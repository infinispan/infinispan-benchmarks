package org.infinispan.jmhbenchmarks;

import java.io.IOException;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Fork(value=1, jvmArgs = {
		"-Xmx6G",
		"-Xms4G",
		"-XX:+HeapDumpOnOutOfMemoryError",
		"-XX:+AlwaysPreTouch",
		"-Xss512k",
		"-XX:HeapDumpPath=/tmp/java_heap",
		"-Djava.net.preferIPv4Stack=true",
//		"-XX:+UseLargePages",
		"-XX:LargePageSizeInBytes=2m",
		"-Dlog4j.configurationFile=log4j2.xml",
//		"-agentlib:jdwp=transport=dt_socket,server=n,address=denulu-tp3:5005,suspend=y",
		"-Dorg.infinispan.feature.data-segmentation=false"})
@BenchmarkMode(Mode.SingleShotTime)
@Threads(1)
@Warmup(iterations = 10)
@Measurement(iterations = 50)
public class JMHBenchmarks {

	@Benchmark
	@SuppressWarnings("rawtypes")
	public void scaleUpAndDown(InfinispanHolder ih, Blackhole bh) throws IOException {
		ih.scaleUp();
		ih.scaleDown();
	}
}
