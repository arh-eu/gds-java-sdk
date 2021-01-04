package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePartType;
import hu.arheu.gds.message.data.MessageData12NextQueryPage;
import hu.arheu.gds.message.data.MessageDataTypeHelper;
import hu.arheu.gds.message.data.QueryContextHolder;
import hu.arheu.gds.message.data.QueryContextHolderSerializable;
import hu.arheu.gds.message.header.MessageDataType;
import hu.arheu.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageData12NextQueryPageImpl extends MessageData12NextQueryPage {
    private static final int NUMBER_OF_PUBLIC_ELEMENTS = 2;
    private QueryContextHolderSerializable queryContextHolderSerializable;
    private QueryContextHolder queryContextDescriptor;
    private Long timeout;

    public MessageData12NextQueryPageImpl(boolean cache,
                                          QueryContextHolder queryContextDescriptor,
                                          Long timeout) throws IOException, NullPointerException, ValidationException {
        this.queryContextDescriptor = queryContextDescriptor;
        this.timeout = timeout;
        this.cache = cache;
        checkContent();
        if (cache) {
            Serialize();
        }
    }

    public MessageData12NextQueryPageImpl(boolean cache,
                                          QueryContextHolderSerializable queryContextDescriptorSerializable,
                                          Long timeout) throws IOException, NullPointerException, ValidationException {
        this.queryContextHolderSerializable = queryContextDescriptorSerializable;
        List<Value> fieldValues = new ArrayList<>();
        for (Object value : queryContextDescriptorSerializable.getFieldValues()) {
            try {
                fieldValues.add(Converters.convertToMessagePackValue(value));
            } catch (Exception e) {
                throw new ValidationException(e.getMessage() + ". " + value);
            }
        }
        this.queryContextDescriptor = new QueryContextHolderImpl(queryContextDescriptorSerializable.getScrollId(),
                queryContextDescriptorSerializable.getQuery(), queryContextDescriptorSerializable.getDeliveredNumberOfHits(),
                queryContextDescriptorSerializable.getQueryStartTime(), queryContextDescriptorSerializable.getConsistencyType(),
                queryContextDescriptorSerializable.getLastBucketId(),
                new GDSHolderImpl(queryContextDescriptorSerializable.getClusterName(), queryContextDescriptorSerializable.getGDSNodeName()),
                fieldValues, queryContextDescriptorSerializable.getPartitionNames());
        this.timeout = timeout;
        this.cache = cache;
        checkContent();
        if (cache) {
            Serialize();
        }
    }


    protected void checkContent() throws NullPointerException {
        ExceptionHelper.requireNonNullValue(queryContextDescriptor, this.getClass().getSimpleName(),
                "queryContextDescriptor");
        ExceptionHelper.requireNonNullValue(timeout, this.getClass().getSimpleName(),
                "timeout");
    }

    public MessageData12NextQueryPageImpl(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData12NextQueryPageImpl(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    @Override
    protected void init() {
        this.typeHelper = new MessageDataTypeHelper() {
            @Override
            public MessageDataType getMessageDataType() {
                return MessageDataType.NEXT_QUERY_PAGE_12;
            }

            @Override
            public MessageData12NextQueryPageImpl asNextQueryPageMessageData12() {
                return MessageData12NextQueryPageImpl.this;
            }

            @Override
            public boolean isNextQueryPageMessageData12() {
                return true;
            }
        };
    }

    @Override
    public QueryContextHolder getQueryContextDescriptor() {
        return this.queryContextDescriptor;
    }

    @Override
    public QueryContextHolderSerializable getQueryContextDescriptorSerializable() throws Exception {
        if (queryContextHolderSerializable == null) {
            queryContextHolderSerializable = Converters
                    .getQueryContextDescriptorSerializable(queryContextDescriptor);
        }
        return queryContextHolderSerializable;
    }

    @Override
    public Long getTimeout() {
        return this.timeout;
    }

    protected MessagePartType getMessagePartType() {
        return MessagePartType.DATA;
    }

    @Override
    protected void PackValues(MessageBufferPacker packer) throws IOException, ValidationException {
        WriterHelper.packArrayHeader(packer, this.getNumberOfPublicElements());
        WriterHelper.packPackable(packer, this.queryContextDescriptor);
        WriterHelper.packValue(packer, timeout);
    }

    @Override
    protected void UnpackValues(MessageUnpacker unpacker) throws ReadException, IOException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "next query page",
                this.getClass().getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, this.getNumberOfPublicElements(), "next query page",
                    this.getClass().getSimpleName());

            this.queryContextDescriptor = QueryContextHolderImpl.unpackContent(unpacker);

            this.timeout = ReaderHelper.unpackLongValue(unpacker, "timeout", this.getClass().getSimpleName());
        } else {
            unpacker.unpackNil();
        }
        checkContent();
    }

    @Override
    public int getNumberOfPublicElements() {
        return NUMBER_OF_PUBLIC_ELEMENTS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageData12NextQueryPageImpl that = (MessageData12NextQueryPageImpl) o;
        if (queryContextDescriptor != null ? !queryContextDescriptor.equals(that.queryContextDescriptor) : that.queryContextDescriptor != null)
            return false;
        return timeout != null ? timeout.equals(that.timeout) : that.timeout == null;
    }

    @Override
    public int hashCode() {
        int result = queryContextDescriptor != null ? queryContextDescriptor.hashCode() : 0;
        result = 31 * result + (timeout != null ? timeout.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MessageData12NextQueryPageImpl{" +
                "queryContextHolderSerializable=" + queryContextHolderSerializable +
                ", queryContextDescriptor=" + queryContextDescriptor +
                ", timeout=" + timeout +
                '}';
    }
}
