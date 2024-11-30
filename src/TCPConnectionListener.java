import java.io.IOException;

public interface TCPConnectionListener {
    void onConnectionReady(TCPConnection tcpConnection);
    void onReceiveMessage(TCPConnection tcpConnection, Message message);
    void onDisconnect(TCPConnection tcpConnection);
    void onException(TCPConnection tcpConnection, IOException e);
}
