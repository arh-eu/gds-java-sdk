/*
 * Intellectual property of ARH Inc.
 * This file belongs to the GDS 5.1 system in the gds-messages project.
 * Budapest, 2020/10/19
 */

package hu.arheu.gds.message.clienttypes;

import hu.arheu.gds.client.Pair;
import hu.arheu.gds.message.header.MessageHeaderBase;

import java.util.Objects;

/**
 * Abstract class indicating that this message is treated as a message from the GDS.
 *
 * @param <T> the type of the Data part.
 */
abstract class GDSMessage<T> {

    protected final MessageHeaderBase header;
    protected final T data;

    GDSMessage(Pair<MessageHeaderBase, T> response) {
        this.header = response.getFirst();
        this.data = response.getSecond();
    }

    GDSMessage(MessageHeaderBase header, T data) {
        this.header = header;
        this.data = data;
    }

    /**
     * Returns the associated header from the message
     *
     * @return the header
     */
    public MessageHeaderBase getHeader() {
        return header;
    }

    /**
     * Returns the data part from the message
     *
     * @return the data
     */
    public T getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GDSMessage<?> that = (GDSMessage<?>) o;
        return Objects.equals(header, that.header) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, data);
    }

    @Override
    public String toString() {
        return "GDSMessage{" +
                "header=" + header +
                ", data=" + data +
                '}';
    }
}
