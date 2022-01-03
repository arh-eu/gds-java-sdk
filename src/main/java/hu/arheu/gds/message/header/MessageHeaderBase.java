
package hu.arheu.gds.message.header;


public interface MessageHeaderBase extends MessageHeader {

    int NUMBER_OF_FIELDS = 10;

    @Override
    default Type getMessageHeaderType() {
        return Type.BASE;
    }

    @Override
    default MessageHeaderBase asBaseMessageHeader() throws ClassCastException {
        return this;
    }

    @Override
    default boolean isBaseMessageHeader() {
        return true;
    }

    @Override
    default int getNumberOfPublicElements() {
        return NUMBER_OF_FIELDS;
    }
}
