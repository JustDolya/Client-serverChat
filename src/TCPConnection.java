import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPConnection {
    private final Socket socket;
    private Thread thread;
    private final TCPConnectionListener eventListener;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public TCPConnection(TCPConnectionListener eventListener, String ipAddress, int port) throws IOException {
        this(eventListener, new Socket(ipAddress, port));
    }

    public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        thread = new Thread(() -> {
            try {
                eventListener.onConnectionReady(TCPConnection.this);
                while (!thread.isInterrupted()) {
                    // Получаем объект Message
                    Message message = (Message) in.readObject();
                    eventListener.onReceiveMessage(TCPConnection.this, message);
                }
            } catch (IOException | ClassNotFoundException e) {
                eventListener.onException(TCPConnection.this,(IOException) e);
            } finally {
                eventListener.onDisconnect(TCPConnection.this);
            }
        });
        thread.start();
    }

    public synchronized void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            eventListener.onException(this, e);
            disconnect();
        }
    }

    private synchronized void disconnect() {
        thread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(this, e);
        }
    }
}
