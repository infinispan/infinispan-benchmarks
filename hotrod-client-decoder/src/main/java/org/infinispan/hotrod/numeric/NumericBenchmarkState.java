package org.infinispan.hotrod.numeric;

import java.util.Random;
import java.util.stream.IntStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class NumericBenchmarkState {

    private static final int SEED = 0;

    public enum InputDistribution {
        SMALL,
        LARGE,
        MEDIUM,
        ALL
    }

    public enum ElementType {
        INT {
            @Override
            public int maxWidth() {
                return 5;
            }
        },

        LONG {
            @Override
            public int maxWidth() {
                return 9;
            }
        };

        public ByteBuf generateData(int varintLength) {
            ByteBuf buf = Unpooled.buffer(1);
            byte[] bytes = new byte[maxWidth()];
            for (int i = 0; i < (varintLength - 1); i++) {
                buf.writeByte((byte) 150);
            }
            // delimiter
            buf.writeByte(1);
            return buf;
        }

        public abstract int maxWidth();
    }

    @Param({ "1", "128", "128000" })
    int inputs;

    @Param
    InputDistribution inputDistribution;

    @Param
    ElementType elementType;

    ByteBuf[] data;
    int index;

    @Setup
    public void init() {
        ByteBuf[] dataSet;
        Random rnd = new Random(SEED);
        int maxSize = elementType.maxWidth();
        switch (inputDistribution) {
            case SMALL:
                int smaller = maxSize / 2;
                dataSet = IntStream.range(1, smaller + 1)
                        .mapToObj(s -> elementType.generateData(s))
                        .toArray(ByteBuf[]::new);
                break;
            case LARGE:
                dataSet = new ByteBuf[] {
                        elementType.generateData(maxSize)
                };
                if (inputs > 1) {
                    System.exit(1);
                }
                break;
            case MEDIUM:
                int lowQuarter = (int) (maxSize * 0.25);
                int highQuarter = (int) (maxSize * 0.75);
                dataSet = IntStream.range(lowQuarter, highQuarter + 1)
                        .mapToObj(s -> elementType.generateData(s))
                        .toArray(ByteBuf[]::new);
                break;
            case ALL:
                dataSet = IntStream.range(1, maxSize + 1)
                        .mapToObj(s -> elementType.generateData(s))
                        .toArray(ByteBuf[]::new);
                break;
            default:
                throw new RuntimeException("Unknown distribution");
        }
        data = new ByteBuf[inputs];
        for (int i = 0; i < inputs; i++) {
            data[i] = dataSet[rnd.nextInt(dataSet.length)];
        }
        index = 0;
    }

    public ByteBuf nextData() {
        index++;
        if (index == data.length) {
            index = 0;
        }
        return data[index].resetReaderIndex();
    }
}
