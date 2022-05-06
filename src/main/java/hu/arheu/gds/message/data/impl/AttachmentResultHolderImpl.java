
package hu.arheu.gds.message.data.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.AttachmentResultHolder;
import hu.arheu.gds.message.errors.ReadException;
import hu.arheu.gds.message.errors.WriteException;
import hu.arheu.gds.message.util.ReaderHelper;
import hu.arheu.gds.message.util.Validator;
import hu.arheu.gds.message.util.WriterHelper;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.Externalizable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


public class AttachmentResultHolderImpl extends MessagePart implements AttachmentResultHolder {

    private List<String> requestIds;
    private String ownerTable;
    private String attachmentId;
    private List<String> ownerIds;
    private String meta;
    private Long ttl;
    private Long toValid;
    private byte[] attachment;

    //not part of the message
    private AttachmentResultHolder.Type type;


    /**
     * Do not remove, as it's needed for the serialization through {@link Externalizable}
     */
    public AttachmentResultHolderImpl() {
    }

    public AttachmentResultHolderImpl(List<String> requestIds,
                                      String ownerTable,
                                      String attachmentId,
                                      List<String> ownerIds,
                                      String meta,
                                      Long ttl,
                                      Long toValid,
                                      byte[] attachment) {

        this.requestIds = requestIds;
        this.ownerTable = ownerTable;
        this.attachmentId = attachmentId;
        this.ownerIds = ownerIds;
        this.meta = meta;
        this.ttl = ttl;
        this.toValid = toValid;
        this.attachment = attachment;

        checkContent();
    }

    public AttachmentResultHolderImpl(List<String> requestIds,
                                      String ownerTable,
                                      String attachmentId) {

        this.requestIds = requestIds;
        this.ownerTable = ownerTable;
        this.attachmentId = attachmentId;
        this.type = AttachmentResultHolder.Type.ATTACHMENT_RESPONSE_ACK;

        checkContent();
    }

    @Override
    public void checkContent() {
        Validator.requireNonNullValue(getRequestIds(), AttachmentResultHolderImpl.class.getSimpleName(), "requestIds");
        Validator.requireNonNullValue(getOwnerTable(), AttachmentResultHolderImpl.class.getSimpleName(), "ownerTable");
        Validator.requireNonNullValue(getAttachmentId(), AttachmentResultHolderImpl.class.getSimpleName(), "attachmentId");
        if (type != AttachmentResultHolder.Type.ATTACHMENT_RESPONSE_ACK) {
            Validator.requireNonNullValue(getOwnerIds(), AttachmentResultHolderImpl.class.getSimpleName(), "ownerIds");
        }
    }

    @Override
    protected MessagePart.Type getMessagePartType() {
        return MessagePart.Type.OTHER;
    }

    @Override
    public List<String> getRequestIds() {
        return requestIds;
    }

    @Override
    public String getOwnerTable() {
        return ownerTable;
    }

    @Override
    public String getAttachmentId() {
        return attachmentId;
    }

    @Override
    public List<String> getOwnerIds() {
        return ownerIds;
    }

    @Override
    public String getMeta() {
        return meta;
    }

    @Override
    public Long getTtl() {
        return ttl;
    }

    @Override
    public Long getToValid() {
        return toValid;
    }

    @Override
    public byte[] getAttachment() {
        return attachment;
    }

    private int getMapHeaderSize() {
        return (int) Stream.of(
                requestIds,
                ownerTable,
                attachmentId,
                ownerIds,
                meta,
                ttl,
                toValid,
                attachment
        ).filter(Objects::nonNull).count();
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {
        WriterHelper.packMapHeader(packer, getMapHeaderSize());
        if (requestIds != null) {
            WriterHelper.packValue(packer, "requestids");
            WriterHelper.packStringCollection(packer, requestIds);
        }
        if (ownerTable != null) {
            WriterHelper.packValue(packer, "ownertable");
            WriterHelper.packValue(packer, ownerTable);
        }
        if (attachmentId != null) {
            WriterHelper.packValue(packer, "attachmentid");
            WriterHelper.packValue(packer, attachmentId);
        }
        if (ownerIds != null) {
            WriterHelper.packValue(packer, "ownerids");
            WriterHelper.packStringCollection(packer, ownerIds);
        }
        if (meta != null) {
            WriterHelper.packValue(packer, "meta");
            WriterHelper.packValue(packer, meta);
        }
        if (ttl != null) {
            WriterHelper.packValue(packer, "ttl");
            WriterHelper.packValue(packer, ttl);
        }
        if (toValid != null) {
            WriterHelper.packValue(packer, "to_valid");
            WriterHelper.packValue(packer, toValid);
        }
        if (attachment != null) {
            WriterHelper.packValue(packer, "attachment");
            WriterHelper.packValue(packer, attachment);
        }
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.MAP, "result",
                AttachmentResultHolderImpl.class.getSimpleName())) {
            int mapHeaderSize = ReaderHelper.unpackMapHeader(unpacker, null, "result",
                    AttachmentResultHolderImpl.class.getSimpleName());


            for (int i = 0; i < mapHeaderSize; ++i) {
                String key = ReaderHelper.unpackStringValue(unpacker, "result map key",
                        AttachmentResultHolderImpl.class.getSimpleName());

                if (type == AttachmentResultHolder.Type.ATTACHMENT_RESPONSE_ACK) {
                    switch (Objects.requireNonNull(key)) {
                        case "requestids" -> requestIds = ReaderHelper.unpackStringValues(unpacker, null, "requestids",
                                "request ids", AttachmentResultHolderImpl.class.getSimpleName());
                        case "ownertable" -> ownerTable = ReaderHelper.unpackStringValue(unpacker, "owner table",
                                AttachmentResultHolderImpl.class.getSimpleName());
                        case "attachmentid" -> attachmentId = ReaderHelper.unpackStringValue(unpacker, "attachment id",
                                AttachmentResultHolderImpl.class.getSimpleName());
                        default -> throw new ReadException(
                                String.format("Map key value [%s] does not match exptected value: " +
                                                "[%s]/[%s]/[%s]",
                                        key,
                                        "requestids",
                                        "ownertable",
                                        "attachmentid"));
                    }
                } else {
                    switch (Objects.requireNonNull(key)) {
                        case "requestids" -> requestIds = ReaderHelper.unpackStringValues(unpacker, null, "requestids",
                                "request ids", AttachmentResultHolderImpl.class.getSimpleName());
                        case "ownertable" -> ownerTable = ReaderHelper.unpackStringValue(unpacker, "owner table",
                                AttachmentResultHolderImpl.class.getSimpleName());
                        case "attachmentid" -> attachmentId = ReaderHelper.unpackStringValue(unpacker, "attachment id",
                                AttachmentResultHolderImpl.class.getSimpleName());
                        case "ownerids" -> ownerIds = ReaderHelper.unpackStringValues(unpacker, null, "ownerids",
                                "owner ids", AttachmentResultHolderImpl.class.getSimpleName());
                        case "meta" -> meta = ReaderHelper.unpackStringValue(unpacker, "meta",
                                AttachmentResultHolderImpl.class.getSimpleName());
                        case "ttl" -> ttl = ReaderHelper.unpackLongValue(unpacker, "ttl",
                                AttachmentResultHolderImpl.class.getSimpleName());
                        case "to_valid" -> toValid = ReaderHelper.unpackLongValue(unpacker, "to valid",
                                AttachmentResultHolderImpl.class.getSimpleName());
                        case "attachment" -> attachment = ReaderHelper.unpackBinary(unpacker, "attachment",
                                AttachmentResultHolderImpl.class.getSimpleName());
                        default -> throw new ReadException(
                                String.format("Map key value [%s] does not match exptected value: " +
                                                "[%s]/[%s]/[%s]/[%s]/[%s]/[%s]/[%s]/[%s]", key,
                                        "requestids",
                                        "ownertable",
                                        "attachmentid",
                                        "ownerids",
                                        "meta",
                                        "ttl",
                                        "to_valid",
                                        "attachment"));
                    }
                }
            }
            checkContent();

        } else {
            ReaderHelper.unpackNil(unpacker);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttachmentResultHolderImpl that)) return false;
        return Objects.equals(requestIds, that.requestIds) &&
                Objects.equals(ownerTable, that.ownerTable) &&
                Objects.equals(attachmentId, that.attachmentId) &&
                Objects.equals(ownerIds, that.ownerIds) &&
                Objects.equals(meta, that.meta) &&
                Objects.equals(ttl, that.ttl) &&
                Objects.equals(toValid, that.toValid) &&
                Arrays.equals(attachment, that.attachment);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(requestIds, ownerTable, attachmentId, ownerIds, meta, ttl, toValid);
        result = 31 * result + Arrays.hashCode(attachment);
        return result;
    }

    public void setType(AttachmentResultHolder.Type type) {
        this.type = type;
    }
}
