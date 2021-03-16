package org.infinispan.jmhbenchmarks;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
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
	@Param({ "50000", "250000"})
	public int distinctEntries;
	@Param("32")
	private int keyObjectSize;
	@Param("100")
	private int distinctValues = 100;
	@Param("1000")
	private int valueObjectSize = 1000;

	@Param({"true","false"})
	private boolean useStrings;

	private RandomSequence keySequence;
	private RandomSequence valueSequence;

	@Setup
	public void createSpace() {
		JDKRandomGenerator jdkRandomGenerator = new JDKRandomGenerator();
		jdkRandomGenerator.setSeed(randomSeed);
		RandomDataGenerator rndg = new RandomDataGenerator(jdkRandomGenerator);
		keySequence = new RandomSequence( rndg, distinctEntries, keyObjectSize);
		valueSequence = new RandomSequence( rndg, distinctValues, valueObjectSize);
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

	public static final class ValueWrapper implements Serializable {

		@ProtoField(number = 1)
		final byte[] bytes;
		final int hashCode;

		public ValueWrapper(String nextHexString) {
			this(nextHexString.getBytes());
		}

		@ProtoFactory
		ValueWrapper(byte[] bytes) {
			this.bytes = bytes;
			this.hashCode = Arrays.hashCode(bytes);
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
