/*
 * Intellectual property of ARH Inc.
 * This file belongs to the GDS 5.1 system in the gds-messages project.
 * Budapest, 2020/10/07
 */

package hu.arh.gds.client;

import java.util.Objects;

public class Tuple<U, V, W> extends Pair<U, V> {
    private W third;

    public Tuple(U first, V second, W third) {
        super(first, second);
        this.third = third;
    }

    public Tuple() {
    }

    public W getThird() {
        return third;
    }

    public void setThird(W third) {
        this.third = third;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Tuple<?, ?, ?> tuple = (Tuple<?, ?, ?>) o;
        return Objects.equals(third, tuple.third);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), third);
    }


    @Override
    public String toString() {
        return "Pair{" +
                "first=" + getFirst() +
                ", second=" + getSecond() +
                ", third=" + third +
                '}';
    }
}
