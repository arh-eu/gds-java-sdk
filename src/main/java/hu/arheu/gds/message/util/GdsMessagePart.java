package hu.arheu.gds.message.util;

import hu.arheu.gds.message.errors.ReadException;
import hu.arheu.gds.message.errors.ValidationException;
import hu.arheu.gds.message.errors.WriteException;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * The interface represents every kind of information that can be serialized and sent
 * by the GDS.
 * As internal serialization is used to forward messages between modules, utility methods are included as well.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"}) //API class, not all methods are used across the project.
public interface GdsMessagePart extends Externalizable {

    long serialVersionUID = 1629726509221L; //2021.08.23. - 15:48

    /**
     * Returns the current object in its serialized, raw-byte format representation.
     *
     * @return the raw byte representation.
     */
    byte[] getBinary();

    /**
     * Returns the number of elements this object will be serialized into.
     * If an element contains an array value, it is counted as one as not as the number-of-elements-of-the-array.
     *
     * @return the number of elements
     */
    int getNumberOfPublicElements();

    /**
     * Returns the size of this object once serialized into binary format.
     * If the object was not yet serialized, it will serialize it first.
     *
     * @return the size of the serialized object-binary in bytes.
     */
    int getMessageSize();

    /**
     * Packs the content of this object into the given MessagePack buffer.
     *
     * @param packer the buffer to pack into
     * @throws WriteException if any I/O exception occurs
     */
    void packContentTo(MessageBufferPacker packer) throws WriteException;

    /**
     * Unpacks the content of the given MessagePack buffer into this object as a new state.
     *
     * @param unpacker the buffer to unpack from
     * @throws ReadException if any I/O exception occurs
     */
    void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException;

    /**
     * Packs the content of this object into the given ObjectOutput buffer.
     *
     * @param out the buffer to pack into
     * @throws WriteException if any I/O exception occurs
     */
    void writeExternal(ObjectOutput out) throws WriteException;

    /**
     * Unpacks the content of the given ObjectInput buffer into this object as a new state.
     *
     * @param in the buffer to unpack from
     * @throws ReadException if any I/O exception occurs
     */
    void readExternal(ObjectInput in) throws ReadException, ValidationException;

    /**
     * Checks the content of this message, validating the members and class invariants.
     *
     * @throws ValidationException if the message is not valid
     */
    void checkContent() throws ValidationException;
}
