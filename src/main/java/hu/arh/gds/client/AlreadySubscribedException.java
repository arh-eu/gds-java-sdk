package hu.arh.gds.client;

public class AlreadySubscribedException extends Exception {

    public AlreadySubscribedException() {
        super();
    }

    public AlreadySubscribedException(String message) {
        super(message);
    }
}
