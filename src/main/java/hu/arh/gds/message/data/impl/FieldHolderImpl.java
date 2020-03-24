package hu.arh.gds.message.data.impl;

import hu.arh.gds.message.data.FieldHolder;
import hu.arh.gds.message.data.FieldValueType;
import hu.arh.gds.message.util.ExceptionHelper;
import hu.arh.gds.message.util.ReadException;
import hu.arh.gds.message.util.ReaderHelper;
import hu.arh.gds.message.util.WriterHelper;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.IOException;

public class FieldHolderImpl implements FieldHolder {
    private static final int NUMBER_OF_PUBLIC_ELEMENTS = 3;

    private final String fieldName;
    private final FieldValueType fieldType;
    private final String mimeType;

    public FieldHolderImpl(String fieldName,
                           FieldValueType fieldType,
                           String mimeType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.mimeType = mimeType;
        checkContent(this);
    }

    private static void checkContent(FieldHolder fieldDescriptor) {
        ExceptionHelper.requireNonNullValue(fieldDescriptor.getFieldType(), fieldDescriptor.getClass().getSimpleName(),
                "fieldType");
        ExceptionHelper.requireNonNullValue(fieldDescriptor.getMimeType(), fieldDescriptor.getClass().getSimpleName(),
                "mimeType");
        ExceptionHelper.requireNonNullValue(fieldDescriptor.getFieldName(), fieldDescriptor.getClass().getSimpleName(),
                "fieldName");
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
    public int getNumberOfPublicElements() {
        return NUMBER_OF_PUBLIC_ELEMENTS;
    }

    @Override
    public void packContent(MessageBufferPacker packer) throws IOException {
        WriterHelper.packArrayHeader(packer, getNumberOfPublicElements());
        WriterHelper.packValue(packer, this.fieldName);
        WriterHelper.packValue(packer, this.fieldType == null ? null : this.fieldType.toString());
        WriterHelper.packValue(packer, this.mimeType);
    }

    public static FieldHolder unpackContent(MessageUnpacker unpacker) throws ReadException, IOException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "field descriptors",
                FieldHolderImpl.class.getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, NUMBER_OF_PUBLIC_ELEMENTS, "field descriptors",
                    FieldHolderImpl.class.getSimpleName());

            FieldHolder descriptorFieldTemp = new FieldHolderImpl(
                    ReaderHelper.unpackStringValue(unpacker, "fieldname",
                            FieldHolderImpl.class.getSimpleName()),
                    ReaderHelper.unpackEnumValueAsString(unpacker, FieldValueType.class, "fieldtype",
                            FieldHolderImpl.class.getSimpleName()),
                    ReaderHelper.unpackStringValue(unpacker, "mime type",
                            FieldHolderImpl.class.getSimpleName()));

            checkContent(descriptorFieldTemp);
            return descriptorFieldTemp;
        } else {
            unpacker.unpackNil();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldHolderImpl that = (FieldHolderImpl) o;

        if (fieldName != null ? !fieldName.equals(that.fieldName) : that.fieldName != null) return false;
        if (fieldType != that.fieldType) return false;
        return mimeType != null ? mimeType.equals(that.mimeType) : that.mimeType == null;
    }

    @Override
    public int hashCode() {
        int result = fieldName != null ? fieldName.hashCode() : 0;
        result = 31 * result + (fieldType != null ? fieldType.hashCode() : 0);
        result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
        return result;
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
