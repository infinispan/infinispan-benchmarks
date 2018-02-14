package org.infinispan.jmhbenchmarks;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Utility to launch the JMH benchmark from the IDE;
 * useful for debugging issues.
 */
public class IDELauncher {

	public static void main(String... args) throws Exception {
		Options opts = new OptionsBuilder()
			.include( ".*" )
			.warmupIterations( 20 )
			.measurementIterations( 200 )
			.forks( 1 )
			.build();

		new Runner(opts).run();
	}
}
