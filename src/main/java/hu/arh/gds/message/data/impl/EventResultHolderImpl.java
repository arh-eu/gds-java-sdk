package hu.arh.gds.message.data.impl;

import hu.arh.gds.message.data.EventResultHolder;
import hu.arh.gds.message.data.EventSubResultHolder;
import hu.arh.gds.message.data.FieldHolder;
import hu.arh.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EventResultHolderImpl implements EventResultHolder {
    private final AckStatus status;
    private final String notification;
    private final List<FieldHolder> fieldHolders;
    private final List<EventSubResultHolder> fieldValues;

    public EventResultHolderImpl(AckStatus status,
                                 String notification,
                                 List<FieldHolder> fieldHolders,
                                 List<EventSubResultHolder> fieldValues) {
        this.status = status;
        this.notification = notification;
        this.fieldHolders = fieldHolders;
        this.fieldValues = fieldValues;
        checkContent(this);
    }

    private static void checkContent(EventResultHolder eventResult) {
        ExceptionHelper.requireNonNullValue(eventResult.getStatus(), eventResult.getClass().getSimpleName(),
                "status");
        ExceptionHelper.requireNonNullValue(eventResult.getFieldHolders(), eventResult.getClass().getSimpleName(),
                "fieldHolders");
        ExceptionHelper.requireNonNullValue(eventResult.getFieldValues(), eventResult.getClass().getSimpleName(),
                "fieldValues");
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
    public void packContent(MessageBufferPacker packer) throws IOException, ValidationException {
        WriterHelper.packArrayHeader(packer, 4);
        WriterHelper.packValue(packer, this.status == null ? null : this.status.getValue());
        WriterHelper.packValue(packer, this.notification);
        WriterHelper.packPackables(packer, this.fieldHolders);
        WriterHelper.packPackables(packer, this.fieldValues);
    }

    public static EventResultHolder unpackContent(MessageUnpacker unpacker) throws ReadException, IOException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "event response",
                EventResultHolderImpl.class.getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, 4, "event response",
                    EventResultHolderImpl.class.getSimpleName());

            AckStatus statusTemp = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "status",
                    EventResultHolderImpl.class.getSimpleName()));

            String notificationTemp = ReaderHelper.unpackStringValue(unpacker, "notification",
                    EventResultHolderImpl.class.getSimpleName());

            List<FieldHolder> fieldHoldersTemp = null;

            if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "field descriptors",
                    EventResultHolderImpl.class.getSimpleName())) {

                fieldHoldersTemp = new ArrayList<>();

                int arrayHeaderSize = ReaderHelper.unpackArrayHeader(unpacker, null, "fieldescriptors",
                        EventResultHolderImpl.class.getSimpleName());

                for (int i = 0; i < arrayHeaderSize; i++) {
                    fieldHoldersTemp.add(FieldHolderImpl.unpackContent(unpacker));
                }
            } else {
                unpacker.unpackNil();
            }

            List<EventSubResultHolder> fieldValuesTemp = null;

            if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "field values",
                    EventResultHolderImpl.class.getSimpleName())) {

                fieldValuesTemp = new ArrayList<>();

                int arrayHeaderSize = ReaderHelper.unpackArrayHeader(unpacker, null, "field values",
                        EventResultHolderImpl.class.getSimpleName());

                for (int i = 0; i < arrayHeaderSize; i++) {
                    fieldValuesTemp.add(EventSubResultHolderImpl.unpackContent(unpacker));
                }

            } else {
                unpacker.unpackNil();
            }

            EventResultHolderImpl eventResultTemp = new EventResultHolderImpl(statusTemp,
                    notificationTemp,
                    fieldHoldersTemp,
                    fieldValuesTemp);

            checkContent(eventResultTemp);
            return eventResultTemp;
        } else {
            unpacker.unpackNil();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventResultHolderImpl that = (EventResultHolderImpl) o;
        if (status != that.status) return false;
        if (notification != null ? !notification.equals(that.notification) : that.notification != null) return false;
        if (fieldHolders != null ? !fieldHolders.equals(that.fieldHolders) : that.fieldHolders != null) return false;
        return fieldValues != null ? fieldValues.equals(that.fieldValues) : that.fieldValues == null;
    }

    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (notification != null ? notification.hashCode() : 0);
        result = 31 * result + (fieldHolders != null ? fieldHolders.hashCode() : 0);
        result = 31 * result + (fieldValues != null ? fieldValues.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("EventResultHolderImpl{status=%1$s}", status);
    }
}
