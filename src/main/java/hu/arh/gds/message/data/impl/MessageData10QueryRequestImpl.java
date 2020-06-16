package hu.arh.gds.message.data.impl;

import hu.arh.gds.message.*;
import hu.arh.gds.message.data.MessageData10QueryRequest;
import hu.arh.gds.message.data.ConsistencyType;
import hu.arh.gds.message.data.MessageDataTypeHelper;
import hu.arh.gds.message.header.MessageDataType;
import hu.arh.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.Objects;

public class MessageData10QueryRequestImpl extends MessageData10QueryRequest {
    private String query;
    private ConsistencyType consistencyType;
    private Long timeout;
    private Integer pageSize;
    private Integer queryType;

    public MessageData10QueryRequestImpl(boolean cache,
                                         String query,
                                         ConsistencyType consistencyType,
                                         Long timeout) throws IOException, NullPointerException, ValidationException {

        this(cache, query, consistencyType, timeout, null, null);
    }

    public MessageData10QueryRequestImpl(boolean cache,
                                         String query,
                                         ConsistencyType consistencyType,
                                         Long timeout,
                                         Integer pageSize,
                                         Integer queryType) throws IOException, NullPointerException, ValidationException {
        this.query = query;
        this.consistencyType = consistencyType;
        this.timeout = timeout;
        this.pageSize = pageSize;
        this.queryType = queryType;
        this.cache = cache;
        checkContent();
        if (cache) {
            Serialize();
        }
    }

    protected void checkContent() throws NullPointerException {
        if(pageSize == null) {
            ExceptionHelper.requireNullValue(queryType, this.getClass().getSimpleName(), "queryType");
        }
        if(queryType == null) {
            ExceptionHelper.requireNullValue(pageSize, this.getClass().getSimpleName(), "pageSize");
        }
        ExceptionHelper.requireNonNullValue(query, this.getClass().getSimpleName(),
                "query");
        ExceptionHelper.requireNonNullValue(consistencyType, this.getClass().getSimpleName(),
                "consistencyType");
        ExceptionHelper.requireNonNullValue(timeout, this.getClass().getSimpleName(),
                "timeout");
    }

    public MessageData10QueryRequestImpl(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData10QueryRequestImpl(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    @Override
    protected void init() {
        this.typeHelper = new MessageDataTypeHelper() {
            @Override
            public MessageDataType getMessageDataType() {
                return MessageDataType.QUERY_REQUEST_10;
            }
            @Override
            public MessageData10QueryRequestImpl asQueryRequestMessageData10() {
                return MessageData10QueryRequestImpl.this;
            }
            @Override
            public boolean isQueryRequestMessageData10() {
                return true;
            }
        };
    }

    @Override
    public String getQuery() {
        return this.query;
    }

    @Override
    public ConsistencyType getConsistencyType() {
        return this.consistencyType;
    }

    @Override
    public Long getTimeout() {
        return this.timeout;
    }

    @Override
    public Integer getPageSize() {
        return this.pageSize;
    }

    @Override
    public Integer getQueryType() { return this.queryType; }

    protected MessagePartType getMessagePartType() {
        return MessagePartType.DATA;
    }

    @Override
    protected void PackValues(MessageBufferPacker packer) throws IOException {
        if(pageSize == null && queryType == null) {
            WriterHelper.packArrayHeader(packer, 3);
        } else {
            WriterHelper.packArrayHeader(packer, 5);
        }
        WriterHelper.packValue(packer, this.query);
        WriterHelper.packValue(packer, this.consistencyType.toString());
        WriterHelper.packValue(packer, this.timeout);
        if(pageSize != null && queryType != null) {
            WriterHelper.packValue(packer, this.pageSize);
            WriterHelper.packValue(packer, this.queryType);
        }
    }

    @Override
    protected void UnpackValues(MessageUnpacker unpacker) throws ReadException, IOException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "query request",
                this.getClass().getSimpleName())) {

            int arraySize = ReaderHelper.unpackArrayHeader(unpacker, null, "query request",
                    this.getClass().getSimpleName());

            this.query = ReaderHelper.unpackStringValue(unpacker, "query", this.getClass().getSimpleName());

            this.consistencyType = ReaderHelper.unpackEnumValueAsString(unpacker, ConsistencyType.class,
                    "consistency typte",
                    this.getClass().getSimpleName());

            this.timeout = ReaderHelper.unpackLongValue(unpacker, "timeout", this.getClass().getSimpleName());

            if(arraySize == 5) {
                this.pageSize = ReaderHelper.unpackIntegerValue(unpacker, "pageSize", this.getClass().getSimpleName());
                this.queryType = ReaderHelper.unpackIntegerValue(unpacker, "queryType", this.getClass().getSimpleName());
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
        MessageData10QueryRequestImpl that = (MessageData10QueryRequestImpl) o;
        return Objects.equals(query, that.query) &&
                consistencyType == that.consistencyType &&
                Objects.equals(timeout, that.timeout) &&
                Objects.equals(pageSize, that.pageSize) &&
                Objects.equals(queryType, that.queryType);
    }

    @Override
    public int hashCode() {
        int result = query != null ? query.hashCode() : 0;
        result = 31 * result + (consistencyType != null ? consistencyType.hashCode() : 0);
        result = 31 * result + (timeout != null ? timeout.hashCode() : 0);
        result = 31 * result + (pageSize != null ? pageSize.hashCode() : 0);
        result = 31 * result + (queryType != null ? queryType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MessageData10QueryRequestImpl{" +
                "query='" + query + '\'' +
                ", consistencyType=" + consistencyType +
                ", timeout=" + timeout +
                ", pageSize=" + pageSize +
                ", queryType=" + queryType +
                '}';
    }
}