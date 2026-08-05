package io.github.jixingdefeng.visionrealm.core.util.random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A weighted random list backed by parallel arrays, optimized for random selection performance.
 * <p>
 * Internally uses two arrays to store values and weights. Compared to {@link WeightedRandomList}:
 * <ul>
 *   <li>Random selection is faster (contiguous array access, no extra object wrapping)</li>
 *   <li>Creation is slightly slower (due to weight array construction)</li>
 *   <li>Iteration performance is better with {@link #forEach}, while {@link #unwrap} incurs additional boxing overhead</li>
 * </ul>
 * Suitable for scenarios requiring frequent random selections.
 * <p>
 * <b>Validation on construction:</b>
 * All constructors enforce the following invariants, throwing exceptions if violated:
 * <ul>
 *   <li>The number of values and weights must match</li>
 *   <li>The list must contain at least one entry</li>
 *   <li>The total weight must be strictly greater than zero</li>
 * </ul>
 *
 * @param <T> the type of values
 * @author JiXingDeFeng
 * @see WeightedRandomList
 * @since 0.1.0
 */
public class ArrayWeightRandomList<T> {
    private final T[] values;
    private final int[] weights;
    private final int totalWeight;

    /**
     * Creates an ArrayWeightRandomList from a list of {@link WeightedEntry} objects.
     * <p>
     * This is a convenience method for creating instances from existing weighted entries.
     * The weight is extracted via {@link WeightedEntry#getWeight()}.
     *
     * @param values the list of weighted entries
     * @param <T> the type of values
     * @return a new ArrayWeightRandomList instance
     */
    public static <T extends WeightedEntry> ArrayWeightRandomList<T> create(List<T> values) {
        return new ArrayWeightRandomList<>(values, w -> w.getWeight().asInt());
    }

    public static <T> MapCodec<ArrayWeightRandomList<T>> codec(MapCodec<T> codec) {
        return RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.list(Entry.codec(codec))
                                .optionalFieldOf("values", Collections.emptyList())
                                .forGetter(ArrayWeightRandomList::unwrap)
                ).apply(instance, ArrayWeightRandomList::new)
        );
    }

    @SuppressWarnings("unchecked")
    public ArrayWeightRandomList(List<T> values, Function<T, Integer> generator) {
        this.values = (T[]) values.toArray();
        weights = new int[this.values.length];
        int totalWeight = 0;
        for (int i = 0; i < this.values.length; i++) {
            int weight = generator.apply(this.values[i]);
            weights[i] = weight;
            totalWeight += weight;
        }

        this.totalWeight = totalWeight;
        this.validate();
    }

    @SuppressWarnings("unchecked")
    public ArrayWeightRandomList(List<Entry<T>> list) {
        T[] values = (T[]) new Object[list.size()];
        int[] weights = new int[list.size()];
        int totalWeight = 0;
        for (int i = 0; i < values.length; i++) {
            Entry<T> entry = list.get(i);
            int weight = entry.weight().asInt();
            values[i] = entry.value();
            weights[i] = weight;
            totalWeight += weight;
        }

        this.values = values;
        this.weights = weights;
        this.totalWeight = totalWeight;
        this.validate();
    }

    public ArrayWeightRandomList(T[] values, int[] weights) {
        if (values.length != weights.length) {
            throw new IllegalArgumentException("The number of values and weights do not match");
        }

        this.values = values.clone();
        this.weights = weights.clone();
        int totalWeight = 0;
        for (int i : weights) {
            totalWeight += i;
        }

        this.totalWeight = totalWeight;
        this.validate();
    }
    
    public int size() {
        return this.values.length;
    }
    
    public boolean isEmpty() {
        return this.values.length == 0;
    }
    
    public boolean contains(T value) {
        for (T v : values) {
            if (v.equals(value)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Returns a randomly selected value based on the weights.
     * <p>
     * The probability of selecting a value is proportional to its weight.
     *
     * @param random the random source
     * @return an Optional containing the selected value, or empty if the list is empty or totalWeight is zero
     */
    public Optional<T> getRandom(RandomSource random) {
        int pos = random.nextInt(this.totalWeight);
        for (int i = 0; i < this.weights.length; i++) {
            pos -= weights[i];
            if (pos < 0) {
                return Optional.of(this.values[i]);
            }
        }

        return Optional.empty();
    }

    /**
     * Returns an unmodifiable list of {@link Entry} objects representing this weighted list.
     *
     * @return the list of entries
     */
    public List<Entry<T>> unwrap() {
        List<Entry<T>> entries = new ArrayList<>(this.values.length);
        for (int i = 0; i < this.values.length; i++) {
            entries.add(Entry.of(this.values[i], this.weights[i]));
        }

        return Collections.unmodifiableList(entries);
    }

    /**
     * Returns an unmodifiable list of the raw values (weights are discarded).
     *
     * @return the list of values
     */
    public List<T> unwrapValue() {
        return Arrays.asList(this.values);
    }

    /**
     * Performs the given action for each entry in this weighted list.
     * <p>
     * The action receives both the value and its weight. This method avoids
     * creating intermediate {@link Entry} objects, making it more efficient
     * than calling {@link #unwrap()} and iterating over the resulting list.
     *
     * @param consumer the action to be performed for each entry
     */
    public void forEach(BiConsumer<T, Integer> consumer) {
        for (int i = 0; i < this.values.length; i++) {
            consumer.accept(this.values[i], this.weights[i]);
        }
    }

    public record Entry<T>(T value, Weight weight) implements WeightedEntry {
        public static <T> Entry<T> of(T value, int weight) {
            return new Entry<>(value, Weight.of(weight));
        }

        public static <T> Codec<Entry<T>> codec(MapCodec<T> codec) {
            return RecordCodecBuilder.create(instance ->
                    instance.group(
                            codec.forGetter(Entry::value),
                            Weight.CODEC.optionalFieldOf("weight", Weight.of(1)).forGetter(Entry::weight)
                    ).apply(instance, Entry<T>::new)
            );
        }

        @Override
        @NotNull
        public Weight getWeight() {
            return this.weight;
        }
    }

    protected void validate() {
        if (this.values.length != this.weights.length) {
            throw new IllegalArgumentException("The number of values and weight do not match");
        } else if (this.values.length == 0) {
            throw new IllegalStateException("ArrayWeightRandomList is empty");
        } else if (this.totalWeight == 0) {
            throw new IllegalStateException("ArrayWeightRandomList total weight cannot be 0");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if(obj != null && this.getClass() == obj.getClass()) {
            ArrayWeightRandomList<?> list = (ArrayWeightRandomList<?>) obj;
            if (this.values != list.values) {
                return false;
            } else {
                return Arrays.equals(this.weights, list.weights);
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(this.values), Arrays.hashCode(this.weights));
    }
}
