package dev.rosewood.rosestacker.nms.v26_2_R1.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A {@link ValueOutput} that drops every write to a top-level key in a fixed set before it is encoded, and forwards
 * everything else to its delegate unchanged.
 * <p>
 * Saving an entity and then removing some top-level keys from the resulting tag produces the same tag as saving it
 * through this output (plus the same removal, for keys written without a name through {@link #store(MapCodec, Object)}):
 * a dropped key is removed either way, and a kept key's value is written by the same call to the same delegate.
 * Skipping a key only skips encoding a value the caller already computed, so the entity is not affected.
 * <p>
 * Only top-level keys are matched. Children of kept keys are the delegate's own outputs and are not filtered; writes
 * below a dropped key go to a discarding output.
 */
public final class KeySkippingValueOutput implements ValueOutput {

    private final ValueOutput delegate;
    private final Set<String> skippedKeys;

    private KeySkippingValueOutput(ValueOutput delegate, Set<String> skippedKeys) {
        this.delegate = delegate;
        this.skippedKeys = skippedKeys;
    }

    /**
     * @param delegate the output to write kept keys to
     * @param skippedKeys the top-level keys to drop, matched exactly
     * @return {@code delegate} itself when nothing is skipped, otherwise a filtering view of it
     */
    public static ValueOutput wrap(ValueOutput delegate, Set<String> skippedKeys) {
        return skippedKeys.isEmpty() ? delegate : new KeySkippingValueOutput(delegate, skippedKeys);
    }

    private boolean skip(String name) {
        return this.skippedKeys.contains(name);
    }

    @Override
    public <T> void store(String name, Codec<T> codec, T value) {
        if (!this.skip(name))
            this.delegate.store(name, codec, value);
    }

    @Override
    public <T> void storeNullable(String name, Codec<T> codec, T value) {
        if (!this.skip(name))
            this.delegate.storeNullable(name, codec, value);
    }

    @Override
    @Deprecated
    public <T> void store(MapCodec<T> codec, T value) {
        // Inlined fields have no name to match here; the caller's key removal still covers them.
        this.delegate.store(codec, value);
    }

    @Override
    public void putBoolean(String name, boolean value) {
        if (!this.skip(name))
            this.delegate.putBoolean(name, value);
    }

    @Override
    public void putByte(String name, byte value) {
        if (!this.skip(name))
            this.delegate.putByte(name, value);
    }

    @Override
    public void putShort(String name, short value) {
        if (!this.skip(name))
            this.delegate.putShort(name, value);
    }

    @Override
    public void putInt(String name, int value) {
        if (!this.skip(name))
            this.delegate.putInt(name, value);
    }

    @Override
    public void putLong(String name, long value) {
        if (!this.skip(name))
            this.delegate.putLong(name, value);
    }

    @Override
    public void putFloat(String name, float value) {
        if (!this.skip(name))
            this.delegate.putFloat(name, value);
    }

    @Override
    public void putDouble(String name, double value) {
        if (!this.skip(name))
            this.delegate.putDouble(name, value);
    }

    @Override
    public void putString(String name, String value) {
        if (!this.skip(name))
            this.delegate.putString(name, value);
    }

    @Override
    public void putIntArray(String name, int[] value) {
        if (!this.skip(name))
            this.delegate.putIntArray(name, value);
    }

    @Override
    public ValueOutput child(String name) {
        return this.skip(name) ? Discarding.OUTPUT : this.delegate.child(name);
    }

    @Override
    public ValueOutputList childrenList(String name) {
        return this.skip(name) ? Discarding.OUTPUT_LIST : this.delegate.childrenList(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypedOutputList<T> list(String name, Codec<T> codec) {
        return this.skip(name) ? (TypedOutputList<T>) Discarding.TYPED_LIST : this.delegate.list(name, codec);
    }

    @Override
    public void discard(String name) {
        if (!this.skip(name))
            this.delegate.discard(name);
    }

    @Override
    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    /**
     * Accepts and forgets every write below a dropped key. Stateless, so one instance serves every caller and thread.
     */
    static final class Discarding implements ValueOutput {

        static final Discarding OUTPUT = new Discarding();

        static final ValueOutputList OUTPUT_LIST = new ValueOutputList() {
            @Override
            public ValueOutput addChild() {
                return OUTPUT;
            }

            @Override
            public void discardLast() {
            }

            @Override
            public boolean isEmpty() {
                return true;
            }
        };

        static final TypedOutputList<Object> TYPED_LIST = new TypedOutputList<>() {
            @Override
            public void add(Object value) {
            }

            @Override
            public boolean isEmpty() {
                return true;
            }
        };

        private Discarding() {
        }

        @Override
        public <T> void store(String name, Codec<T> codec, T value) {
        }

        @Override
        public <T> void storeNullable(String name, Codec<T> codec, T value) {
        }

        @Override
        @Deprecated
        public <T> void store(MapCodec<T> codec, T value) {
        }

        @Override
        public void putBoolean(String name, boolean value) {
        }

        @Override
        public void putByte(String name, byte value) {
        }

        @Override
        public void putShort(String name, short value) {
        }

        @Override
        public void putInt(String name, int value) {
        }

        @Override
        public void putLong(String name, long value) {
        }

        @Override
        public void putFloat(String name, float value) {
        }

        @Override
        public void putDouble(String name, double value) {
        }

        @Override
        public void putString(String name, String value) {
        }

        @Override
        public void putIntArray(String name, int[] value) {
        }

        @Override
        public ValueOutput child(String name) {
            return this;
        }

        @Override
        public ValueOutputList childrenList(String name) {
            return OUTPUT_LIST;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> TypedOutputList<T> list(String name, Codec<T> codec) {
            return (TypedOutputList<T>) TYPED_LIST;
        }

        @Override
        public void discard(String name) {
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

    }

}
