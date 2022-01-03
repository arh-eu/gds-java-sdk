
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.ConsistencyType;
import hu.arheu.gds.message.data.MessageData10QueryRequest;
import hu.arheu.gds.message.errors.ReadException;
import hu.arheu.gds.message.errors.ValidationException;
import hu.arheu.gds.message.errors.WriteException;
import hu.arheu.gds.message.util.ReaderHelper;
import hu.arheu.gds.message.util.Validator;
import hu.arheu.gds.message.util.WriterHelper;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.Externalizable;
import java.util.Objects;


public class MessageData10QueryRequestImpl extends MessagePart implements MessageData10QueryRequest {

    private String query;
    private ConsistencyType consistencyType;
    private Long timeout;
    private Integer pageSize;
    private Integer queryType;

    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public MessageData10QueryRequestImpl() {
    }

    public MessageData10QueryRequestImpl(String query,
                                         ConsistencyType consistencyType,
                                         Long timeout) throws ValidationException {

        this(query, consistencyType, timeout, null, null);
    }

    public MessageData10QueryRequestImpl(String query,
                                         ConsistencyType consistencyType,
                                         Long timeout,
                                         Integer pageSize,
                                         Integer queryType) throws ValidationException {

        this.query = query;
        this.consistencyType = consistencyType;
        this.timeout = timeout;
        this.pageSize = pageSize;
        this.queryType = queryType;

        checkContent();
    }

    public MessageData10QueryRequestImpl(byte[] binary) throws ReadException, ValidationException {
        deserialize(binary);
    }

    public MessageData10QueryRequestImpl(byte[] binary, boolean isFullMessage) throws ReadException, ValidationException {
        deserialize(binary, isFullMessage);
    }

    public void checkContent() throws ValidationException {

        if (pageSize == null) {
            Validator.requireNullValue(queryType, this.getClass().getSimpleName(), "queryType");
        }

        if (queryType == null) {
            Validator.requireNullValue(pageSize, this.getClass().getSimpleName(), "pageSize");
        }

        Validator.requireNonNullValue(query, this.getClass().getSimpleName(),
                "query");
        Validator.requireNonNullValue(consistencyType, this.getClass().getSimpleName(),
                "consistencyType");
        Validator.requireNonNullValue(timeout, this.getClass().getSimpleName(),
                "timeout");
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
    public Integer getQueryType() {
        return this.queryType;
    }

    protected Type getMessagePartType() {
        return Type.DATA;
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {


        if (pageSize == null && queryType == null) {
            WriterHelper.packArrayHeader(packer, 3);
        } else {
            WriterHelper.packArrayHeader(packer, 5);
        }

        WriterHelper.packValue(packer, this.query);
        WriterHelper.packValue(packer, this.consistencyType.toString());
        WriterHelper.packValue(packer, this.timeout);

        if (pageSize != null && queryType != null) {
            WriterHelper.packValue(packer, this.pageSize);
            WriterHelper.packValue(packer, this.queryType);
        }
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "query request",
                this.getClass().getSimpleName())) {

            int arraySize = ReaderHelper.unpackArrayHeader(unpacker, null, "query request",
                    this.getClass().getSimpleName());

            this.query = ReaderHelper.unpackStringValue(unpacker, "query", this.getClass().getSimpleName());

            this.consistencyType = ReaderHelper.unpackEnumValueAsString(unpacker, ConsistencyType.class,
                    "consistency typte",
                    this.getClass().getSimpleName());

            this.timeout = ReaderHelper.unpackLongValue(unpacker, "timeout", this.getClass().getSimpleName());

            if (arraySize == 5) {
                this.pageSize = ReaderHelper.unpackIntegerValue(unpacker, "pageSize", this.getClass().getSimpleName());
                this.queryType = ReaderHelper.unpackIntegerValue(unpacker, "queryType", this.getClass().getSimpleName());
            }

        } else {
            ReaderHelper.unpackNil(unpacker);
        }
        checkContent();
    }

    @Override
    public String toString() {
        return "MessageData10QueryRequestImpl{" +
                "" + "query=" + query + ", consistencyType=" + consistencyType + ", timeout=" + timeout + ", pageSize=" + pageSize + ", queryType=" + queryType + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageData10QueryRequestImpl that = (MessageData10QueryRequestImpl) o;
        return Objects.equals(query, that.query)
                && consistencyType == that.consistencyType
                && Objects.equals(timeout, that.timeout)
                && Objects.equals(pageSize, that.pageSize)
                && Objects.equals(queryType, that.queryType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, consistencyType, timeout, pageSize, queryType);
    }
}