
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.PriorityLevelHolder;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class PriorityLevelHolderImpl extends MessagePart implements PriorityLevelHolder {

    private Map<Integer, Boolean> operations;

    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public PriorityLevelHolderImpl() {
    }

    public PriorityLevelHolderImpl(Map<Integer, Boolean> operations) {

        this.operations = operations;

        checkContent();
    }

    @Override
    public void checkContent() {
        Validator.requireNonNullValue(getOperations(), getClass().getSimpleName(), "operations");
    }

    @Override
    protected Type getMessagePartType() {
        return Type.OTHER;
    }

    @Override
    public Map<Integer, Boolean> getOperations() {
        return this.operations;
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException, ValidationException {
        if (this.operations != null) {
            if (this.operations.size() == 0) {
                throw new ValidationException("Operation priorities cannot be empty!");
            }
            WriterHelper.packArrayHeader(packer, operations.size());
            for (Map.Entry<Integer, Boolean> entry : this.operations.entrySet()) {
                if (entry.getValue() == null) {
                    throw new ValidationException("Admin log write cannot be null!");
                }
                WriterHelper.packMapHeader(packer, 1);
                WriterHelper.packValue(packer, entry.getKey());
                WriterHelper.packValue(packer, entry.getValue());
            }

        } else {
            WriterHelper.packNil(packer);
        }

    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "priority level",
                PriorityLevelHolderImpl.class.getSimpleName())) {

            int arrayHeader = ReaderHelper.unpackArrayHeader(unpacker, null, "operations",
                    PriorityLevelHolderImpl.class.getSimpleName());

            operations = new HashMap<>();
            for (int i = 0; i < arrayHeader; ++i) {
                if (!ReaderHelper.nextExpectedValueTypeIsNil(
                        unpacker, ValueType.MAP, "operation", PriorityLevelHolderImpl.class.getSimpleName())) {

                    ReaderHelper.unpackMapHeader(unpacker, null, "operation", PriorityLevelHolderImpl.class.getSimpleName());
                    Integer mapKeyTemp = ReaderHelper.unpackIntegerValue(unpacker, "operation map key", PriorityLevelHolderImpl.class.getSimpleName());
                    Boolean mapValueTemp = ReaderHelper.unpackBooleanValue(unpacker, "operation map value", PriorityLevelHolderImpl.class.getSimpleName());

                    operations.put(mapKeyTemp, mapValueTemp);

                } else {
                    ReaderHelper.unpackNil(unpacker);
                }
            }

            if (operations.size() == 0) {
                throw new ValidationException("Operation priorities cannot be empty!");
            }
            checkContent();

        } else {
            ReaderHelper.unpackNil(unpacker);
        }
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
}
