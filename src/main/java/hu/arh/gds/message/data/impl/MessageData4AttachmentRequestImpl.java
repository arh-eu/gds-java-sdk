package hu.arh.gds.message.data.impl;

import hu.arh.gds.message.MessagePartType;
import hu.arh.gds.message.data.MessageData4AttachmentRequest;
import hu.arh.gds.message.data.MessageDataTypeHelper;
import hu.arh.gds.message.header.MessageDataType;
import hu.arh.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class MessageData4AttachmentRequestImpl extends MessageData4AttachmentRequest {
    private String request;

    public MessageData4AttachmentRequestImpl(boolean cache,
                                             String request) throws IOException, ValidationException {
        this.request = request;
        this.cache = cache;
        checkContent();
        if (cache) {
            Serialize();
        }
    }

    public MessageData4AttachmentRequestImpl(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData4AttachmentRequestImpl(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }


    @Override
    protected void init() {
        this.typeHelper = new MessageDataTypeHelper() {
            @Override
            public MessageDataType getMessageDataType() {
                return MessageDataType.ATTACHMENT_REQUEST_4;
            }
            @Override
            public MessageData4AttachmentRequestImpl asAttachmentRequestMessageData4() {
                return MessageData4AttachmentRequestImpl.this;
            }
            @Override
            public boolean isAttachmentRequestMessageData4() {
                return true;
            }
        };
    }

    @Override
    public String getRequest() {
        return this.request;
    }

    protected MessagePartType getMessagePartType() {
        return MessagePartType.DATA;
    }

    public boolean isAttachmentRequestMessageData4() {
        return true;
    }

    @Override
    protected void checkContent() {
        ExceptionHelper.requireNonNullValue(this.request, this.getClass().getSimpleName(), "request");
    }

    @Override
    protected void PackValues(MessageBufferPacker packer) throws IOException {
        WriterHelper.packValue(packer, this.request);
    }

    @Override
    protected void UnpackValues(MessageUnpacker unpacker) throws ReadException, IOException {
        this.request = ReaderHelper.unpackStringValue(unpacker, "attachment request",
                this.getClass().getSimpleName());

        checkContent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageData4AttachmentRequestImpl that = (MessageData4AttachmentRequestImpl) o;
        return request != null ? request.equals(that.request) : that.request == null;
    }

    @Override
    public int hashCode() {
        return request != null ? request.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "MessageData4AttachmentRequestImpl{" +
                "request='" + request + '\'' +
                '}';
    }
}
