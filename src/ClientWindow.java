import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalTime;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {

    private static final String IP_ADDR = "localhost";
    private static final int PORT = 8189;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    private static final String timePattern = "[" + LocalTime.now().getHour() + ":" + LocalTime.now().getMinute() + "] ";

    private final JPanel chatPanel = new JPanel();
    private final JTextField fieldNickname = new JTextField("Гость");
    private final JTextField fieldInput = new JTextField();
    private final JButton sendImageButton = new JButton("Отправить изображение");
    private TCPConnection connection;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientWindow::new);
    }

    private ClientWindow() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(chatPanel);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(fieldNickname, BorderLayout.WEST);
        bottomPanel.add(fieldInput, BorderLayout.CENTER);
        bottomPanel.add(sendImageButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // События для текстового поля и кнопки
        fieldInput.addActionListener(this);
        sendImageButton.addActionListener(e -> sendImage());

        setVisible(true);

        try {
            connection = new TCPConnection(this, IP_ADDR, PORT);
        } catch (IOException e) {
            printMessage("Connection exception: " + e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = fieldInput.getText();
        if (msg.isEmpty()) return;
        fieldInput.setText(null);
        connection.sendMessage(new Message(Message.Type.TEXT, timePattern + fieldNickname.getText() + ": " + msg));
    }

    private void sendImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите изображение для отправки");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files", "jpg", "png", "jpeg"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                byte[] imageBytes = Files.readAllBytes(file.toPath());
                connection.sendMessage(new Message(Message.Type.IMAGE, imageBytes));
                printMessage("Изображение отправлено: " + file.getName());
            } catch (IOException ex) {
                printMessage("Ошибка отправки изображения: " + ex);
            }
        }
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMessage("Connection ready...");
    }

    @Override
    public void onReceiveMessage(TCPConnection tcpConnection, Message message) {
        switch (message.getType()) {
            case TEXT:
                printMessage(message.getText());
                break;
            case IMAGE:
                // Отображаем изображение вместе с ником
                displayImage(message.getImageBytes(), timePattern + fieldNickname.getText());
                break;
        }
    }

    private void displayImage(byte[] imageBytes, String sender) {
        ImageIcon icon = new ImageIcon(imageBytes);
        Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        // Добавляем ник отправителя перед изображением
        JLabel senderLabel = new JLabel(sender + ":");
        JLabel imageLabel = new JLabel(scaledIcon);
        imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // При нажатии на изображение открывается в полном размере
        imageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showFullImage(imageBytes);
            }
        });

        // Добавляем ник и изображение в чат
        addMessageComponent(senderLabel);
        addMessageComponent(imageLabel);
    }


    private void showFullImage(byte[] imageBytes) {
        ImageIcon fullIcon = new ImageIcon(imageBytes);
        JLabel fullImageLabel = new JLabel(fullIcon);
        JOptionPane.showMessageDialog(this, fullImageLabel, "Просмотр изображения", JOptionPane.PLAIN_MESSAGE);
    }

    private void addMessageComponent(JComponent component) {
        SwingUtilities.invokeLater(() -> {
            chatPanel.add(component);
            chatPanel.revalidate();
            chatPanel.repaint();
        });
    }

    private synchronized void printMessage(String message) {
        JLabel label = new JLabel(message);
        addMessageComponent(label);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMessage("Connection close");
    }

    @Override
    public void onException(TCPConnection tcpConnection, IOException e) {
        printMessage("Connection exception: " + e);
    }
}

