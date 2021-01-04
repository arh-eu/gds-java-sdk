package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePartType;
import hu.arheu.gds.message.data.EventDocumentResultHolder;
import hu.arheu.gds.message.data.MessageData9EventDocumentAck;
import hu.arheu.gds.message.data.MessageDataTypeHelper;
import hu.arheu.gds.message.header.MessageDataType;
import hu.arheu.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageData9EventDocumentAckImpl extends MessageData9EventDocumentAck {
    private AckStatus globalStatus;
    private List<EventDocumentResultHolder> result;
    private String globalException;

    public MessageData9EventDocumentAckImpl(boolean cache,
                                            AckStatus globalStatus,
                                            List<EventDocumentResultHolder> result,
                                            String globalException) throws IOException, ValidationException {
        this.globalStatus = globalStatus;
        this.result = result;
        this.globalException = globalException;
        this.cache = cache;
        checkContent();
        if (cache) {
            Serialize();
        }
    }

    public MessageData9EventDocumentAckImpl(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData9EventDocumentAckImpl(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    @Override
    protected void init() {
        this.typeHelper = new MessageDataTypeHelper() {
            @Override
            public MessageDataType getMessageDataType() {
                return MessageDataType.EVENT_DOCUMENT_ACK_9;
            }
            @Override
            public MessageData9EventDocumentAckImpl asEventDocumentAckMessageData9() {
                return MessageData9EventDocumentAckImpl.this;
            }
            @Override
            public boolean isEventDocumentAckMessageData9() {
                return true;
            }
        };
    }

    @Override
    public AckStatus getGlobalStatus() {
        return this.globalStatus;
    }

    @Override
    public List<EventDocumentResultHolder> getResults() {
        return this.result;
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
        ExceptionHelper.requireNonEmptyCollection(this.result, this.getClass().getSimpleName(),
                "result");
    }

    @Override
    protected void PackValues(MessageBufferPacker packer) throws IOException, ValidationException {
        WriterHelper.packArrayHeader(packer, 3);
        WriterHelper.packValue(packer, this.globalStatus == null ? null : this.globalStatus.getValue());
        if (this.result != null) {
            WriterHelper.packArrayHeader(packer, this.result.size());
            for(EventDocumentResultHolder eventDocumentResultHolder: this.result) {
                eventDocumentResultHolder.packContent(packer);
            }
        } else {
            packer.packNil();
        }
        WriterHelper.packValue(packer, this.globalException);
    }

    @Override
    protected void UnpackValues(MessageUnpacker unpacker) throws ReadException, IOException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "event document ack data",
                this.getClass().getSimpleName())) {
            ReaderHelper.unpackArrayHeader(unpacker, 3, "event document ack data",
                    this.getClass().getSimpleName());
            this.globalStatus = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "global status",
                    this.getClass().getSimpleName()));
            if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "result",
                    this.getClass().getSimpleName())) {
                int arrayHeaderSize = ReaderHelper.unpackArrayHeader(unpacker, null, "result",
                        this.getClass().getSimpleName());
                this.result = new ArrayList<>();
                for (int i = 0; i < arrayHeaderSize; ++i) {
                    this.result.add(EventDocumentResultHolderImpl.unpackContent(unpacker));
                }
            } else {
                unpacker.unpackNil();
            }
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
        MessageData9EventDocumentAckImpl that = (MessageData9EventDocumentAckImpl) o;
        if (globalStatus != that.globalStatus) return false;
        if (result != null ? !result.equals(that.result) : that.result != null) return false;
        return globalException != null ? globalException.equals(that.globalException) : that.globalException == null;
    }

    @Override
    public int hashCode() {
        int result1 = globalStatus != null ? globalStatus.hashCode() : 0;
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        result1 = 31 * result1 + (globalException != null ? globalException.hashCode() : 0);
        return result1;
    }

    @Override
    public String toString() {
        return "MessageData9EventDocumentAckImpl{" +
                "globalStatus=" + globalStatus +
                ", result=" + result +
                ", globalException='" + globalException + '\'' +
                '}';
    }
}
