package hu.arh.gds.client.websocket;

public interface BinaryMessageListener {
    void onMessageReceived(byte[] message);
}
