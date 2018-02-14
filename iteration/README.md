# JMH Performance tests

## Build

To build the benchmark iteration tests:

    $ mvn clean install


## Run it from command line using an "uber jar"

There is an alternative way to run it which might be handy when needing to copy the
performance test to a dedicated server and for some reason you're not liking rsync:

    $ java -jar target/benchmarks.jar


## Run flight recorder on Infinispan with many iterations

   $ java -jar target/benchmarks.jar -jvmArgsPrepend "-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints -XX:FlightRecorderOptions=stackdepth=128" -e NoRehash -i 500 -t 2

 - jvmArgsPrepend: allows to add additional JVM properties to the forked process which runs the benchmark
 - e: excludes running all tests including this name (for example ignore tests that don't have rehash enabled)
 - i: sets the number of iterations. Setting it very high here to have time to play with Flight Recorder before it shuts down
 - t: sets the number of worker threads. If you wish to test concurrent iteration requests

 ## Set benchmark parameters

Benchmark parameters (those who use @Param) will get the benchmark repeated to satisfy each possible permutation.
Sometimes you want to limit execution to a specific condition, e.g.:

   -p useStrings=false

Will only use objects for the payload and skip the test run using strings. Strings are quite a bit slower.

For all possible paramters see InfinispanHolder and KeySequenceGenerator

## Start a cluster node without the benchmark

Infinispan node; set the appropriate interface address; in this case you can use a pattern:
   $ java -cp target/benchmarks.jar -Dbind_address=match-address:192.168.* -Djava.net.preferIPv4Stack=true org.infinispan.jmhbenchmarks.InfinispanNodeStarter

This needs to be added still.

## Start a distributed benchmark

To run the benchmark and have it connect to other nodes on the cluster but running "standalone" (see previous paragraph),
set the 'distributed=true' flag.

For example to run all Hazelcast test benchmarks while connecting to other nodes on the cluster:

   $ java -jar target/benchmarks.jar -jvmArgsPrepend "-Dbind_address=192.168.1.20 -Djava.net.preferIPv4Stack=true -Ddistributed=true" -e getPutInfinispan -e infinispanG -e infinispanP


# Notes

For best results disable features such as power management, dynamic CPU scaling,
and run it on a dedicated box which has no other significant services running.
So the "run it from your IDE" approach is just meant for development of new tests.

# Network checklist

 - Disable any firewall! e.g. iptables

Make sure you have a route for the 232.0.0.0 routed to the network interface connected to the cluster:

Sample route output:
232.0.0.0       0.0.0.0         248.0.0.0       U     0      0        0 enp0s25


## TODO

- make sure there's some key overlap between the threads (and unit tests for that?)
- have it run the other nodes in physically different boxes
- improve scenario parameters

