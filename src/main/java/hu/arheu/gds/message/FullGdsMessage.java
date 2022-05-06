package hu.arheu.gds.message;

import hu.arheu.gds.message.data.MessageData;
import hu.arheu.gds.message.data.impl.*;
import hu.arheu.gds.message.errors.ReadException;
import hu.arheu.gds.message.errors.ValidationException;
import hu.arheu.gds.message.errors.WriteException;
import hu.arheu.gds.message.header.MessageHeaderBase;
import hu.arheu.gds.message.header.impl.MessageHeaderBaseImpl;
import hu.arheu.gds.message.util.MessageManager;
import hu.arheu.gds.message.util.ReaderHelper;
import hu.arheu.gds.message.util.WriterHelper;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.Externalizable;
import java.util.Objects;

public class FullGdsMessage extends MessagePart {

    private MessageHeaderBase header;
    private MessageData data;


    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public FullGdsMessage() {
    }

    public FullGdsMessage(MessageHeaderBase header, MessageData data) {
        this.header = header;
        this.data = data;
    }

    public FullGdsMessage(byte[] binary) throws ReadException {
        deserialize(binary);
    }

    @Override
    public int getNumberOfPublicElements() {
        return header.getNumberOfPublicElements() + 1;
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {
        WriterHelper.packArrayHeader(packer, header.getNumberOfPublicElements() + 1);
        header.packContentTo(packer);
        data.packContentTo(packer);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {
        int arraySize = ReaderHelper.unpackArrayHeader(unpacker);
        if (arraySize == MessageHeaderBase.NUMBER_OF_FIELDS + 1) {
            header = new MessageHeaderBaseImpl();
        } else {
            throw new ReadException("Unknown message format with an element count of " + arraySize);
        }
        header.unpackContentFrom(unpacker);

        switch (header.getDataType()) {
            case CONNECTION_0 -> data = new MessageData0ConnectionImpl();
            case CONNECTION_ACK_1 -> data = new MessageData1ConnectionAckImpl();
            case EVENT_2 -> data = new MessageData2EventImpl();
            case EVENT_ACK_3 -> data = new MessageData3EventAckImpl();
            case ATTACHMENT_REQUEST_4 -> data = new MessageData4AttachmentRequestImpl();
            case ATTACHMENT_REQUEST_ACK_5 -> data = new MessageData5AttachmentRequestAckImpl();
            case ATTACHMENT_RESPONSE_6 -> data = new MessageData6AttachmentResponseImpl();
            case ATTACHMENT_RESPONSE_ACK_7 -> data = new MessageData7AttachmentResponseAckImpl();
            case EVENT_DOCUMENT_8 -> data = new MessageData8EventDocumentImpl();
            case EVENT_DOCUMENT_ACK_9 -> data = new MessageData9EventDocumentAckImpl();
            case QUERY_REQUEST_10 -> data = new MessageData10QueryRequestImpl();
            case QUERY_REQUEST_ACK_11 -> data = new MessageData11QueryRequestAckImpl();
            case NEXT_QUERY_PAGE_12 -> data = new MessageData12NextQueryPageImpl();
            default -> throw new ReadException(String.format("%s: Unknown message data type (%s)",
                    MessageManager.class.getSimpleName(),
                    header.getDataType()));
        }
        data.unpackContentFrom(unpacker);
    }

    @Override
    public void checkContent() throws ValidationException {
        header.checkContent();
        data.checkContent();
    }

    @Override
    protected Type getMessagePartType() {
        return Type.FULL;
    }

    public MessageHeaderBase getHeader() {
        return header;
    }

    public MessageData getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FullGdsMessage message = (FullGdsMessage) o;
        return Objects.equals(header, message.header) && Objects.equals(data, message.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, data);
    }
}
