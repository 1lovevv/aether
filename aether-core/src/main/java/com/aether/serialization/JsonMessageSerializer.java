package com.aether.serialization;

import com.aether.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Base64;

/**
 * A simple JSON-like text serializer using Java's built-in serialization as a fallback.
 * <p>
 * This is a basic implementation for the MVP. In production, you would use
 * a proper JSON library like Jackson or Gson, or a binary format like Protobuf.
 * <p>
 * For the MVP, this implementation uses Java Serialization as the default strategy
 * with Base64 encoding for text representation. Future versions will support
 * pluggable JSON libraries.
 *
 * @see MessageSerializer
 */
public class JsonMessageSerializer implements MessageSerializer {

    private static final Logger logger = LoggerFactory.getLogger(JsonMessageSerializer.class);
    private static final String CONTENT_TYPE = "application/x-java-serialized-object";

    @Override
    public byte[] serialize(Message message) throws SerializationException {
        if (message == null) {
            throw new SerializationException("Cannot serialize null message");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(message);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Failed to serialize message of type " + message.getClass().getName(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Message> T deserialize(byte[] bytes, Class<T> type) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            throw new SerializationException("Cannot deserialize null or empty bytes");
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            Object object = ois.readObject();
            if (!type.isInstance(object)) {
                throw new SerializationException("Deserialized object is not of expected type. Expected: " +
                        type.getName() + ", Actual: " + object.getClass().getName());
            }
            return (T) object;
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationException("Failed to deserialize message to type " + type.getName(), e);
        }
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public boolean supports(Class<? extends Message> type) {
        // Java serialization supports any Serializable class
        return java.io.Serializable.class.isAssignableFrom(type);
    }

    /**
     * Serializes a message to a Base64-encoded string.
     * Useful for text-based transports.
     *
     * @param message the message to serialize
     * @return Base64-encoded string
     * @throws SerializationException if serialization fails
     */
    public String serializeToString(Message message) throws SerializationException {
        return Base64.getEncoder().encodeToString(serialize(message));
    }

    /**
     * Deserializes a Base64-encoded string back into a message.
     *
     * @param base64 the Base64-encoded string
     * @param type   the expected message type
     * @param <T>    the message type
     * @return the deserialized message
     * @throws SerializationException if deserialization fails
     */
    public <T extends Message> T deserializeFromString(String base64, Class<T> type) throws SerializationException {
        return deserialize(Base64.getDecoder().decode(base64), type);
    }
}
