#!/usr/bin/env bash

set -e

JAVA8_HOME=~/Tools/java8
JFR8_OPTIONS="-XX:+UnlockCommercialFeatures -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints \
-XX:+FlightRecorder -XX:FlightRecorderOptions=stackdepth=128,dumponexitpath=. \
-XX:StartFlightRecording=settings=profile.jfc,delay=0s,dumponexit=true"

JAVA_HOME=$JAVA8_HOME
PATH=$JAVA8_HOME:$PATH

mvn package -Dinfinispan.version=9.4.15.Final

$JAVA_HOME/bin/java -jar target/benchmarks.jar -jvmArgsPrepend "$JFR8_OPTIONS" 2>&1 | tee "console-$(date +%Y-%m-%d_%H-%M).log"

JFR_BASENAME=$(basename "$(ls -t *.jfr | head -1)" .jfr)

~/Work/jfr-report-tool/jfr-report-tool -e none -w 1900 --flamegraph-command ~/Work/FlameGraph/flamegraph.pl $JFR_BASENAME$I.jfr


mvn package -Dinfinispan.version=10.0.0.CR1

$JAVA_HOME/bin/java -jar target/benchmarks.jar -jvmArgsPrepend "$JFR8_OPTIONS" 2>&1 | tee "console-$(date +%Y-%m-%d_%H-%M).log"

JFR_BASENAME=$(basename "$(ls -t *.jfr | head -1)" .jfr)

~/Work/jfr-report-tool/jfr-report-tool -e none -w 1900 --flamegraph-command ~/Work/FlameGraph/flamegraph.pl $JFR_BASENAME$I.jfr
