package com.aether.serialization;

import com.aether.message.Message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link SerializerRegistry}.
 * <p>
 * Thread-safe implementation using a concurrent hash map.
 *
 * @see SerializerRegistry
 */
public class DefaultSerializerRegistry implements SerializerRegistry {

    private final Map<String, MessageSerializer> serializers = new ConcurrentHashMap<>();
    private volatile MessageSerializer defaultSerializer;

    public DefaultSerializerRegistry() {
        // Register the default JSON serializer
        JsonMessageSerializer jsonSerializer = new JsonMessageSerializer();
        this.defaultSerializer = jsonSerializer;
        serializers.put(jsonSerializer.getContentType(), jsonSerializer);
    }

    @Override
    public void register(String contentType, MessageSerializer serializer) {
        if (contentType == null || contentType.isEmpty()) {
            throw new IllegalArgumentException("Content type cannot be null or empty");
        }
        if (serializer == null) {
            throw new IllegalArgumentException("Serializer cannot be null");
        }
        serializers.put(contentType, serializer);
    }

    @Override
    public MessageSerializer getSerializer(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return defaultSerializer;
        }
        return serializers.getOrDefault(contentType, defaultSerializer);
    }

    @Override
    public MessageSerializer getDefaultSerializer() {
        return defaultSerializer;
    }

    @Override
    public void setDefaultSerializer(MessageSerializer serializer) {
        if (serializer == null) {
            throw new IllegalArgumentException("Default serializer cannot be null");
        }
        this.defaultSerializer = serializer;
    }
}
