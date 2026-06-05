package com.aether.serialization;

import com.aether.message.Message;

/**
 * Registry for message serializers.
 * <p>
 * Provides a central place to register and retrieve serializers for different
 * message formats. Supports content-type negotiation and fallback strategies.
 * <p>
 * This class is thread-safe.
 *
 * @see MessageSerializer
 * @see JsonMessageSerializer
 */
public interface SerializerRegistry {

    /**
     * Registers a serializer for a specific content type.
     *
     * @param contentType  the content type (e.g., "application/json")
     * @param serializer   the serializer to register
     */
    void register(String contentType, MessageSerializer serializer);

    /**
     * Retrieves a serializer for the given content type.
     *
     * @param contentType the content type
     * @return the serializer, or null if not found
     */
    MessageSerializer getSerializer(String contentType);

    /**
     * Returns the default serializer.
     *
     * @return the default serializer
     */
    MessageSerializer getDefaultSerializer();

    /**
     * Sets the default serializer.
     *
     * @param serializer the default serializer
     */
    void setDefaultSerializer(MessageSerializer serializer);
}
