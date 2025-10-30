package org.infinispan;

import java.util.Arrays;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoSchema;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Generates keys and values to be used during the cache.
 * These instances are pre-created to minimize impact
 * during measurements.
 * Strings are a popular choice, but trigger ad-hoc optimisations
 * so we have options to test both with strings and other things.
 */
@State(Scope.Thread)
public class KeySequenceGenerator {

	private static final int randomSeed = 17;
	public static final int keySpaceSize = 10_000;
	private static final int valueSpaceSize = 1_000;

	@Param({"true","false"})
	private boolean useStrings;

	@Param({"10", "100"})
	private int keyObjectSize;

	@Param({"10", "1000", "40000"})
	private int valueObjectSize;

	private RandomSequence keySequence;
	private RandomSequence valueSequence;

	@Setup
	public void createSpace() {
		JDKRandomGenerator jdkRandomGenerator = new JDKRandomGenerator();
		jdkRandomGenerator.setSeed(randomSeed);
		RandomDataGenerator rndg = new RandomDataGenerator(jdkRandomGenerator);
		keySequence = new RandomSequence( rndg, keySpaceSize, keyObjectSize);
		valueSequence = new RandomSequence( rndg, valueSpaceSize, valueObjectSize);
	}

	public Object getNextKey() {
		return keySequence.nextValue();
	}

	public Object getNextValue() {
		return valueSequence.nextValue();
	}

	private class RandomSequence {
		private final Object[] list;
		private final int spaceSize;
	
		private int idx = -1;

		RandomSequence(RandomDataGenerator rndg, int spaceSize, int objectSize) {
			this.list = new Object[spaceSize];
			this.spaceSize = spaceSize;
			for (int i=0; i<spaceSize; i++) {
				list[i] = toUserObject(rndg.nextHexString(objectSize));
			}
		}

		Object nextValue() {
			idx++;
			if (idx==spaceSize) {
				idx = 0;
			}
			return list[idx];
		}

	}

	private Object toUserObject(String nextHexString) {
		if (useStrings) {
			return nextHexString;
		} else {
			return new ValueWrapper(nextHexString);
		}
	}

	@ProtoSchema(includeClasses = { ValueWrapper.class }, schemaFileName = "benchmark.proto", schemaFilePath = "proto", schemaPackageName = "benchmark")
	public interface BenchmarkInitializer extends SerializationContextInitializer {

	}

	public static final class ValueWrapper {

		private final byte[] bytes;
		private final int hashCode;

		public ValueWrapper(String nextHexString) {
			this(nextHexString.getBytes());
		}

		@ProtoFactory
		public ValueWrapper(byte[] bytes) {
			this.bytes = bytes;
			this.hashCode = Arrays.hashCode(bytes);
		}

		@ProtoField(1)
		public byte[] getBytes() {
			return bytes;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			else if (obj instanceof ValueWrapper)
				return Arrays.equals(bytes, ((ValueWrapper) obj).bytes);
			else
				return false;
		}

	}
}
