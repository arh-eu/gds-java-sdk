package hu.arh.gds.message.data.impl;

import hu.arh.gds.message.data.GDSHolder;
import hu.arh.gds.message.util.ExceptionHelper;
import hu.arh.gds.message.util.ReadException;
import hu.arh.gds.message.util.ReaderHelper;
import hu.arh.gds.message.util.WriterHelper;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.IOException;

public class GDSHolderImpl implements GDSHolder {
    private static final int NUMBER_OF_PUBLIC_ELEMENTS = 2;

    private final String clusterName;
    private final String gdsNodeName;

    public GDSHolderImpl(String clusterName, String gdsNodeName) {
        this.clusterName = clusterName;
        this.gdsNodeName = gdsNodeName;
        checkContent(this);
    }

    private static void checkContent(GDSHolder gdsDescriptor) {
        ExceptionHelper.requireNonNullValue(gdsDescriptor.getClusterName(), gdsDescriptor.getClass().getSimpleName(),
                "clusterName");
        ExceptionHelper.requireNonNullValue(gdsDescriptor.getGDSNodeName(), gdsDescriptor.getClass().getSimpleName(),
                "gdsNodeName");
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
    public int getNumberOfPublicElements() {
        return NUMBER_OF_PUBLIC_ELEMENTS;
    }

    @Override
    public void packContent(MessageBufferPacker packer) throws IOException {
        WriterHelper.packArrayHeader(packer, getNumberOfPublicElements());
        WriterHelper.packValue(packer, getClusterName());
        WriterHelper.packValue(packer, getGDSNodeName());
    }

    public static GDSHolder unpackContent(MessageUnpacker unpacker) throws ReadException, IOException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.ARRAY, "gds descriptor",
                GDSHolderImpl.class.getSimpleName())) {

            ReaderHelper.unpackArrayHeader(unpacker, NUMBER_OF_PUBLIC_ELEMENTS, "gds descriptor",
                    GDSHolderImpl.class.getSimpleName());

            GDSHolder gdsHolderTemp = new GDSHolderImpl(
                    ReaderHelper.unpackStringValue(unpacker, "cluster name",
                            GDSHolderImpl.class.getSimpleName()),
                    ReaderHelper.unpackStringValue(unpacker, "gds node name",
                            GDSHolderImpl.class.getSimpleName()));

            checkContent(gdsHolderTemp);
            return gdsHolderTemp;
        } else {
            unpacker.unpackNil();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GDSHolderImpl gdsHolder = (GDSHolderImpl) o;
        if (clusterName != null ? !clusterName.equals(gdsHolder.clusterName) : gdsHolder.clusterName != null)
            return false;
        return gdsNodeName != null ? gdsNodeName.equals(gdsHolder.gdsNodeName) : gdsHolder.gdsNodeName == null;
    }

    @Override
    public int hashCode() {
        int result = clusterName != null ? clusterName.hashCode() : 0;
        result = 31 * result + (gdsNodeName != null ? gdsNodeName.hashCode() : 0);
        return result;
    }
}
