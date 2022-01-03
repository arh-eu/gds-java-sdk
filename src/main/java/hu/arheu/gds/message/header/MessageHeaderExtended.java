
package hu.arheu.gds.message.header;

import java.util.List;


public interface MessageHeaderExtended extends MessageHeaderBase {

    int NUMBER_OF_FIELDS = 8;

    @Override
    default MessageHeaderBase asBaseMessageHeader() throws ClassCastException {
        throw new ClassCastException(
                String.format("%s cannot be cast to %s",
                        this.getClass().getSimpleName(),
                        MessageHeaderBase.class.getSimpleName()));
    }

    default MessageHeaderExtended asExtendedMessageHeader()  {
        return this;
    }

    @Override
    default Type getMessageHeaderType() {
        return Type.EXTENDED;
    }

    @Override
    default boolean isBaseMessageHeader() {
        return false;
    }

    @Override
    default boolean isExtendedMessageHeader() {
        return true;
    }

    List<String> getGdsNodeNamesInRequests();

    List<String> getGdsNodeNamesInResponses();

    String getConnectionHash();

    Boolean isServeOnTheSameConnection();

    Integer getHopCount();

    Long getInputBufferSize();

    Long getOutputBufferSize();

    Boolean isSecured();

    @Override
    default int getNumberOfPublicElements() {
        return MessageHeaderBase.super.getNumberOfPublicElements() + NUMBER_OF_FIELDS;
    }
}
