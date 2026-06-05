package com.aether.serialization;

import com.aether.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link JsonMessageSerializer}.
 */
class JsonMessageSerializerTest {

    // Test message that implements Serializable (required for Java serialization)
    record TestMessage(String content, int value) implements Message, Serializable {}

    private JsonMessageSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new JsonMessageSerializer();
    }

    @Test
    void testSerializeAndDeserialize() {
        TestMessage original = new TestMessage("Hello, World!", 42);

        byte[] bytes = serializer.serialize(original);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        TestMessage deserialized = serializer.deserialize(bytes, TestMessage.class);
        assertNotNull(deserialized);
        assertEquals("Hello, World!", deserialized.content());
        assertEquals(42, deserialized.value());
    }

    @Test
    void testSerializeToString() {
        TestMessage original = new TestMessage("Test", 123);

        String base64 = serializer.serializeToString(original);
        assertNotNull(base64);
        assertFalse(base64.isEmpty());

        TestMessage deserialized = serializer.deserializeFromString(base64, TestMessage.class);
        assertNotNull(deserialized);
        assertEquals("Test", deserialized.content());
        assertEquals(123, deserialized.value());
    }

    @Test
    void testContentType() {
        assertEquals("application/x-java-serialized-object", serializer.getContentType());
    }

    @Test
    void testSupportsSerializable() {
        assertTrue(serializer.supports(TestMessage.class));
    }

    @Test
    void testSerializeNullThrowsException() {
        assertThrows(SerializationException.class, () -> serializer.serialize(null));
    }

    @Test
    void testDeserializeNullThrowsException() {
        assertThrows(SerializationException.class, () -> serializer.deserialize(null, TestMessage.class));
    }

    @Test
    void testDeserializeEmptyThrowsException() {
        assertThrows(SerializationException.class, () -> serializer.deserialize(new byte[0], TestMessage.class));
    }
}
