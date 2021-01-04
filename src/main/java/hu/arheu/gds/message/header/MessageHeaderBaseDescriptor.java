/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.arheu.gds.message.header;


/**
 * Interface used for describing a Message Header part.
 *
 * @author oliver.nagy
 */
public interface MessageHeaderBaseDescriptor {

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
    Long getCreateTime();

    /**
     * Returns when the request/response represented by this message was sent.
     *
     * @return the request time
     */
    Long getRequestTime();

    /**
     * Returns whether this message is fragmented or not.
     * This field also indicates the values for the{@link MessageHeaderBaseDescriptor#getFirstFragment()},
     * {@link MessageHeaderBaseDescriptor#getLastFragment()}, {@link MessageHeaderBaseDescriptor#getOffset()}
     * and {@link MessageHeaderBaseDescriptor#getFullDataSize()} fields as they cannot be {@code null} if this is set to
     * {@code true}.
     * <p>
     * Otherwise all of them should be {@code null}.
     *
     * @return {@code true} if the message is fragmented, {@code false} otherwise.
     */

    Boolean getIsFragmented();


    /**
     * Value is only interpretable if the {@link MessageHeaderBaseDescriptor#getIsFragmented()} is {@code true}.
     * Otherwise this always returns {@code null}.
     *
     * @return {@code true} if this is the first fragment of the message, {@code false} if it is not.
     * {@code null} if the message is not fragmented.
     */
    Boolean getFirstFragment();


    /**
     * Value is only interpretable if the {@link MessageHeaderBaseDescriptor#getIsFragmented()} is {@code true}.
     * Otherwise this always returns {@code null}.
     *
     * @return {@code true} if this is the last fragment of the message, {@code false} if it is not.
     * {@code null} if the message is not fragmented.
     */
    Boolean getLastFragment();


    /**
     * Value is only interpretable if the {@link MessageHeaderBaseDescriptor#getIsFragmented()} is {@code true}.
     * Otherwise this always returns {@code null}.
     *
     * @return the byte offset of this message in the full message.
     * {@code null} if the message is not fragmented.
     */
    Long getOffset();


    /**
     * Value is only interpretable if the {@link MessageHeaderBaseDescriptor#getIsFragmented()} is {@code true}.
     * Otherwise this always returns {@code null}.
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
