package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePartType;
import hu.arheu.gds.message.data.AttachmentRequestAckDataHolder;
import hu.arheu.gds.message.data.MessageData5AttachmentRequestAck;
import hu.arheu.gds.message.data.MessageDataTypeHelper;
import hu.arheu.gds.message.header.MessageDataType;
import hu.arheu.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.IOException;

public class MessageData5AttachmentRequestAckImpl extends MessageData5AttachmentRequestAck {
    private AckStatus globalStatus;
    private AttachmentRequestAckDataHolder data;
    private String globalException;

    public MessageData5AttachmentRequestAckImpl(boolean cache,
                                                AckStatus globalStatus,
                                                AttachmentRequestAckDataHolder data,
                                                String globalException) throws IOException, ValidationException {
        this.globalStatus = globalStatus;
        this.data = data;
        this.globalException = globalException;
        this.cache = cache;
        checkContent();
        if (cache) {
            Serialize();
        }
    }

    public MessageData5AttachmentRequestAckImpl(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData5AttachmentRequestAckImpl(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    @Override
    protected void init() {
        this.typeHelper = new MessageDataTypeHelper() {
            @Override
            public MessageDataType getMessageDataType() {
                return MessageDataType.ATTACHMENT_REQUEST_ACK_5;
            }
            @Override
            public MessageData5AttachmentRequestAckImpl asAttachmentRequestAckMessageData5() {
                return MessageData5AttachmentRequestAckImpl.this;
            }
            @Override
            public boolean isAttachmentRequestAckMessageData5() {
                return true;
            }
        };
    }

    @Override
    public AckStatus getGlobalStatus() {
        return this.globalStatus;
    }

    @Override
    public AttachmentRequestAckDataHolder getData() {
        return this.data;
    }

    @Override
    public String getGlobalException() {
        return this.globalException;
    }

    protected MessagePartType getMessagePartType() {
        return MessagePartType.DATA;
    }

    @Override
    protected void checkContent() {
        ExceptionHelper.requireNonNullValue(this.globalStatus, this.getClass().getSimpleName(),
                "globalStatus");
    }

    @Override
    protected void PackValues(MessageBufferPacker packer) throws IOException, ValidationException {

        WriterHelper.packArrayHeader(packer, 3);
        WriterHelper.packValue(packer, this.globalStatus.getValue());
        WriterHelper.packPackable(packer, this.data);
        WriterHelper.packValue(packer, this.globalException);
    }

    @Override
    protected void UnpackValues(MessageUnpacker unpacker) throws ReadException, IOException, ValidationException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "attachment request ack data",
                this.getClass().getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, 3, "attachment request ack data",
                    this.getClass().getSimpleName());
            this.globalStatus = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "global status",
                    this.getClass().getSimpleName()));
            this.data = AttachmentRequestAckDataHolderImpl.unpackConent(unpacker, AttachmentResultHolderType.ATTACHMENT_REQUEST_ACK);
            this.globalException = ReaderHelper.unpackStringValue(unpacker, "global exception",
                    this.getClass().getSimpleName());
        } else {
            unpacker.unpackNil();
        }
        checkContent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageData5AttachmentRequestAckImpl that = (MessageData5AttachmentRequestAckImpl) o;
        if (globalStatus != that.globalStatus) return false;
        if (data != null ? !data.equals(that.data) : that.data != null) return false;
        return globalException != null ? globalException.equals(that.globalException) : that.globalException == null;
    }

    @Override
    public int hashCode() {
        int result = globalStatus != null ? globalStatus.hashCode() : 0;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (globalException != null ? globalException.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MessageData5AttachmentRequestAckImpl{" +
                "globalStatus=" + globalStatus +
                ", data=" + data +
                ", globalException='" + globalException + '\'' +
                '}';
    }
}
