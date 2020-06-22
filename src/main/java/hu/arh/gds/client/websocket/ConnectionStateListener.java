package hu.arh.gds.client.websocket;

public interface ConnectionStateListener {
    void onConnected();
    void onConnectionFailed(String reason);
    void onDisconnected();
}
