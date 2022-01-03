
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.EventDocumentResultHolder;
import hu.arheu.gds.message.errors.ReadException;
import hu.arheu.gds.message.errors.WriteException;
import hu.arheu.gds.message.util.ReaderHelper;
import hu.arheu.gds.message.util.Validator;
import hu.arheu.gds.message.util.WriterHelper;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

import java.io.Externalizable;
import java.util.Map;
import java.util.Objects;


public class EventDocumentResultHolderImpl extends MessagePart implements EventDocumentResultHolder {

    private AckStatus status;
    private String notification;
    private Map<String, Value> returnValues;


    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public EventDocumentResultHolderImpl() {
    }

    public EventDocumentResultHolderImpl(AckStatus status,
                                         String notification,
                                         Map<String, Value> returnValues) {

        this.status = status;
        this.notification = notification;
        this.returnValues = returnValues;

        checkContent();
    }

    @Override
    public void checkContent() {
        Validator.requireNonNullValue(getStatus(), getClass().getSimpleName(), "status");
        Validator.requireNonNullValue(getReturnValues(), getClass().getSimpleName(), "returnValues");
    }

    @Override
    protected Type getMessagePartType() {
        return Type.OTHER;
    }

    @Override
    public AckStatus getStatus() {
        return this.status;
    }

    @Override
    public String getNotification() {
        return this.notification;
    }

    @Override
    public Map<String, Value> getReturnValues() {
        return this.returnValues;
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {

        WriterHelper.packArrayHeader(packer, getNumberOfPublicElements());
        WriterHelper.packValue(packer, this.status == null ? null : this.status.getValue());
        WriterHelper.packValue(packer, this.notification);
        WriterHelper.packMapStringValueValues(packer, this.returnValues);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "result",
                EventDocumentResultHolderImpl.class.getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, getNumberOfPublicElements(), "result",
                    EventDocumentResultHolderImpl.class.getSimpleName());

            status = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "status",
                    EventDocumentResultHolderImpl.class.getSimpleName()));

            notification = ReaderHelper.unpackStringValue(unpacker, "notification",
                    EventDocumentResultHolderImpl.class.getSimpleName());

            returnValues = ReaderHelper.unpackMapStringValueValues(unpacker,
                    null,
                    "return values",
                    "return values map key (fieldname)",
                    "return values map value (fieldvalue)",
                    EventDocumentResultHolderImpl.class.getSimpleName());

            checkContent();
        } else {
            ReaderHelper.unpackNil(unpacker);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventDocumentResultHolderImpl)) return false;
        EventDocumentResultHolderImpl that = (EventDocumentResultHolderImpl) o;
        return status == that.status &&
                Objects.equals(notification, that.notification) &&
                Objects.equals(returnValues, that.returnValues);
    }

    @Override
    public int hashCode() {

        return Objects.hash(status, notification, returnValues);
    }
}
