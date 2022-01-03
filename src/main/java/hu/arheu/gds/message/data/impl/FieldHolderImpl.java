
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.FieldHolder;
import hu.arheu.gds.message.data.FieldValueType;
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


public class FieldHolderImpl extends MessagePart implements FieldHolder {

    private String fieldName;
    private FieldValueType fieldType;
    private String mimeType;


    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public FieldHolderImpl() {
    }

    public FieldHolderImpl(String fieldName,
                           FieldValueType fieldType,
                           String mimeType) throws ValidationException {

        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.mimeType = mimeType;

        checkContent();
    }

    @Override
    public void checkContent() throws ValidationException {

        Validator.requireNonNullValue(getFieldType(), getClass().getSimpleName(),
                "fieldType");

        Validator.requireNonNullValue(getMimeType(), getClass().getSimpleName(),
                "mimeType");

        Validator.requireNonNullValue(getFieldName(), getClass().getSimpleName(),
                "fieldName");
    }

    @Override
    protected Type getMessagePartType() {
        return Type.OTHER;
    }

    @Override
    public String getFieldName() {
        return this.fieldName;
    }

    @Override
    public FieldValueType getFieldType() {
        return this.fieldType;
    }

    @Override
    public String getMimeType() {
        return this.mimeType;
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {

        WriterHelper.packArrayHeader(packer, getNumberOfPublicElements());
        WriterHelper.packValue(packer, this.fieldName);
        WriterHelper.packValue(packer, this.fieldType == null ? null : this.fieldType.toString());
        WriterHelper.packValue(packer, this.mimeType);
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "field descriptors",
                FieldHolderImpl.class.getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, getNumberOfPublicElements(), "field descriptors",
                    FieldHolderImpl.class.getSimpleName());

            fieldName = ReaderHelper.unpackStringValue(unpacker, "fieldname",
                    FieldHolderImpl.class.getSimpleName());
            fieldType = ReaderHelper.unpackEnumValueAsString(unpacker, FieldValueType.class, "fieldtype",
                    FieldHolderImpl.class.getSimpleName());
            mimeType = ReaderHelper.unpackStringValue(unpacker, "mime type",
                    FieldHolderImpl.class.getSimpleName());
            checkContent();
        } else {
            ReaderHelper.unpackNil(unpacker);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldHolderImpl that = (FieldHolderImpl) o;
        return Objects.equals(fieldName, that.fieldName)
                && fieldType == that.fieldType
                && Objects.equals(mimeType, that.mimeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, fieldType, mimeType);
    }

    @Override
    public String toString() {
        return "FieldHolderImpl{" +
                "fieldName='" + fieldName + '\'' +
                ", fieldType=" + fieldType +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }
}
