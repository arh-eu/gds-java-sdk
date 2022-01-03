
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.EventResultHolder;
import hu.arheu.gds.message.data.EventSubResultHolder;
import hu.arheu.gds.message.data.FieldHolder;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class EventResultHolderImpl extends MessagePart implements EventResultHolder {

    private AckStatus status;
    private String notification;
    private List<FieldHolder> fieldHolders;
    private List<EventSubResultHolder> fieldValues;


    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public EventResultHolderImpl() {
    }

    public EventResultHolderImpl(AckStatus status,
                                 String notification,
                                 List<FieldHolder> fieldHolders,
                                 List<EventSubResultHolder> fieldValues) {

        this.status = status;
        this.notification = notification;
        this.fieldHolders = fieldHolders;
        this.fieldValues = fieldValues;

        checkContent();
    }

    @Override
    public void checkContent() {

        Validator.requireNonNullValue(getStatus(), getClass().getSimpleName(), "status");

        Validator.requireNonNullValue(getFieldHolders(), getClass().getSimpleName(), "fieldHolders");

        Validator.requireNonNullValue(getFieldValues(), getClass().getSimpleName(), "fieldValues");
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
    public List<FieldHolder> getFieldHolders() {
        return this.fieldHolders;
    }

    @Override
    public List<EventSubResultHolder> getFieldValues() {
        return this.fieldValues;
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException, ValidationException {

        WriterHelper.packArrayHeader(packer, 4);
        WriterHelper.packValue(packer, this.status == null ? null : this.status.getValue());
        WriterHelper.packValue(packer, this.notification);
        WriterHelper.packMessagePartCollection(packer, this.fieldHolders);
        WriterHelper.packMessagePartCollection(packer, this.fieldValues);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "event response",
                EventResultHolderImpl.class.getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, 4, "event response",
                    EventResultHolderImpl.class.getSimpleName());

            status = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "status",
                    EventResultHolderImpl.class.getSimpleName()));

            notification = ReaderHelper.unpackStringValue(unpacker, "notification",
                    EventResultHolderImpl.class.getSimpleName());

            fieldHolders = null;

            if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "field descriptors",
                    EventResultHolderImpl.class.getSimpleName())) {

                fieldHolders = new ArrayList<>();

                int arrayHeaderSize = ReaderHelper.unpackArrayHeader(unpacker, null, "fieldescriptors",
                        EventResultHolderImpl.class.getSimpleName());

                for (int i = 0; i < arrayHeaderSize; i++) {
                    FieldHolderImpl holder = new FieldHolderImpl();
                    holder.unpackContentFrom(unpacker);
                    fieldHolders.add(holder);
                }
            } else {
                ReaderHelper.unpackNil(unpacker);
            }

            fieldValues = null;

            if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "field values",
                    EventResultHolderImpl.class.getSimpleName())) {

                fieldValues = new ArrayList<>();

                int arrayHeaderSize = ReaderHelper.unpackArrayHeader(unpacker, null, "field values",
                        EventResultHolderImpl.class.getSimpleName());

                for (int i = 0; i < arrayHeaderSize; i++) {
                    EventSubResultHolderImpl holder = new EventSubResultHolderImpl();
                    holder.unpackContentFrom(unpacker);
                    fieldValues.add(holder);
                }

            } else {
                ReaderHelper.unpackNil(unpacker);
            }

            checkContent();

        } else {
            ReaderHelper.unpackNil(unpacker);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventResultHolderImpl that = (EventResultHolderImpl) o;
        return status == that.status
                && Objects.equals(notification, that.notification)
                && Objects.equals(fieldHolders, that.fieldHolders)
                && Objects.equals(fieldValues, that.fieldValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, notification, fieldHolders, fieldValues);
    }

    @Override
    protected Type getMessagePartType() {
        return Type.OTHER;
    }

    @Override
    public String toString() {
        return String.format("EventResultHolderImpl{status=%1$s, noOfFieldValues=%2$d}",
                status,
                fieldValues.size());
    }
}
