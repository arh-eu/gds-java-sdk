/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.arh.gds.message.header.impl;

import hu.arh.gds.message.*;
import hu.arh.gds.message.header.*;
import hu.arh.gds.message.util.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

/**
 * @author oliver.nagy
 */
public class MessageHeaderBaseImpl extends MessageHeaderBase {

    private String userName;
    private String messageId;
    private Long createTime;
    private Long requestTime;
    private Boolean isFragmented;
    private Boolean firstFragment;
    private Boolean lastFragment;
    private Long offset;
    private Long fullDataSize;
    private MessageDataType dataType;

    public MessageHeaderBaseImpl(boolean cache,
                                 String userName,
                                 String messageId,
                                 Long createTime,
                                 Long requestTime,
                                 Boolean isFragmented,
                                 Boolean firstFragment,
                                 Boolean lastFragment,
                                 Long offset,
                                 Long fullDataSize,
                                 MessageDataType dataType) throws IOException, ValidationException {

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
        this.cache = cache;

        checkContent();

        if (cache) {
            Serialize();
        }
    }

    public MessageHeaderBaseImpl(byte[] binary, boolean cache) throws IOException, ReadException, ValidationException {
        super(binary, cache);
    }

    public MessageHeaderBaseImpl(byte[] binary, boolean cache, boolean isFullMessage) throws IOException, ReadException, ValidationException {
        super(binary, cache, isFullMessage);
    }

    @Override
    protected void init() {
        this.typeHelper = new MessageHeaderTypeHelper() {
            @Override
            public MessageHeaderType getMessageHeaderType() {
                return MessageHeaderType.BASE;
            }
            @Override
            public MessageHeaderBase asBaseMessageHeader() {
                return MessageHeaderBaseImpl.this;
            }
            @Override
            public boolean isBaseMessageHeader() {
                return true;
            }
        };
    }

    @Override
    protected void checkContent() {

        ExceptionHelper.requireNonNullValue(this.userName, this.getClass().getSimpleName(), "userName");
        ExceptionHelper.requireNonNullValue(this.messageId, this.getClass().getSimpleName(), "messageId");
        ExceptionHelper.requireNonNullValue(this.createTime, this.getClass().getSimpleName(), "createTime");
        ExceptionHelper.requireNonNullValue(this.requestTime, this.getClass().getSimpleName(), "requestTime");
        ExceptionHelper.requireNonNullValue(this.isFragmented, this.getClass().getSimpleName(),
                "isFragmented");
        ExceptionHelper.requireNonNullValue(this.dataType, this.getClass().getSimpleName(), "dataType");

        if (this.isFragmented) {
            ExceptionHelper.requireNonNullValue(this.firstFragment, this.getClass().getSimpleName(),
                    "firstFragment");
            ExceptionHelper.requireNonNullValue(this.lastFragment, this.getClass().getSimpleName(),
                    "lastFragment");
            ExceptionHelper.requireNonNullValue(this.offset, this.getClass().getSimpleName(), "offset");
            ExceptionHelper.requireNonNullValue(this.fullDataSize, this.getClass().getSimpleName(),
                    "fullDataSize");
        } else {
            ExceptionHelper.requireNullValue(this.firstFragment, this.getClass().getSimpleName(), "firstFragment");
            ExceptionHelper.requireNullValue(this.lastFragment, this.getClass().getSimpleName(), "lastFragment");
            ExceptionHelper.requireNullValue(this.fullDataSize, this.getClass().getSimpleName(), "fullDataSize");
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
    public Long getRequestTime() {
        return this.requestTime;
    }

    @Override
    public Long getCreateTime() {
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
    protected MessagePartType getMessagePartType() {
        return MessagePartType.HEADER;
    }

    @Override
    protected void PackValues(MessageBufferPacker packer) throws IOException {

        //WriterHelper.packArrayHeader(packer, Globals.BASE_HEADER_FIELDS_NUMBER + Globals.DATA_FIELDS_NUMBER);

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
    protected void UnpackValues(MessageUnpacker unpacker) throws IOException, ReadException {

        //ReaderHelper.unpackArrayHeader(unpacker, null, null, null);

        this.userName = (ReaderHelper.unpackStringValue(unpacker, "username",
                this.getClass().getSimpleName()));
        this.messageId = (ReaderHelper.unpackStringValue(unpacker, "message id",
                this.getClass().getSimpleName()));
        this.createTime = (ReaderHelper.unpackLongValue(unpacker, "create time",
                this.getClass().getSimpleName()));
        this.requestTime = (ReaderHelper.unpackLongValue(unpacker, "request time",
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

        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        if (messageId != null ? !messageId.equals(that.messageId) : that.messageId != null) return false;
        if (isFragmented != null ? !isFragmented.equals(that.isFragmented) : that.isFragmented != null) return false;
        if (firstFragment != null ? !firstFragment.equals(that.firstFragment) : that.firstFragment != null)
            return false;
        if (lastFragment != null ? !lastFragment.equals(that.lastFragment) : that.lastFragment != null) return false;
        if (offset != null ? !offset.equals(that.offset) : that.offset != null) return false;
        if (fullDataSize != null ? !fullDataSize.equals(that.fullDataSize) : that.fullDataSize != null) return false;
        return dataType == that.dataType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, messageId, requestTime, createTime, isFragmented, firstFragment, lastFragment,
                offset, fullDataSize, dataType);
    }
}
