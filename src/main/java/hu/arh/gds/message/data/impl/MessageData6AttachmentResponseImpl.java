package hu.arh.gds.message.data.impl;

import hu.arh.gds.message.MessagePartType;
import hu.arh.gds.message.data.AttachmentResultHolder;
import hu.arh.gds.message.data.EventHolder;
import hu.arh.gds.message.data.MessageData6AttachmentResponse;
import hu.arh.gds.message.data.MessageDataTypeHelper;
import hu.arh.gds.message.header.MessageDataType;
import hu.arh.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.Objects;

public class MessageData6AttachmentResponseImpl extends MessageData6AttachmentResponse {
    private AttachmentResultHolder result;
    private EventHolder eventHolder;

    public MessageData6AttachmentResponseImpl(boolean cache,
                                              AttachmentResultHolder result,
                                              EventHolder eventHolder) throws IOException, ValidationException {
        this.result = result;
        this.eventHolder = eventHolder;
        this.cache = cache;
        checkContent();
        if (cache) {
            Serialize();
        }
    }

    public MessageData6AttachmentResponseImpl(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData6AttachmentResponseImpl(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    @Override
    protected void init() {
        this.typeHelper = new MessageDataTypeHelper() {
            @Override
            public MessageDataType getMessageDataType() {
                return MessageDataType.ATTACHMENT_RESPONSE_6;
            }

            @Override
            public MessageData6AttachmentResponseImpl asAttachmentResponseMessageData6() {
                return MessageData6AttachmentResponseImpl.this;
            }
            @Override
            public boolean isAttachmentResponseMessageData6() {
                return true;
            }
        };
    }

    @Override
    public AttachmentResultHolder getResult() {
        return this.result;
    }

    @Override
    public EventHolder getEventHolder() {
        return this.eventHolder;
    }

    protected MessagePartType getMessagePartType() {
        return MessagePartType.DATA;
    }

    @Override
    protected void checkContent() {
        ExceptionHelper.requireNonNullValue(this.result, this.getClass().getSimpleName(), "result");
    }

    @Override
    protected void PackValues(MessageBufferPacker packer) throws IOException, ValidationException {

        WriterHelper.packArrayHeader(packer, 2);
        WriterHelper.packPackable(packer, this.result);
        WriterHelper.packPackable(packer, this.eventHolder);
    }

    @Override
    protected void UnpackValues(MessageUnpacker unpacker) throws ReadException, IOException, ValidationException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "attachment response data",
                this.getClass().getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, null, "attachemnt respose data",
                    this.getClass().getSimpleName());

            this.result = AttachmentResultHolderImpl.unpackContent(unpacker, AttachmentResultHolderType.ATTACHMENT_RESPONSE);
            if(this.eventHolder != null) {
                this.eventHolder = EventHolderImpl.unpackContent(unpacker);
            }
        } else {
            unpacker.unpackNil();
        }
        checkContent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageData6AttachmentResponseImpl that = (MessageData6AttachmentResponseImpl) o;
        return Objects.equals(result, that.result) &&
                Objects.equals(eventHolder, that.eventHolder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, eventHolder);
    }
}
