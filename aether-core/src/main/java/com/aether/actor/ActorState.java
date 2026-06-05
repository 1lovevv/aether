package com.aether.actor;

/**
 * Represents the lifecycle states of an Actor.
 */
public enum ActorState {
    CREATED,
    STARTING,
    RUNNING,
    STOPPING,
    STOPPED,
    FAILED,
    TERMINATED
}
