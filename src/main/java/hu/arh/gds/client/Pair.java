/*
 * Intellectual property of ARH Inc.

 * Budapest, 2020/09/29
 */

package hu.arh.gds.client;

import java.util.Objects;

/**
 * Generic class containing a pair of two values.
 * These values can change on-the-fly without any restrictions, making this class mutable.
 *
 * @param <U> the type of the first component of the pair
 * @param <V> the type of the second component of the pair
 */
public class Pair<U, V> {
    private U first;
    private V second;

    /**
     * Constructs a pair with the given values
     *
     * @param first  initial value for the first component
     * @param second initial value for the second component
     */
    public Pair(U first, V second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Construct a pair with the values set to {@code null}.
     */
    public Pair() {
    }

    /**
     * Returns the first component of the pair
     *
     * @return the first component
     */
    public U getFirst() {
        return first;
    }

    /**
     * Sets the first component of the pair.
     *
     * @param first the new value for the first component
     */
    public void setFirst(U first) {
        this.first = first;
    }

    /**
     * Returns the second component of the pair
     *
     * @return the second component
     */
    public V getSecond() {
        return second;
    }

    /**
     * Sets the second component of the pair.
     *
     * @param second the new value for the first component
     */
    public void setSecond(V second) {
        this.second = second;
    }

    /**
     * Two pairs are equal if and only if their first components are equal and their second components are equal as well.
     *
     * @param o the object to compare to
     * @return whether the two pairs are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) &&
                Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "Pair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
