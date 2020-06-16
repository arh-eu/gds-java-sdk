package hu.arh.gds.message.data.impl;

import hu.arh.gds.message.data.PriorityLevelHolder;
import hu.arh.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.*;

public class PriorityLevelHolderImpl implements PriorityLevelHolder {
    private static final int NUMBER_OF_PUBLIC_ELEMENTS = 1;
    private Map<Integer, Boolean> operations;

    public PriorityLevelHolderImpl(Map<Integer, Boolean> operations) {
        this.operations = operations;
        checkContent(this);
    }

    private static void checkContent(PriorityLevelHolder priorityLevel) {
        ExceptionHelper.requireNonNullValue(priorityLevel.getOperations(), priorityLevel.getClass().getSimpleName(),
                "operations");
    }

    @Override
    public Map<Integer, Boolean> getOperations() {
        return this.operations;
    }

    @Override
    public int getNumberOfPublicElements() {
        return NUMBER_OF_PUBLIC_ELEMENTS;
    }

    @Override
    public void packContent(MessageBufferPacker packer) throws IOException, ValidationException {
        if(this.operations.size() == 0) {
            throw new ValidationException("Operation priorities cannot be empty!");
        }
        if(this.operations != null) {
            WriterHelper.packArrayHeader(packer, operations.size());
            for (Map.Entry<Integer, Boolean> entry : this.operations.entrySet()) {
                if(entry.getValue() == null) {
                    throw new ValidationException("Adming log write cannot be null");
                }
                WriterHelper.packMapHeader(packer, 1);
                WriterHelper.packValue(packer, entry.getKey());
                WriterHelper.packValue(packer, entry.getValue());
            }
        } else {
            packer.packNil();
        }
    }

    public static PriorityLevelHolder unpackContent(MessageUnpacker unpacker) throws IOException, ReadException, ValidationException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "priority level",
                PriorityLevelHolderImpl.class.getSimpleName())) {

            int arrayHeader = ReaderHelper.unpackArrayHeader(unpacker, null, "operations",
                    PriorityLevelHolderImpl.class.getSimpleName());

            Map<Integer, Boolean> operationsTemp = new HashMap<>();
            for(int i = 0; i < arrayHeader; ++i) {
                if (!ReaderHelper.nextExpectedValueTypeIsNil(
                        unpacker, ValueType.MAP, "operation", PriorityLevelHolderImpl.class.getSimpleName())) {

                    ReaderHelper.unpackMapHeader(unpacker, null, "operation", PriorityLevelHolderImpl.class.getSimpleName());
                    Integer mapKeyTemp = ReaderHelper.unpackIntegerValue(unpacker, "operation map key", PriorityLevelHolderImpl.class.getSimpleName());
                    Boolean mapValueTemp = ReaderHelper.unpackBooleanValue(unpacker, "operation map value", PriorityLevelHolderImpl.class.getSimpleName());

                    operationsTemp.put(mapKeyTemp, mapValueTemp);
                } else {
                    unpacker.unpackNil();
                }
            }

            if(operationsTemp.size() == 0) {
                throw new ValidationException("Operation priorities cannot be empty!");
            }

            PriorityLevelHolder priorityLevelDescriptorTemp = new PriorityLevelHolderImpl(operationsTemp);
            checkContent(priorityLevelDescriptorTemp);
            return priorityLevelDescriptorTemp;
        } else {
            unpacker.unpackNil();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PriorityLevelHolderImpl)) return false;
        PriorityLevelHolderImpl that = (PriorityLevelHolderImpl) o;
        return Objects.equals(operations, that.operations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operations);
    }

    @Override
    public String toString() {
        return "PriorityLevelHolderImpl{" +
                "operations=" + operations +
                '}';
    }
}
