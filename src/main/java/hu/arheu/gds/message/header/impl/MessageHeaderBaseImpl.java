
package hu.arheu.gds.message.header.impl;

import hu.arheu.gds.message.MessagePart;
import hu.arheu.gds.message.data.MessageDataType;
import hu.arheu.gds.message.errors.ReadException;
import hu.arheu.gds.message.errors.ValidationException;
import hu.arheu.gds.message.errors.WriteException;
import hu.arheu.gds.message.header.MessageHeaderBase;
import hu.arheu.gds.message.util.ReaderHelper;
import hu.arheu.gds.message.util.Validator;
import hu.arheu.gds.message.util.WriterHelper;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.util.Objects;


public class MessageHeaderBaseImpl extends MessagePart implements MessageHeaderBase {

    private String userName;
    private String messageId;
    private long createTime;
    private long requestTime;
    private Boolean isFragmented;
    private Boolean firstFragment;
    private Boolean lastFragment;
    private Long offset;
    private Long fullDataSize;
    private MessageDataType dataType;

    /**
     * Do not remove, as it's needed for the serialization through {@link java.io.Externalizable}
     */
    public MessageHeaderBaseImpl() {
    }

    public MessageHeaderBaseImpl(String userName,
                                 String messageId,
                                 long createTime,
                                 long requestTime,
                                 Boolean isFragmented,
                                 Boolean firstFragment,
                                 Boolean lastFragment,
                                 Long offset,
                                 Long fullDataSize,
                                 MessageDataType dataType) throws ValidationException {

        this.userName = userName;
        this.messageId = messageId;
        this.createTime = createTime;
        this.requestTime = requestTime;
        this.isFragmented = isFragmented;
        this.firstFragment = firstFragment;
        this.lastFragment = lastFragment;
        this.offset = offset;
        this.fullDataSize = fullDataSize;
        this.dataType = dataType;

        checkContent();
        serialize();
    }

    public MessageHeaderBaseImpl(MessageHeaderBase other) throws ValidationException {
        this.userName = other.getUserName();
        this.messageId = other.getMessageId();
        this.createTime = other.getCreateTime();
        this.requestTime = other.getRequestTime();
        this.isFragmented = other.getIsFragmented();
        this.firstFragment = other.getFirstFragment();
        this.lastFragment = other.getLastFragment();
        this.offset = other.getOffset();
        this.fullDataSize = other.getFullDataSize();
        this.dataType = other.getDataType();
        //other is checked and serialized
    }

    public MessageHeaderBaseImpl(byte[] binary) throws ReadException, ValidationException {
        deserialize(binary);
    }

    public MessageHeaderBaseImpl(byte[] binary, boolean isFullMessage) throws ReadException, ValidationException {
        deserialize(binary, isFullMessage);
    }

    @Override
    public void checkContent() {

        Validator.requireNonNullValue(this.userName, this.getClass().getSimpleName(), "userName");
        Validator.requireNonNullValue(this.messageId, this.getClass().getSimpleName(), "messageId");
        Validator.requireNonNullValue(this.createTime, this.getClass().getSimpleName(), "createTime");
        Validator.requireNonNullValue(this.requestTime, this.getClass().getSimpleName(), "requestTime");
        Validator.requireNonNullValue(this.isFragmented, this.getClass().getSimpleName(),
                "isFragmented");
        Validator.requireNonNullValue(this.dataType, this.getClass().getSimpleName(), "dataType");

        if (this.isFragmented) {
            Validator.requireNonNullValue(this.firstFragment, this.getClass().getSimpleName(),
                    "firstFragment");
            Validator.requireNonNullValue(this.lastFragment, this.getClass().getSimpleName(),
                    "lastFragment");
            Validator.requireNonNullValue(this.offset, this.getClass().getSimpleName(), "offset");
            Validator.requireNonNullValue(this.fullDataSize, this.getClass().getSimpleName(),
                    "fullDataSize");
        } else {
            Validator.requireNullValue(this.firstFragment, this.getClass().getSimpleName(), "firstFragment");
            Validator.requireNullValue(this.lastFragment, this.getClass().getSimpleName(), "lastFragment");
            Validator.requireNullValue(this.fullDataSize, this.getClass().getSimpleName(), "fullDataSize");
        }
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getMessageId() {
        return messageId;
    }

    @Override
    public long getRequestTime() {
        return this.requestTime;
    }

    @Override
    public long getCreateTime() {
        return this.createTime;
    }

    @Override
    public Boolean getIsFragmented() {
        return isFragmented;
    }

    @Override
    public Boolean getFirstFragment() {
        return firstFragment;
    }

    @Override
    public Boolean getLastFragment() {
        return lastFragment;
    }

    @Override
    public Long getOffset() {
        return offset;
    }

    @Override
    public Long getFullDataSize() {
        return fullDataSize;
    }

    @Override
    public MessageDataType getDataType() {
        return dataType;
    }

    @Override
    protected final MessagePart.Type getMessagePartType() {
        return MessagePart.Type.HEADER;
    }

    @Override
    public void packContentTo(MessageBufferPacker packer) throws WriteException {
        WriterHelper.packValue(packer, this.getUserName());
        WriterHelper.packValue(packer, this.getMessageId());
        WriterHelper.packValue(packer, this.getCreateTime());
        WriterHelper.packValue(packer, this.getRequestTime());
        WriterHelper.packValue(packer, this.getIsFragmented());
        WriterHelper.packValue(packer, this.getFirstFragment());
        WriterHelper.packValue(packer, this.getLastFragment());
        WriterHelper.packValue(packer, this.getOffset());
        WriterHelper.packValue(packer, this.getFullDataSize());
        WriterHelper.packValue(packer, this.getDataType() == null ? null : getDataType().getValue());
    }

    @Override
    public void unpackContentFrom(MessageUnpacker unpacker) throws ReadException, ValidationException {
        this.userName = (ReaderHelper.unpackStringValue(unpacker, "username",
                this.getClass().getSimpleName()));
        this.messageId = (ReaderHelper.unpackStringValue(unpacker, "message id",
                this.getClass().getSimpleName()));
        this.createTime = (ReaderHelper.unpackNotNullLongValue(unpacker, "create time",
                this.getClass().getSimpleName()));
        this.requestTime = (ReaderHelper.unpackNotNullLongValue(unpacker, "request time",
                this.getClass().getSimpleName()));
        this.isFragmented = (ReaderHelper.unpackBooleanValue(unpacker, "is fragmented",
                this.getClass().getSimpleName()));
        this.firstFragment = (ReaderHelper.unpackBooleanValue(unpacker, "first fragment",
                this.getClass().getSimpleName()));
        this.lastFragment = (ReaderHelper.unpackBooleanValue(unpacker, "last fragment",
                this.getClass().getSimpleName()));
        this.offset = (ReaderHelper.unpackLongValue(unpacker, "offset", this.getClass().getSimpleName()));
        this.fullDataSize = (ReaderHelper.unpackLongValue(unpacker, "full data size",
                this.getClass().getSimpleName()));
        this.dataType = (MessageDataType.valueOf(ReaderHelper.unpackIntegerValue(unpacker, "data type",
                this.getClass().getSimpleName())));
        checkContent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageHeaderBaseImpl that = (MessageHeaderBaseImpl) o;
        return Objects.equals(userName, that.userName)
                && Objects.equals(messageId, that.messageId)
                && Objects.equals(createTime, that.createTime)
                && Objects.equals(requestTime, that.requestTime)
                && Objects.equals(isFragmented, that.isFragmented)
                && Objects.equals(firstFragment, that.firstFragment)
                && Objects.equals(lastFragment, that.lastFragment)
                && Objects.equals(offset, that.offset)
                && Objects.equals(fullDataSize, that.fullDataSize)
                && dataType == that.dataType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName,
                messageId,
                createTime,
                requestTime,
                isFragmented,
                firstFragment,
                lastFragment,
                offset,
                fullDataSize,
                dataType);
    }
}
