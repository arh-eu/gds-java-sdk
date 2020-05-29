package hu.arh.gds.client.websocket;

public interface BinaryMessageListener extends ConnectionStateListener {
    void onMessageReceived(byte[] message);
}
