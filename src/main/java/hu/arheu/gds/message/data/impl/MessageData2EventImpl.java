package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePartType;
import hu.arheu.gds.message.data.MessageData2Event;
import hu.arheu.gds.message.data.MessageDataTypeHelper;
import hu.arheu.gds.message.data.PriorityLevelHolder;
import hu.arheu.gds.message.header.MessageDataType;
import hu.arheu.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.*;

public class MessageData2EventImpl extends MessageData2Event {
    private String operations;
    private Map<String, byte[]> binaryContents;
    private List<PriorityLevelHolder> priorityLevels;

    public MessageData2EventImpl(boolean cache,
                                 String operations,
                                 Map<String, byte[]> binaryContents,
                                 List<PriorityLevelHolder> priorityLevels) throws IOException, ValidationException {

        this.operations = operations;
        if (binaryContents != null) {
            this.binaryContents = new HashMap<>();
            for (Map.Entry<String, byte[]> binaryContent : binaryContents.entrySet()) {
                this.binaryContents.put(
                        Utils.stringToUTF8Hex(
                                binaryContent.getKey()),
                        binaryContent.getValue());
            }
        }
        this.priorityLevels = priorityLevels;
        this.cache = cache;

        //Priority validation
        Set<Integer> operationIndexes = new HashSet<>();
        for(PriorityLevelHolder priorityLevel: priorityLevels) {
            for(Integer operationIndex: priorityLevel.getOperations().keySet()) {
                if(operationIndexes.contains(operationIndex)) {
                    throw new ValidationException("The operational priority can not be included twice");
                } else {
                    operationIndexes.add(operationIndex);
                }
            }
        }

        checkContent();

        if (cache) {
            Serialize();
        }
    }

    public MessageData2EventImpl(boolean cache,
                                 List<String> operations,
                                 Map<String, byte[]> binaryContents,
                                 List<PriorityLevelHolder> priorityLevels) throws IOException, ValidationException {

        ExceptionHelper.requireNonEmptyCollection(operations, this.getClass().getSimpleName(),
                "operations");

        this.operations = "";
        for(int i = 0; i < operations.size(); ++i) {
            this.operations += operations.get(i);
            if(i != operations.size() -1) {
                if(!operations.get(i).endsWith(";")) {
                    this.operations += ";";
                }
            }
        }

        if (binaryContents != null) {
            this.binaryContents = new HashMap<>();
            for (Map.Entry<String, byte[]> binaryContent : binaryContents.entrySet()) {
                this.binaryContents.put(
                        Utils.stringToUTF8Hex(
                                binaryContent.getKey()),
                                binaryContent.getValue());
            }
        }
        this.priorityLevels = priorityLevels;
        this.cache = cache;

        //Priority validation
        Set<Integer> operationIndexes = new HashSet<>();
        for(PriorityLevelHolder priorityLevel: priorityLevels) {
            for(Integer operationIndex: priorityLevel.getOperations().keySet()) {
                if(operationIndexes.contains(operationIndex)) {
                    throw new ValidationException("The operational priority can not be included twice");
                } else {
                    operationIndexes.add(operationIndex);
                }
            }
        }

        checkContent();

        if (cache) {
            Serialize();
        }
    }

    public MessageData2EventImpl(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageData2EventImpl(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    @Override
    protected void init() {
        this.typeHelper = new MessageDataTypeHelper() {
            @Override
            public MessageDataType getMessageDataType() {
                return MessageDataType.EVENT_2;
            }
            @Override
            public MessageData2Event asEventMessageData2() {
                return MessageData2EventImpl.this;
            }
            @Override
            public boolean isEventMessageData2() {
                return true;
            }
        };
    }

    @Override
    public String getOperations() {
        return this.operations;
    }

    @Override
    public Map<String, byte[]> getBinaryContents() {
        return this.binaryContents;
    }

    @Override
    public List<PriorityLevelHolder> getPriorityLevels() {
        return this.priorityLevels;
    }

    protected MessagePartType getMessagePartType() {
        return MessagePartType.DATA;
    }

    @Override
    protected void checkContent() {
        ExceptionHelper.requireNonNullValue(this.operations, this.getClass().getSimpleName(),
                "operations");

        ExceptionHelper.requireNonNullValue(this.binaryContents, this.getClass().getSimpleName(),
                "binaryContents");

        ExceptionHelper.requireNonNullValue(this.priorityLevels, this.getClass().getSimpleName(),
                "priorityLevels");
    }

    @Override
    protected void PackValues(MessageBufferPacker packer) throws IOException, ValidationException {

        WriterHelper.packArrayHeader(packer, 3);
        if(this.operations != null) {
            WriterHelper.packValue(packer, operations);
        } else {
            packer.packNil();
        }
        WriterHelper.packMapStringByteArrayValues(packer, this.binaryContents);
        WriterHelper.packPackables(packer, this.priorityLevels);
    }

    @Override
    protected void UnpackValues(MessageUnpacker unpacker) throws ReadException, IOException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "event data",
                this.getClass().getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, 3, "event data", this.getClass().getSimpleName());

            this.operations = ReaderHelper.unpackStringValue(unpacker, "operations",
                    this.getClass().getSimpleName());

            this.binaryContents = ReaderHelper.unpackMapStringByteArrayValues(unpacker,
                    null,
                    "binary contents",
                    "binary contents map key",
                    "binary contents map value",
                    this.getClass().getSimpleName());

            if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "priority levels",
                    this.getClass().getSimpleName())) {

                this.priorityLevels = new ArrayList<>();

                int arrayHeaderSize = ReaderHelper.unpackArrayHeader(unpacker, null, "priority level",
                        this.getClass().getSimpleName());

                for (int j = 0; j < arrayHeaderSize; j++) {
                    this.priorityLevels.add(PriorityLevelHolderImpl.unpackContent(unpacker));
                }
            } else {
                unpacker.unpackNil();
            }
        } else {
            unpacker.unpackNil();
        }

        //Priority validation
        Set<Integer> operationIndexes = new HashSet<>();
        for(PriorityLevelHolder priorityLevel: priorityLevels) {
            for(Integer operationIndex: priorityLevel.getOperations().keySet()) {
                if(operationIndexes.contains(operationIndex)) {
                    throw new ValidationException("The operational priority can not be included twice");
                } else {
                    operationIndexes.add(operationIndex);
                }
            }
        }

        checkContent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageData2EventImpl that = (MessageData2EventImpl) o;
        return Objects.equals(operations, that.operations) &&
                Objects.equals(binaryContents, that.binaryContents) &&
                Objects.equals(priorityLevels, that.priorityLevels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operations, binaryContents, priorityLevels);
    }

    @Override
    public String toString() {
        return "MessageData2EventImpl{" +
                "operations='" + operations + '\'' +
                ", binaryContents=" + binaryContents +
                ", priorityLevels=" + priorityLevels +
                '}';
    }
}
