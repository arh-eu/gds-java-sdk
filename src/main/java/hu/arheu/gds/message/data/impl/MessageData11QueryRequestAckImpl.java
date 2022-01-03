
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.MessageData11QueryRequestAck;
import hu.arheu.gds.message.data.QueryResponseHolder;
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


public class MessageData11QueryRequestAckImpl extends MessagePart implements MessageData11QueryRequestAck {

    private AckStatus globalStatus;
    private QueryResponseHolder queryResponseHolder;
    private String globalException;


    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public MessageData11QueryRequestAckImpl() {
    }

    public MessageData11QueryRequestAckImpl(AckStatus globalStatus,
                                            QueryResponseHolder queryResponseHolder,
                                            String globalException) throws ValidationException {

        this.globalStatus = globalStatus;
        this.queryResponseHolder = queryResponseHolder;
        this.globalException = globalException;
        checkContent();
    }


    public void checkContent() throws ValidationException {

        Validator.requireNonNullValue(this.globalStatus, this.getClass().getSimpleName(),
                "globalStatus");
    }

    public MessageData11QueryRequestAckImpl(byte[] binary) throws ReadException, ValidationException {
        deserialize(binary);
    }

    public MessageData11QueryRequestAckImpl(byte[] binary, boolean isFullMessage) throws ReadException, ValidationException {
        deserialize(binary, isFullMessage);
    }

    protected Type getMessagePartType() {
        return Type.DATA;
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {

        WriterHelper.packArrayHeader(packer, getNumberOfPublicElements());
        WriterHelper.packValue(packer, this.globalStatus == null ? null : this.globalStatus.getValue());
        WriterHelper.packMessagePart(packer, this.queryResponseHolder);
        WriterHelper.packValue(packer, this.globalException);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "query response",
                this.getClass().getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, getNumberOfPublicElements(), "query response",
                    this.getClass().getSimpleName());

            this.globalStatus = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "global status",
                    this.getClass().getSimpleName()));

            this.queryResponseHolder = new QueryResponseHolderImpl();
            this.queryResponseHolder.unpackContentFrom(unpacker);

            this.globalException = ReaderHelper.unpackStringValue(unpacker, "global exception",
                    this.getClass().getSimpleName());

            checkContent();
        } else {
            ReaderHelper.unpackNil(unpacker);
        }
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
        return globalStatus == that.globalStatus
                && Objects.equals(queryResponseHolder, that.queryResponseHolder)
                && Objects.equals(globalException, that.globalException);
    }

    @Override
    public int hashCode() {
        return Objects.hash(globalStatus, queryResponseHolder, globalException);
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

