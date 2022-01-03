
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.MessageData4AttachmentRequest;
import hu.arheu.gds.message.errors.ReadException;
import hu.arheu.gds.message.errors.ValidationException;
import hu.arheu.gds.message.errors.WriteException;
import hu.arheu.gds.message.util.ReaderHelper;
import hu.arheu.gds.message.util.Validator;
import hu.arheu.gds.message.util.WriterHelper;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.Externalizable;
import java.util.Objects;


public class MessageData4AttachmentRequestImpl extends MessagePart implements MessageData4AttachmentRequest {

    private String request;

    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public MessageData4AttachmentRequestImpl() {
    }

    public MessageData4AttachmentRequestImpl(String request) throws ValidationException {

        this.request = request;
        checkContent();
    }

    public MessageData4AttachmentRequestImpl(byte[] binary) throws ReadException, ValidationException {
        deserialize(binary);
    }

    public MessageData4AttachmentRequestImpl(byte[] binary, boolean isFullMessage) throws ReadException, ValidationException {
        deserialize(binary, isFullMessage);
    }

    @Override
    public String getRequest() {
        return this.request;
    }

    protected Type getMessagePartType() {
        return Type.DATA;
    }

    public boolean isAttachmentRequestMessageData4() {
        return true;
    }

    @Override
    public void checkContent() throws ValidationException {
        Validator.requireNonNullValue(this.request, this.getClass().getSimpleName(), "request");
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {
        WriterHelper.packValue(packer, this.request);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {
        this.request = ReaderHelper.unpackStringValue(unpacker, "attachment request",
                this.getClass().getSimpleName());

        checkContent();
    }

    @Override
    public String toString() {
        return "MessageData4AttachmentRequestImpl{" +
                "request='" + request + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageData4AttachmentRequestImpl that = (MessageData4AttachmentRequestImpl) o;

        return Objects.equals(request, that.request);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(request);
    }
}
