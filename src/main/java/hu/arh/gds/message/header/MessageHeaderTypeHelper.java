package hu.arh.gds.message.header;

public abstract class MessageHeaderTypeHelper {

    public abstract MessageHeaderType getMessageHeaderType();

    public MessageHeaderBase asBaseMessageHeader() throws ClassCastException {
        throw new ClassCastException(
                String.format("%s cannot be cast to %s",
                        this.getClass().getSimpleName(),
                        MessageHeaderBase.class.getSimpleName()));
    }

    public boolean isBaseMessageHeader() {
        return false;
    }
}
