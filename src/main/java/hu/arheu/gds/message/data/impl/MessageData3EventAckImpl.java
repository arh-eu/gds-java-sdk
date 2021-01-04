package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePartType;
import hu.arheu.gds.message.data.EventResultHolder;
import hu.arheu.gds.message.data.MessageData3EventAck;
import hu.arheu.gds.message.data.MessageDataTypeHelper;
import hu.arheu.gds.message.header.MessageDataType;
import hu.arheu.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageData3EventAckImpl extends MessageData3EventAck {
    private List<EventResultHolder> eventResults;
    private AckStatus globalStatus;
    private String globalException;

    public MessageData3EventAckImpl(boolean cache,
                                    List<EventResultHolder> eventResults,
                                    AckStatus globalStatus,
                                    String globalException) throws IOException, ValidationException {
        this.eventResults = eventResults;
        this.globalStatus = globalStatus;
        this.globalException = globalException;
        this.cache = cache;
        checkContent();
        if (cache) {
            Serialize();
        }
    }

    public MessageData3EventAckImpl(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData3EventAckImpl(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }


    @Override
    protected void init() {
        this.typeHelper = new MessageDataTypeHelper() {
            @Override
            public MessageDataType getMessageDataType() {
                return MessageDataType.EVENT_ACK_3;
            }
            @Override
            public MessageData3EventAckImpl asEventAckMessageData3() {
                return MessageData3EventAckImpl.this;
            }
            @Override
            public boolean isEventAckMessageData3() {
                return true;
            }
        };
    }

    @Override
    public List<EventResultHolder> getEventResult() {
        return this.eventResults;
    }

    @Override
    public AckStatus getGlobalStatus() {
        return this.globalStatus;
    }

    @Override
    public String getGlobalException() {
        return this.globalException;
    }

    protected MessagePartType getMessagePartType() {
        return MessagePartType.DATA;
    }

    @Override
    protected void checkContent() {
        ExceptionHelper.requireNonNullValue(this.globalStatus, this.getClass().getSimpleName(),
                "globalStatus");
    }

    @Override
    protected void PackValues(MessageBufferPacker packer) throws IOException, ValidationException {
        WriterHelper.packArrayHeader(packer, 3);
        WriterHelper.packValue(packer, this.globalStatus == null ? null : this.globalStatus.getValue());
        WriterHelper.packPackables(packer, this.eventResults);
        WriterHelper.packValue(packer, this.globalException);
    }

    @Override
    protected void UnpackValues(MessageUnpacker unpacker) throws ReadException, IOException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "event ack data",
                this.getClass().getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, 3, "event ack data",
                    this.getClass().getSimpleName());

            this.globalStatus = AckStatus.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "global status",
                    this.getClass().getSimpleName()));

            if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "event results",
                    this.getClass().getSimpleName())) {

                this.eventResults = new ArrayList<>();
                int arrayHeaderSize = ReaderHelper.unpackArrayHeader(unpacker, null, "event results",
                        this.getClass().getSimpleName());
                for (int i = 0; i < arrayHeaderSize; ++i) {
                    this.eventResults.add(EventResultHolderImpl.unpackContent(unpacker));
                }
            } else {
                unpacker.unpackNil();
            }
            this.globalException = ReaderHelper.unpackStringValue(unpacker, "global exception",
                    this.getClass().getSimpleName());
        } else {
            unpacker.unpackNil();
        }
        checkContent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageData3EventAckImpl that = (MessageData3EventAckImpl) o;
        if (eventResults != null ? !eventResults.equals(that.eventResults) : that.eventResults != null) return false;
        if (globalStatus != that.globalStatus) return false;
        return globalException != null ? globalException.equals(that.globalException) : that.globalException == null;
    }

    @Override
    public int hashCode() {
        int result = eventResults != null ? eventResults.hashCode() : 0;
        result = 31 * result + (globalStatus != null ? globalStatus.hashCode() : 0);
        result = 31 * result + (globalException != null ? globalException.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MessageData3EventAckImpl{" +
                "eventResults=" + eventResults +
                ", globalStatus=" + globalStatus +
                ", globalException='" + globalException + '\'' +
                '}';
    }
}
