package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePartType;
import hu.arheu.gds.message.data.MessageData11QueryRequestAck;
import hu.arheu.gds.message.data.MessageDataTypeHelper;
import hu.arheu.gds.message.data.QueryResponseHolder;
import hu.arheu.gds.message.header.MessageDataType;
import hu.arheu.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.IOException;

public class MessageData11QueryRequestAckImpl extends MessageData11QueryRequestAck {
    private static final int NUMBER_OF_PUBLIC_ELEMENTS = 3;

    private AckStatus globalStatus;
    private QueryResponseHolder queryResponseHolder;
    private String globalException;

    public MessageData11QueryRequestAckImpl(boolean cache,
                                            AckStatus globalStatus,
                                            QueryResponseHolder queryResponseHolder,
                                            String globalException) throws IOException, NullPointerException, ValidationException {
        this.globalStatus = globalStatus;
        this.queryResponseHolder = queryResponseHolder;
        this.globalException = globalException;
        checkContent();
        if (cache) {
            Serialize();
        }

    }

    protected void checkContent() throws NullPointerException {
        ExceptionHelper.requireNonNullValue(this.globalStatus, this.getClass().getSimpleName(),
                "globalStatus");
    }

    public MessageData11QueryRequestAckImpl(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData11QueryRequestAckImpl(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    @Override
    protected void init() {
        this.typeHelper = new MessageDataTypeHelper() {
            @Override
            public MessageDataType getMessageDataType() {
                return MessageDataType.QUERY_REQUEST_ACK_11;
            }
            @Override
            public MessageData11QueryRequestAckImpl asQueryRequestAckMessageData11() {
                return MessageData11QueryRequestAckImpl.this;
            }
            @Override
            public boolean isQueryRequestAckMessageData11() {
                return true;
            }
        };
    }

    protected MessagePartType getMessagePartType() {
        return MessagePartType.DATA;
    }

    @Override
    protected void PackValues(MessageBufferPacker packer) throws IOException, ValidationException {
        WriterHelper.packArrayHeader(packer, NUMBER_OF_PUBLIC_ELEMENTS);
        WriterHelper.packValue(packer, this.globalStatus == null ? null : this.globalStatus.getValue());
        WriterHelper.packPackable(packer, this.queryResponseHolder);
        WriterHelper.packValue(packer, this.globalException);
    }

    @Override
    protected void UnpackValues(MessageUnpacker unpacker) throws ReadException, IOException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "query response",
                this.getClass().getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, NUMBER_OF_PUBLIC_ELEMENTS, "query response",
                    this.getClass().getSimpleName());

            this.globalStatus = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "global status",
                    this.getClass().getSimpleName()));

            this.queryResponseHolder = QueryResponseHolderImpl.unpackContent(unpacker);

            this.globalException = ReaderHelper.unpackStringValue(unpacker, "global exception",
                    this.getClass().getSimpleName());
            checkContent();
        } else {
            unpacker.unpackNil();
        }
    }

    @Override
    public int getNumberOfPublicElements() {
        return NUMBER_OF_PUBLIC_ELEMENTS;
    }

    @Override
    public QueryResponseHolder getQueryResponseHolder() {
        return this.queryResponseHolder;
    }

    @Override
    public AckStatus getGlobalStatus() {
        return this.globalStatus;
    }

    @Override
    public String getGlobalException() {
        return this.globalException;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageData11QueryRequestAckImpl that = (MessageData11QueryRequestAckImpl) o;
        if (globalStatus != that.globalStatus) return false;
        if (queryResponseHolder != null ? !queryResponseHolder.equals(that.queryResponseHolder) : that.queryResponseHolder != null)
            return false;
        return globalException != null ? globalException.equals(that.globalException) : that.globalException == null;
    }

    @Override
    public int hashCode() {
        int result = globalStatus != null ? globalStatus.hashCode() : 0;
        result = 31 * result + (queryResponseHolder != null ? queryResponseHolder.hashCode() : 0);
        result = 31 * result + (globalException != null ? globalException.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MessageData11QueryRequestAckImpl{" +
                "globalStatus=" + globalStatus +
                ", queryResponseHolder=" + queryResponseHolder +
                ", globalException='" + globalException + '\'' +
                '}';
    }
}

