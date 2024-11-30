import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

@SuppressWarnings("InfiniteLoopStatement")
public class ChatServer implements TCPConnectionListener {

    public static void main(String[] args) {
        new ChatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    private ChatServer() {
        System.out.println("Server running...");
        try(ServerSocket serverSocket = new ServerSocket(8189)) {
            while(true) {
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e) {
                    System.out.println("TCPConnection exception: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
    }

    @Override
    public synchronized void onReceiveMessage(TCPConnection tcpConnection, Message message) {
        switch (message.getType()) {
            case TEXT:
                sendToAllConnections(message);
                break;
            case IMAGE:
                saveImageToFile(message.getImageBytes());
                sendToAllConnections(message);
                break;
        }
    }

    private void saveImageToFile(byte[] imageBytes) {
        try (FileOutputStream fos = new FileOutputStream("received_image.jpg")) {
            fos.write(imageBytes);
        } catch (IOException e) {
            System.out.println("Error saving image: " + e);
        }
    }

    private void sendToAllConnections(Message message) {
        for (TCPConnection connection : connections) {
            connection.sendMessage(message);
        }
    }


    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
    }

    @Override
    public void onException(TCPConnection tcpConnection, IOException e) {
        System.out.println("TCPConnection exception: " + e);
    }
}