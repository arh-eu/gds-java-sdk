/*
 * Intellectual property of ARH Inc.

 * Budapest, 2020/10/05
 */

package hu.arheu.gds.client;


import java.util.Objects;

/**
 * Either class, which behaves like a {@link Pair}, but only one of the values is in use at a time.
 *
 * @param <L> type of the Left value
 * @param <R> type of the Right value
 */
public final class Either<L, R> {
    private Pair<L, R> pair;
    private boolean isLeftSet;
    private boolean isRightSet;

    /**
     * Constructs a new Either object from the given value setting its Left side.
     *
     * @param left the initial value for the Left side
     * @param <L>  type of the Left parameter
     * @param <R>  type of the Right parameter
     * @return the new Either object with a set left side
     */
    public static <L, R> Either<L, R> fromLeft(L left) {
        return new Either<>(left, null, true, false);
    }


    /**
     * Constructs a new Either object from the given value setting its Right side.
     *
     * @param right the initial value for the Right side
     * @param <L>   type of the Left parameter
     * @param <R>   type of the Right parameter
     * @return the new Either object with a set right side
     */
    public static <L, R> Either<L, R> fromRight(R right) {
        return new Either<>(null, right, false, true);
    }

    private Either(L left, R right, boolean isLeftSet, boolean isRightSet) {
        pair = new Pair<>(left, right);
        this.isLeftSet = isLeftSet;
        this.isRightSet = isRightSet;
    }

    /**
     * Returns whether the left component is set in the object.
     *
     * @return {@code true} if the left is set, {@code false} otherwise.
     */
    public boolean isLeftSet() {
        return isLeftSet;
    }


    /**
     * Returns whether the right component is set in the object.
     *
     * @return {@code true} if the right is set, {@code false} otherwise.
     */
    public boolean isRightSet() {
        return isRightSet;
    }

    /**
     * Sets the left side value of this object.
     * After calling, {@link Either#isLeftSet()} will return {@code true}, while {@link Either#isRightSet()} will give {@code false}.
     *
     * @param left the new left value
     */
    public void setLeft(L left) {
        pair.setFirst(left);
        this.isLeftSet = true;
        this.isRightSet = false;
    }

    /**
     * Returns the value set to the Left side. If the Right side is in use, method will throw an {@link IllegalStateException}
     *
     * @return the left value
     */
    public L getLeft() {
        if (isLeftSet) {
            return pair.getFirst();
        } else {
            throw new IllegalStateException("Either<> does not have its left side set!");
        }
    }

    /**
     * Sets the right side value of this object.
     * After calling, {@link Either#isRightSet()} will return {@code true}, while {@link Either#isLeftSet()} will give {@code false}.
     *
     * @param right the new right value
     */
    public void setRight(R right) {
        pair.setSecond(right);
        this.isRightSet = true;
        this.isLeftSet = false;
    }


    /**
     * Returns the value set to the Right side. If the Left side is in use, method will throw an {@link IllegalStateException}
     *
     * @return the right value
     */
    public R getRight() {
        if (isRightSet) {
            return pair.getSecond();
        } else {
            throw new IllegalStateException("Either<> does not have its right side set!");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Either<?, ?> either = (Either<?, ?>) o;
        if (isLeftSet()) {
            return either.isLeftSet() && Objects.equals(pair.getFirst(), either.pair.getFirst());
        } else if (isRightSet()) {
            return either.isRightSet() && Objects.equals(pair.getSecond(), either.pair.getSecond());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(pair, isLeftSet, isRightSet);
    }

    @Override
    public String toString() {
        return '{' + (isLeftSet() ? getLeft() + ";_" : "_;" + getRight()) + '}';
    }
}
