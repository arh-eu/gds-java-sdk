package hu.arh.gds.message.data.impl;

import hu.arh.gds.message.util.ReadException;
import hu.arh.gds.message.util.ReaderHelper;
import hu.arh.gds.message.util.WriterHelper;
import hu.arh.gds.message.data.EventDocumentResultHolder;
import hu.arh.gds.message.util.ExceptionHelper;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class EventDocumentResultHolderImpl implements EventDocumentResultHolder {
    private static final int NUMBER_OF_PUBLIC_FIELDS = 3;

    private final AckStatus status;
    private final String notification;
    private final Map<String, Value> returnValues;

    public EventDocumentResultHolderImpl(AckStatus status,
                                         String notification,
                                         Map<String, Value> returnValues) {
        this.status = status;
        this.notification = notification;
        this.returnValues = returnValues;
        checkContent(this);
    }

    private static void checkContent(EventDocumentResultHolder eventDocumentResult) {
        ExceptionHelper.requireNonNullValue(eventDocumentResult.getStatus(),
                eventDocumentResult.getClass().getSimpleName(),
                "status");
        ExceptionHelper.requireNonNullValue(eventDocumentResult.getReturnValues(),
                eventDocumentResult.getClass().getSimpleName(),
                "returnValues");
    }

    @Override
    public int getNumberOfPublicElements() {
        return NUMBER_OF_PUBLIC_FIELDS;
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
    public void packContent(MessageBufferPacker packer) throws IOException {
        WriterHelper.packArrayHeader(packer, getNumberOfPublicElements());
        WriterHelper.packValue(packer, this.status == null ? null : this.status.getValue());
        WriterHelper.packValue(packer, this.notification);
        WriterHelper.packMapStringValueValues(packer, this.returnValues);
    }

    public static EventDocumentResultHolder unpackContent(MessageUnpacker unpacker) throws IOException, ReadException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "result",
                EventDocumentResultHolderImpl.class.getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, NUMBER_OF_PUBLIC_FIELDS, "result",
                    EventDocumentResultHolderImpl.class.getSimpleName());

            AckStatus statusTemp = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "status",
                    EventDocumentResultHolderImpl.class.getSimpleName()));

            String notificationTemp = ReaderHelper.unpackStringValue(unpacker, "notification",
                    EventDocumentResultHolderImpl.class.getSimpleName());

            Map<String, Value> returnValuesTemp = ReaderHelper.unpackMapStringValueValues(unpacker,
                    null,
                    "return values",
                    "return values map key (fieldname)",
                    "return values map value (fieldvalue)",
                    EventDocumentResultHolderImpl.class.getSimpleName());

            EventDocumentResultHolder eventDocumentResultTemp = new EventDocumentResultHolderImpl(statusTemp,
                    notificationTemp,
                    returnValuesTemp);
            checkContent(eventDocumentResultTemp);
            return eventDocumentResultTemp;
        } else {
            unpacker.unpackNil();
        }
        return null;
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

    @Override
    public String toString() {
        return "EventDocumentResultHolderImpl{" +
                "status=" + status +
                ", notification='" + notification + '\'' +
                ", returnValues=" + returnValues +
                '}';
    }
}
