package org.infinispan.jmhbenchmarks;

public final class SharedConfigurationSettings {

	/**
	 * Controls which interface we bind to.
	 * Currently needed for Hazelcast only: JGroups applies this same property but
	 * that's done by the configuration file.
	 */
	static final String bind_address = System.getProperty( "bind_address", "127.0.0.1" );

	/**
	 * Let it know that the benchmark is run in multiple independent JVMs.
	 * For example, prevent Hazelcast to kill the whole cluster but only
	 * shut down the local node.
	 */
	static final boolean distributedRun = Boolean.valueOf(System.getProperty("distributed", "false"));


}
