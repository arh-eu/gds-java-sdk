package hu.arh.gds.message.data.impl;

import hu.arh.gds.message.util.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.*;

import hu.arh.gds.message.data.AttachmentResultHolder;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

public class AttachmentResultHolderImpl implements AttachmentResultHolder {
    private final List<String> requestIds;
    private final String ownerTable;
    private final String attachmentId;
    private List<String> ownerIds;
    private String meta;
    private Long ttl;
    private Long toValid;
    private byte[] attachment;

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
        checkContent(this);
    }

    public AttachmentResultHolderImpl(List<String> requestIds,
                                      String ownerTable,
                                      String attachmentId) {
        this.requestIds = requestIds;
        this.ownerTable = ownerTable;
        this.attachmentId = attachmentId;
        checkContentAttachmentResponseAck(this);
    }

    private static void checkContent(AttachmentResultHolder attachmentResultHolder) {
        ExceptionHelper.requireNonNullValue(attachmentResultHolder.getRequestIds(), AttachmentResultHolderImpl.class.getSimpleName(), "requestIds");
        ExceptionHelper.requireNonNullValue(attachmentResultHolder.getOwnerTable(), AttachmentResultHolderImpl.class.getSimpleName(), "ownerTable");
        ExceptionHelper.requireNonNullValue(attachmentResultHolder.getAttachmentId(), AttachmentResultHolderImpl.class.getSimpleName(), "attachmentId");
        ExceptionHelper.requireNonNullValue(attachmentResultHolder.getOwnerIds(), AttachmentResultHolderImpl.class.getSimpleName(), "ownerIds");
    }

    private static void checkContentAttachmentResponseAck(AttachmentResultHolder attachmentResultHolder) {
        ExceptionHelper.requireNonNullValue(attachmentResultHolder.getRequestIds(), AttachmentResultHolderImpl.class.getSimpleName(), "requestIds");
        ExceptionHelper.requireNonNullValue(attachmentResultHolder.getOwnerTable(), AttachmentResultHolderImpl.class.getSimpleName(), "ownerTable");
        ExceptionHelper.requireNonNullValue(attachmentResultHolder.getAttachmentId(), AttachmentResultHolderImpl.class.getSimpleName(), "attachmentId");
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
        int mapHeaderSize = 0;
        if(requestIds != null) {
            mapHeaderSize++;
        }
        if(ownerTable != null) {
            mapHeaderSize++;
        }
        if(attachmentId != null) {
            mapHeaderSize++;
        }
        if(ownerIds != null) {
            mapHeaderSize++;
        }
        if(meta != null) {
            mapHeaderSize++;
        }
        if(ttl != null) {
            mapHeaderSize++;
        }
        if(toValid != null) {
            mapHeaderSize++;
        }
        if(attachment != null) {
            mapHeaderSize++;
        }
        return mapHeaderSize;
    }

    @Override
    public void packContent(MessageBufferPacker packer) throws IOException {
        WriterHelper.packMapHeader(packer, getMapHeaderSize());
        if(requestIds != null) {
            WriterHelper.packValue(packer, "requestids");
            WriterHelper.packStringValues(packer, requestIds);
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
            WriterHelper.packStringValues(packer, ownerIds);
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

    public static AttachmentResultHolder unpackContent(MessageUnpacker unpacker, AttachmentResultHolderType attachmentResultHolderType) throws ReadException, IOException {
        if (!ReaderHelper.nextExpectedValueTypeIsNil(unpacker, ValueType.MAP, "result",
                AttachmentResultHolderImpl.class.getSimpleName())) {
            int mapHeaderSize = ReaderHelper.unpackMapHeader(unpacker, null, "result",
                    AttachmentResultHolderImpl.class.getSimpleName());

            List<String> requestIdsTemp = null;
            String ownerTableTemp = null;
            String attachmentIdTemp = null;
            List<String> ownerIdsTemp = null;
            String metaTemp = null;
            Long ttlTemp = null;
            Long toValidTemp = null;
            byte[] attachmentTemp = null;

            for (int i = 0; i < mapHeaderSize; ++i) {
                String key = ReaderHelper.unpackStringValue(unpacker, "result map key",
                        AttachmentResultHolderImpl.class.getSimpleName());

                if(attachmentResultHolderType.equals(AttachmentResultHolderType.ATTACHMENT_RESPONSE_ACK)) {
                    switch (key) {
                        case "requestids":
                            requestIdsTemp = ReaderHelper.unpackStringValues(unpacker, null, "requestids",
                                    "request ids", AttachmentResultHolderImpl.class.getSimpleName());
                            break;
                        case "ownertable":
                            ownerTableTemp = ReaderHelper.unpackStringValue(unpacker, "owner table",
                                    AttachmentResultHolderImpl.class.getSimpleName());
                            break;
                        case "attachmentid":
                            attachmentIdTemp = ReaderHelper.unpackStringValue(unpacker, "attachment id",
                                    AttachmentResultHolderImpl.class.getSimpleName());
                            break;
                        default:
                            throw new ReadException(
                                    String.format("Map key value [%s] does not match exptected value: " +
                                                    "[%s]/[%s]/[%s]",
                                            key,
                                            "requestids",
                                            "ownertable",
                                            "attachmentid"));
                    }
                } else {
                    switch (key) {
                        case "requestids":
                            requestIdsTemp = ReaderHelper.unpackStringValues(unpacker, null, "requestids",
                                    "request ids", AttachmentResultHolderImpl.class.getSimpleName());
                            break;
                        case "ownertable":
                            ownerTableTemp = ReaderHelper.unpackStringValue(unpacker, "owner table",
                                    AttachmentResultHolderImpl.class.getSimpleName());
                            break;
                        case "attachmentid":
                            attachmentIdTemp = ReaderHelper.unpackStringValue(unpacker, "attachment id",
                                    AttachmentResultHolderImpl.class.getSimpleName());
                            break;
                        case "ownerids":
                            ownerIdsTemp = ReaderHelper.unpackStringValues(unpacker, null, "ownerids",
                                    "owner ids", AttachmentResultHolderImpl.class.getSimpleName());
                            break;
                        case "meta":
                            metaTemp = ReaderHelper.unpackStringValue(unpacker, "meta",
                                    AttachmentResultHolderImpl.class.getSimpleName());
                            break;
                        case "ttl":
                            ttlTemp = ReaderHelper.unpackLongValue(unpacker, "ttl",
                                    AttachmentResultHolderImpl.class.getSimpleName());
                            break;
                        case "to_valid":
                            toValidTemp = ReaderHelper.unpackLongValue(unpacker, "to valid",
                                    AttachmentResultHolderImpl.class.getSimpleName());
                            break;
                        case "attachment":
                            attachmentTemp = ReaderHelper.unpackBinary(unpacker, "attachment",
                                    AttachmentResultHolderImpl.class.getSimpleName());
                            break;
                        default:
                            throw new ReadException(
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

            AttachmentResultHolder attachmentResultHolderTemp;

            if(attachmentResultHolderType.equals(AttachmentResultHolderType.ATTACHMENT_RESPONSE_ACK)) {
                attachmentResultHolderTemp = new AttachmentResultHolderImpl(requestIdsTemp, ownerTableTemp,
                        attachmentIdTemp);
                checkContentAttachmentResponseAck(attachmentResultHolderTemp);
            } else {
                attachmentResultHolderTemp = new AttachmentResultHolderImpl(requestIdsTemp, ownerTableTemp,
                        attachmentIdTemp, ownerIdsTemp, metaTemp, ttlTemp, toValidTemp, attachmentTemp);
                checkContent(attachmentResultHolderTemp);
            }
            return attachmentResultHolderTemp;
        } else {
            unpacker.unpackNil();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttachmentResultHolderImpl)) return false;
        AttachmentResultHolderImpl that = (AttachmentResultHolderImpl) o;
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
}
