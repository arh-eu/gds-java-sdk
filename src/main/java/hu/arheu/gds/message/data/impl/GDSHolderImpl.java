
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.GDSHolder;
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


public class GDSHolderImpl extends MessagePart implements GDSHolder {
    private String clusterName;
    private String gdsNodeName;


    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public GDSHolderImpl() {
    }

    public GDSHolderImpl(String clusterName,
                         String gdsNodeName) {

        this.clusterName = clusterName;
        this.gdsNodeName = gdsNodeName;

        checkContent();
    }

    @Override
    public void checkContent() {

        Validator.requireNonNullValue(getClusterName(), getClass().getSimpleName(),
                "clusterName");

        Validator.requireNonNullValue(getGDSNodeName(), getClass().getSimpleName(),
                "gdsNodeName");
    }

    @Override
    protected Type getMessagePartType() {
        return Type.OTHER;
    }

    @Override
    public String getClusterName() {
        return this.clusterName;
    }

    @Override
    public String getGDSNodeName() {
        return this.gdsNodeName;
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {

        WriterHelper.packArrayHeader(packer, getNumberOfPublicElements());
        WriterHelper.packValue(packer, getClusterName());
        WriterHelper.packValue(packer, getGDSNodeName());
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {

        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "gds descriptor",
                GDSHolderImpl.class.getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, getNumberOfPublicElements(), "gds descriptor",
                    GDSHolderImpl.class.getSimpleName());

            clusterName = ReaderHelper.unpackStringValue(unpacker, "cluster name",
                    GDSHolderImpl.class.getSimpleName());
            gdsNodeName = ReaderHelper.unpackStringValue(unpacker, "gds node name",
                    GDSHolderImpl.class.getSimpleName());

            checkContent();
        } else {
            ReaderHelper.unpackNil(unpacker);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GDSHolderImpl gdsHolder = (GDSHolderImpl) o;
        return Objects.equals(clusterName, gdsHolder.clusterName)
                && Objects.equals(gdsNodeName, gdsHolder.gdsNodeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusterName, gdsNodeName);
    }
}
