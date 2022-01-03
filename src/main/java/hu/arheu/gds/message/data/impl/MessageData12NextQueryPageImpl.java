
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.MessageData12NextQueryPage;
import hu.arheu.gds.message.data.QueryContextHolder;
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


public class MessageData12NextQueryPageImpl extends MessagePart implements MessageData12NextQueryPage {

    private QueryContextHolder queryContextDescriptor;
    private Long timeout;

    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public MessageData12NextQueryPageImpl() {
    }

    public MessageData12NextQueryPageImpl(QueryContextHolder queryContextDescriptor,
                                          Long timeout) throws ValidationException {

        this.queryContextDescriptor = queryContextDescriptor;
        this.timeout = timeout;

        checkContent();
    }


    public MessageData12NextQueryPageImpl(byte[] binary) throws ReadException, ValidationException {
        deserialize(binary);
    }

    public MessageData12NextQueryPageImpl(byte[] binary, boolean isFullMessage) throws ReadException, ValidationException {
        deserialize(binary, isFullMessage);
    }

    public void checkContent() throws ValidationException {

        Validator.requireNonNullValue(queryContextDescriptor, this.getClass().getSimpleName(),
                "queryContextDescriptor");
        Validator.requireNonNullValue(timeout, this.getClass().getSimpleName(),
                "timeout");
    }

    @Override
    public QueryContextHolder getQueryContextHolder() {
        return this.queryContextDescriptor;
    }

    @Override
    public Long getTimeout() {
        return this.timeout;
    }

    protected Type getMessagePartType() {
        return Type.DATA;
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {

        WriterHelper.packArrayHeader(packer, this.getNumberOfPublicElements());
        WriterHelper.packMessagePart(packer, this.queryContextDescriptor);
        WriterHelper.packValue(packer, timeout);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "next query page",
                this.getClass().getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, this.getNumberOfPublicElements(), "next query page",
                    this.getClass().getSimpleName());

            this.queryContextDescriptor = new QueryContextHolderImpl();
            this.queryContextDescriptor.unpackContentFrom(unpacker);

            this.timeout = ReaderHelper.unpackLongValue(unpacker, "timeout", this.getClass().getSimpleName());
        } else {
            ReaderHelper.unpackNil(unpacker);
        }
        checkContent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageData12NextQueryPageImpl that = (MessageData12NextQueryPageImpl) o;

        if (!Objects.equals(queryContextDescriptor, that.queryContextDescriptor))
            return false;
        return Objects.equals(timeout, that.timeout);
    }

    @Override
    public int hashCode() {
        int result = queryContextDescriptor != null ? queryContextDescriptor.hashCode() : 0;
        result = 31 * result + (timeout != null ? timeout.hashCode() : 0);
        return result;
    }
}
