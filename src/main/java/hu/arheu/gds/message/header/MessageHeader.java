package hu.arheu.gds.message.header;

import hu.arheu.gds.message.data.MessageDataType;
import hu.arheu.gds.message.util.GdsMessagePart;

public interface MessageHeader extends GdsMessagePart {

    enum Type {
        BASE,
        EXTENDED
    }

    Type getMessageHeaderType();

    byte[] getBinary();

    default MessageHeaderBase asBaseMessageHeader() throws ClassCastException {
        throw new ClassCastException(
                String.format("%s cannot be cast to %s",
                        this.getClass().getSimpleName(),
                        MessageHeaderBase.class.getSimpleName()));
    }

    default MessageHeaderExtended asExtendedMessageHeader() throws ClassCastException {
        throw new ClassCastException(
                String.format("%s cannot be cast to %s",
                        this.getClass().getSimpleName(),
                        MessageHeaderExtended.class.getSimpleName()));
    }

    default boolean isBaseMessageHeader() {
        return false;
    }

    default boolean isExtendedMessageHeader() {
        return false;
    }


    /**
     * Returns the specified username in the message
     *
     * @return the username
     */
    String getUserName();

    /**
     * Returns the unique ID associated with this message.
     *
     * @return the message ID
     */
    String getMessageId();

    /**
     * Returns the time this message was created.
     *
     * @return the creation time
     */
    long getCreateTime();

    /**
     * Returns when the request/response represented by this message was sent.
     *
     * @return the request time
     */
    long getRequestTime();

    /**
     * Returns whether this message is fragmented or not.
     * This field also indicates the values for the{@link MessageHeader#getFirstFragment()},
     * {@link MessageHeader#getLastFragment()}, {@link MessageHeader#getOffset()}
     * and {@link MessageHeader#getFullDataSize()} fields as they cannot be {@code null} if this is set to
     * {@code true}.
     * <p>
     * Otherwise, all of them should be {@code null}.
     *
     * @return {@code true} if the message is fragmented, {@code false} otherwise.
     */

    Boolean getIsFragmented();


    /**
     * Value is only interpretable if the {@link MessageHeader#getIsFragmented()} is {@code true}.
     * Otherwise, this always returns {@code null}.
     *
     * @return {@code true} if this is the first fragment of the message, {@code false} if it is not.
     * {@code null} if the message is not fragmented.
     */
    Boolean getFirstFragment();


    /**
     * Value is only interpretable if the {@link MessageHeader#getIsFragmented()} is {@code true}.
     * Otherwise, this always returns {@code null}.
     *
     * @return {@code true} if this is the last fragment of the message, {@code false} if it is not.
     * {@code null} if the message is not fragmented.
     */
    Boolean getLastFragment();


    /**
     * Value is only interpretable if the {@link MessageHeader#getIsFragmented()} is {@code true}.
     * Otherwise, this always returns {@code null}.
     *
     * @return the byte offset of this message in the full message.
     * {@code null} if the message is not fragmented.
     */
    Long getOffset();


    /**
     * Value is only interpretable if the {@link MessageHeader#getIsFragmented()} is {@code true}.
     * Otherwise, this always returns {@code null}.
     *
     * @return the length of the full message (restored from the fragments)
     * {@code null} if the message is not fragmented.
     */
    Long getFullDataSize();

    /**
     * Indicates the data type of this message specified in the data part of the full message.
     *
     * @return The {@link MessageDataType} contained in this message.
     */
    MessageDataType getDataType();
}
