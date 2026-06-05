package com.aether.supervisor;

/**
 * Represents the possible directives a supervisor can issue when a child actor fails.
 */
public enum SupervisorDirective {
    /**
     * Restart the failed child actor.
     */
    RESTART,

    /**
     * Stop the failed child actor without restarting it.
     */
    STOP,

    /**
     * Escalate the failure to the parent supervisor.
     */
    ESCALATE,

    /**
     * Terminate the failed child actor and all its children.
     */
    TERMINATE
}
