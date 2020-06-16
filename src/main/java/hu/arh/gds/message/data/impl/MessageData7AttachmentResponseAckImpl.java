package hu.arh.gds.message.data.impl;

import hu.arh.gds.message.MessagePartType;
import hu.arh.gds.message.data.AttachmentResponseAckResultHolder;
import hu.arh.gds.message.data.MessageData7AttachmentResponseAck;
import hu.arh.gds.message.data.MessageDataTypeHelper;
import hu.arh.gds.message.header.MessageDataType;
import hu.arh.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.Objects;

public class MessageData7AttachmentResponseAckImpl extends MessageData7AttachmentResponseAck {
    private AckStatus globalStatus;
    AttachmentResponseAckResultHolder data;
    private String globalException;

    public MessageData7AttachmentResponseAckImpl(boolean cache,
                                                 AckStatus globalStatus,
                                                 AttachmentResponseAckResultHolder data,
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

    public MessageData7AttachmentResponseAckImpl(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData7AttachmentResponseAckImpl(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    @Override
    protected void init() {
        this.typeHelper = new MessageDataTypeHelper() {
            @Override
            public MessageDataType getMessageDataType() {
                return MessageDataType.ATTACHMENT_RESPONSE_ACK_7;
            }

            @Override
            public MessageData7AttachmentResponseAckImpl asAttachmentResponseAckMessageData7() {
                return MessageData7AttachmentResponseAckImpl.this;
            }

            @Override
            public boolean isAttachmentResponseAckMessageData7() {
                return true;
            }
        };
    }

    @Override
    public AckStatus getGlobalStatus() {
        return this.globalStatus;
    }

    public AttachmentResponseAckResultHolder getData() {
        return this.data;
    }

    @Override
    public String getGlobalException() {
        return this.globalException;
    }

    protected MessagePartType getMessagePartType() {
        return MessagePartType.DATA;
    }

    public boolean isAttachmentResponseAckMessageData7() {
        return true;
    }

    @Override
    protected void checkContent() {
        ExceptionHelper.requireNonNullValue(this.globalStatus, this.getClass().getSimpleName(),
                "globalStatus");
    }

    @Override
    protected void PackValues(MessageBufferPacker packer) throws IOException, ValidationException {
        WriterHelper.packArrayHeader(packer, 3);
        WriterHelper.packValue(packer, this.globalStatus == null ? null : this.globalStatus.getValue());
        WriterHelper.packPackable(packer, this.data);
        WriterHelper.packValue(packer, this.globalException);
    }

    @Override
    protected void UnpackValues(MessageUnpacker unpacker) throws ReadException, IOException, ValidationException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "attachment response ack data",
                this.getClass().getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, 3, "attachment response ack data",
                    this.getClass().getSimpleName());
            this.globalStatus = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "global status",
                    this.getClass().getSimpleName()));
            this.data = AttachmentResponseAckResultHolderImpl.unpackContent(unpacker);
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
        if (!(o instanceof MessageData7AttachmentResponseAckImpl)) return false;
        MessageData7AttachmentResponseAckImpl that = (MessageData7AttachmentResponseAckImpl) o;
        return globalStatus == that.globalStatus &&
                Objects.equals(data, that.data) &&
                Objects.equals(globalException, that.globalException);
    }

    @Override
    public int hashCode() {
        return Objects.hash(globalStatus, data, globalException);
    }

    @Override
    public String toString() {
        return "MessageData7AttachmentResponseAckImpl{" +
                "globalStatus=" + globalStatus +
                ", data=" + data +
                ", globalException='" + globalException + '\'' +
                '}';
    }
}
