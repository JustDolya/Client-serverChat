import java.io.Serializable;

public class Message implements Serializable {
    public enum Type {
        TEXT,
        IMAGE
    }

    private final Type type;
    private final String text;
    private final byte[] imageBytes;

    public Message(Type type, String text) {
        this.type = type;
        this.text = text;
        this.imageBytes = null;
    }

    public Message(Type type, byte[] imageBytes) {
        this.type = type;
        this.text = null;
        this.imageBytes = imageBytes;
    }

    public Type getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }
}
