
package hu.arheu.gds.message.data;

public interface MessageData0Connection extends MessageData {

    Boolean getServeOnTheSameConnection();

    String getClusterName();

    Integer getProtocolVersionNumber();

    Boolean getFragmentationSupported();

    Long getFragmentTransmissionUnit();

    String getPassword();

    @Override
    default MessageData0Connection asConnectionMessageData0() throws ClassCastException {
        return this;
    }

    @Override
    default boolean isConnectionMessageData0() {
        return true;
    }

    @Override
    default MessageDataType getMessageDataType() {
        return MessageDataType.CONNECTION_0;
    }

    @Override
    default int getNumberOfPublicElements() {
        return 6;
    }
}
