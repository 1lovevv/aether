package com.aether.actor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ActorPathValidator}.
 */
class ActorPathValidatorTest {

    @Test
    void testValidPaths() {
        assertTrue(ActorPathValidator.isValid("/"));
        assertTrue(ActorPathValidator.isValid("/user"));
        assertTrue(ActorPathValidator.isValid("/user/actor"));
        assertTrue(ActorPathValidator.isValid("/user/actor-1"));
        assertTrue(ActorPathValidator.isValid("/user/actor_1"));
        assertTrue(ActorPathValidator.isValid("/a/b/c/d/e"));
    }

    @Test
    void testInvalidPaths() {
        assertFalse(ActorPathValidator.isValid(null));
        assertFalse(ActorPathValidator.isValid(""));
        assertFalse(ActorPathValidator.isValid("user"));
        assertFalse(ActorPathValidator.isValid("/user/"));
        assertFalse(ActorPathValidator.isValid("/user//actor"));
        assertFalse(ActorPathValidator.isValid("/user actor"));
        assertFalse(ActorPathValidator.isValid("/user@actor"));
    }

    @Test
    void testValidateThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> ActorPathValidator.validate(null));
        assertThrows(IllegalArgumentException.class, () -> ActorPathValidator.validate(""));
        assertThrows(IllegalArgumentException.class, () -> ActorPathValidator.validate("user"));
    }

    @Test
    void testGetParentPath() {
        assertNull(ActorPathValidator.getParentPath("/"));
        assertEquals("/", ActorPathValidator.getParentPath("/user"));
        assertEquals("/user", ActorPathValidator.getParentPath("/user/actor"));
        assertEquals("/a/b", ActorPathValidator.getParentPath("/a/b/c"));
    }

    @Test
    void testGetName() {
        assertEquals("/", ActorPathValidator.getName("/"));
        assertEquals("user", ActorPathValidator.getName("/user"));
        assertEquals("actor", ActorPathValidator.getName("/user/actor"));
        assertEquals("c", ActorPathValidator.getName("/a/b/c"));
    }

    @Test
    void testMaxLength() {
        StringBuilder longPath = new StringBuilder("/");
        for (int i = 0; i < 300; i++) {
            longPath.append("a");
        }
        assertFalse(ActorPathValidator.isValid(longPath.toString()));
    }

    @Test
    void testMaxDepth() {
        StringBuilder deepPath = new StringBuilder("/a");
        for (int i = 0; i < 15; i++) {
            deepPath.append("/a");
        }
        assertFalse(ActorPathValidator.isValid(deepPath.toString()));
    }
}
