package com.aether.serialization;

import com.aether.message.Message;

/**
 * SPI for serializing and deserializing actor messages.
 * <p>
 * Implementations can support various formats such as JSON, Protocol Buffers,
 * Java Serialization, or custom binary formats. The framework uses this interface
 * to enable transparent message serialization for features like:
 * <ul>
 *   <li>Persistent mailboxes (message storage)</li>
 *   <li>Remote actor communication (distributed actors)</li>
 *   <li>Message logging and auditing</li>
 * </ul>
 * <p>
 * Implementations must be thread-safe.
 *
 * @see JsonMessageSerializer
 */
public interface MessageSerializer {

    /**
     * Serializes a message to a byte array.
     *
     * @param message the message to serialize
     * @return the serialized message as bytes
     * @throws SerializationException if serialization fails
     */
    byte[] serialize(Message message) throws SerializationException;

    /**
     * Deserializes a byte array back into a message.
     *
     * @param bytes the serialized message
     * @param type  the expected message type
     * @param <T>   the message type
     * @return the deserialized message
     * @throws SerializationException if deserialization fails
     */
    <T extends Message> T deserialize(byte[] bytes, Class<T> type) throws SerializationException;

    /**
     * Returns the content type or format identifier for this serializer.
     * Examples: "application/json", "application/x-java-serialized-object", "application/x-protobuf"
     *
     * @return the content type string
     */
    String getContentType();

    /**
     * Checks if this serializer supports the given message type.
     *
     * @param type the message type to check
     * @return true if supported
     */
    boolean supports(Class<? extends Message> type);
}
