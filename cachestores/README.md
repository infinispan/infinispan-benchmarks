# JMH Performance tests

## Build

To build the benchmark cachestore tests:

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

# Notes

For best results disable features such as power management, dynamic CPU scaling,
and run it on a dedicated box which has no other significant services running.
So the "run it from your IDE" approach is just meant for development of new tests.
