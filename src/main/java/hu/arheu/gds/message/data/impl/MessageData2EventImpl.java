
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.MessageData2Event;
import hu.arheu.gds.message.data.PriorityLevelHolder;
import hu.arheu.gds.message.errors.ReadException;
import hu.arheu.gds.message.errors.ValidationException;
import hu.arheu.gds.message.errors.WriteException;
import hu.arheu.gds.message.util.Converters;
import hu.arheu.gds.message.util.ReaderHelper;
import hu.arheu.gds.message.util.Validator;
import hu.arheu.gds.message.util.WriterHelper;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.Externalizable;
import java.util.*;


public class MessageData2EventImpl extends MessagePart implements MessageData2Event {

    private String operations;
    private Map<String, byte[]> binaryContents;
    private List<PriorityLevelHolder> priorityLevels;

    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public MessageData2EventImpl() {
    }

    public MessageData2EventImpl(String operations,
                                 Map<String, byte[]> binaryContents,
                                 List<PriorityLevelHolder> priorityLevels) throws ValidationException {
        this(Collections.singletonList(operations), binaryContents, priorityLevels);
    }

    public MessageData2EventImpl(List<String> operations,
                                 Map<String, byte[]> binaryContents,
                                 List<PriorityLevelHolder> priorityLevels) throws ValidationException {

        Validator.requireNonEmptyCollection(operations, this.getClass().getSimpleName(),
                "operations");

        StringJoiner joiner = new StringJoiner(";");
        operations.forEach(joiner::add);
        this.operations = joiner.toString();

        if (binaryContents != null) {
            this.binaryContents = new HashMap<>();
            for (Map.Entry<String, byte[]> binaryContent : binaryContents.entrySet()) {
                this.binaryContents.put(
                        Converters.stringToUTF8Hex(binaryContent.getKey()),
                        binaryContent.getValue());
            }
        }
        this.priorityLevels = priorityLevels;

        //Priority validation
        Set<Integer> operationIndexes = new HashSet<>();
        for (PriorityLevelHolder priorityLevel : priorityLevels) {
            for (Integer operationIndex : priorityLevel.getOperations().keySet()) {
                if (operationIndexes.contains(operationIndex)) {
                    throw new ValidationException("The operational priority can not be included twice");
                } else {
                    operationIndexes.add(operationIndex);
                }
            }
        }

        checkContent();
    }

    public MessageData2EventImpl(byte[] binary) throws ReadException, ValidationException {
        deserialize(binary);
    }

    public MessageData2EventImpl(byte[] binary, boolean isFullMessage) throws ReadException, ValidationException {
        deserialize(binary, isFullMessage);
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

    protected Type getMessagePartType() {
        return Type.DATA;
    }

    @Override
    public void checkContent() {

        Validator.requireNonNullValue(this.operations, this.getClass().getSimpleName(),
                "operations");

        Validator.requireNonNullValue(this.binaryContents, this.getClass().getSimpleName(),
                "binaryContents");

        Validator.requireNonNullValue(this.priorityLevels, this.getClass().getSimpleName(),
                "priorityLevels");
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {

        WriterHelper.packArrayHeader(packer, 3);
        if (this.operations != null) {
            WriterHelper.packValue(packer, operations);
        } else {
            WriterHelper.packNil(packer);
        }
        WriterHelper.packMapStringByteArrayValues(packer, this.binaryContents);
        WriterHelper.packMessagePartCollection(packer, this.priorityLevels);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

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
                    PriorityLevelHolderImpl holder = new PriorityLevelHolderImpl();
                    holder.unpackContentFrom(unpacker);
                    this.priorityLevels.add(holder);
                }
            } else {
                ReaderHelper.unpackNil(unpacker);
            }
        } else {
            ReaderHelper.unpackNil(unpacker);
        }

        //Priority validation
        Set<Integer> operationIndexes = new HashSet<>();
        for (PriorityLevelHolder priorityLevel : priorityLevels) {
            for (Integer operationIndex : priorityLevel.getOperations().keySet()) {
                if (operationIndexes.contains(operationIndex)) {
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
        long sumBinaryContents = 0L;
        if (null != binaryContents) {
            for(byte[] by : binaryContents.values()) {
                sumBinaryContents += (null != by) ? by.length : 0L;
            }
        }
        return String.format("MessageData2EventImpl{operationsStringLen:%1$s, noOfBinaryContents:%2$s, " +
                        "sumOfBinaryContentsDataSize:%3$d, noOfPriorityLevels:%4$s, operations:%5$s",
                null != operations ? operations.length() : "null",
                null != binaryContents ? binaryContents.size() : "null",
                sumBinaryContents,
                null != priorityLevels ? priorityLevels.size() : "null",
                operations);
    }
}
