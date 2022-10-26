package hu.arheu.gds.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hu.arheu.gds.message.errors.ReadException;
import hu.arheu.gds.message.errors.ValidationException;
import hu.arheu.gds.message.errors.WriteException;
import hu.arheu.gds.message.util.GdsMessagePart;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;


public abstract class MessagePart implements GdsMessagePart {
    public enum Type {
        /**
         * Message Header types
         */
        HEADER,
        /**
         * Message Data types
         */
        DATA,
        /**
         * Other, mostly nested types
         */
        OTHER,
        /**
         * Full message with header and data
         */
        FULL
    }

    private byte[] binary;

    @Override
    @JsonIgnore
    public final byte[] getBinary() {
        if (this.binary == null) {
            return serialize();
        }
        return this.binary;
    }

    @Override
    public final int getMessageSize() {
        return getBinary().length;
    }

    @Override
    public abstract void packContentTo(MessageBufferPacker packer) throws WriteException;

    @Override
    public abstract void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException;

    @Override
    public abstract void checkContent() throws ValidationException;

    protected abstract Type getMessagePartType();

    protected final void deserialize(byte[] binary) throws ReadException, ValidationException {
        deserialize(binary, false);
    }

    protected final void deserialize(byte[] binary, boolean cacheBinary) throws ReadException, ValidationException {
        try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(binary)) {
            int start = (int) unpacker.getTotalReadBytes();
            unpackContentFrom(unpacker);
            int end = (int) unpacker.getTotalReadBytes();
            if (cacheBinary) {
                this.binary = Arrays.copyOfRange(binary, start, end);
            } else {
                this.binary = null;
            }
        } catch (IOException exc) {
            throw new ReadException("Could not deserialize the message!", exc);
        }
    }


    protected final byte[] serialize() {
        try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packContentTo(packer);
            this.binary = packer.toByteArray();
        } catch (IOException exc) {
            throw new IllegalStateException("Unexpected error during serialization!", exc);
        }
        return this.binary;
    }

    @Override
    public final void readExternal(ObjectInput in) throws ReadException {
        try {
            byte[] binary = new byte[in.readInt()];
            in.readFully(binary);
            deserialize(binary, false);
        } catch (IOException | ValidationException ex) {
            throw new ReadException("Could not read object from ObjectInput!", ex);
        }
    }

    @Override
    public final void writeExternal(ObjectOutput out) throws WriteException {
        try {
            byte[] binary = getBinary();
            out.writeInt(binary.length);
            out.write(binary);
        } catch (IOException ex) {
            throw new WriteException("Could not write object to ObjectOutput!", ex);
        }
    }
}
