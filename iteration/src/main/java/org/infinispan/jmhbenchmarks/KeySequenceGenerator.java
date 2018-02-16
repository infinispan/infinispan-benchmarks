package org.infinispan.jmhbenchmarks;

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
@State(Scope.Benchmark)
public class KeySequenceGenerator {

	private static final int randomSeed = 17;
	@Param("10")
	private int keyObjectSize;
	@Param({"10", "100", "1000"})
	private int valueObjectSize;

	@Param({"false"})
	private boolean useStrings;

	private RandomDataGenerator generator;

	@Setup
	public void createSpace() {
		JDKRandomGenerator jdkRandomGenerator = new JDKRandomGenerator();
		jdkRandomGenerator.setSeed(randomSeed);
		generator = new RandomDataGenerator(jdkRandomGenerator);
	}

	public Object getNextKey() {
		return toUserObject(generator.nextHexString(keyObjectSize));
	}

	public Object getNextValue() {
		return toUserObject(generator.nextHexString(valueObjectSize));
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
