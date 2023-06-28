package org.infinispan;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.infinispan.commons.io.UnsignedNumeric;
import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.commons.util.Util;
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
	public static final int keySpaceSize = 1000;
	private static final int keyObjectSize = 10;
	private static final int valueSpaceSize = 100;
	private static final int valueObjectSize = 1000;

	@Param({"true","false"})
	private boolean useStrings;

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

	private static final class ValueWrapper implements Serializable {

		private final byte[] bytes;
		private final int hashCode;

		public ValueWrapper(String nextHexString) {
			this(nextHexString.getBytes());
		}

		protected ValueWrapper(byte[] bytes) {
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

	public static class ValueWrapperSerializer implements AdvancedExternalizer<ValueWrapper> {

		@Override
		public Set<Class<? extends ValueWrapper>> getTypeClasses() {
			return Util.asSet(ValueWrapper.class);
		}

		@Override
		public Integer getId() {
			return 205;
		}

		@Override
		public void writeObject(ObjectOutput objectOutput, ValueWrapper valueWrapper) throws IOException {
			UnsignedNumeric.writeUnsignedInt(objectOutput, valueWrapper.bytes.length);
			objectOutput.write(valueWrapper.bytes);
		}

		@Override
		public ValueWrapper readObject(ObjectInput objectInput) throws IOException, ClassNotFoundException {
			int size = UnsignedNumeric.readUnsignedInt(objectInput);
			byte[] bytes = new byte[size];
			objectInput.read(bytes);
			return new ValueWrapper(bytes);
		}
	}
}
