package com.aether.supervisor;

import com.aether.actor.Actor;
import com.aether.actor.ActorState;
import com.aether.mailbox.Mailbox;
import com.aether.spi.ActorRef;
import com.aether.spi.ThreadScheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A supervisor actor that manages child actors and handles their failures
 * according to a configured {@link SupervisorStrategy}.
 * <p>
 * For the MVP, the supervisor always returns {@link SupervisorDirective#RESTART}
 * when a child fails. Full restart rate limiting and backoff will be added later.
 */
public class SupervisorActor extends Actor {

    /**
     * Represents a child actor entry managed by this supervisor.
     */
    public static class ChildActor {
        private final Class<? extends Actor> actorClass;
        private final String name;
        private volatile ActorRef ref;
        private volatile ActorState state;
        private final AtomicInteger restartCount;

        public ChildActor(Class<? extends Actor> actorClass, String name) {
            this.actorClass = actorClass;
            this.name = name;
            this.state = ActorState.CREATED;
            this.restartCount = new AtomicInteger(0);
        }

        public Class<? extends Actor> getActorClass() {
            return actorClass;
        }

        public String getName() {
            return name;
        }

        public ActorRef getRef() {
            return ref;
        }

        public void setRef(ActorRef ref) {
            this.ref = ref;
        }

        public ActorState getState() {
            return state;
        }

        public void setState(ActorState state) {
            this.state = state;
        }

        public int getRestartCount() {
            return restartCount.get();
        }

        public int incrementRestartCount() {
            return restartCount.incrementAndGet();
        }

        public void resetRestartCount() {
            restartCount.set(0);
        }
    }

    private final SupervisorStrategy strategy;
    private final Map<String, ChildActor> children;

    public SupervisorActor(String path, Mailbox mailbox, ThreadScheduler scheduler) {
        this(path, mailbox, scheduler, SupervisorStrategy.defaults());
    }

    public SupervisorActor(String path, Mailbox mailbox, ThreadScheduler scheduler, SupervisorStrategy strategy) {
        super(path, mailbox, scheduler);
        this.strategy = strategy;
        this.children = new ConcurrentHashMap<>();
    }

    /**
     * Registers a child actor to be supervised.
     *
     * @param actorClass the class of the child actor
     * @param name       the unique name of the child actor
     * @return the registered child actor entry
     */
    public ChildActor supervise(Class<? extends Actor> actorClass, String name) {
        ChildActor child = new ChildActor(actorClass, name);
        children.put(name, child);
        return child;
    }

    /**
     * Handles a failure reported by a child actor.
     * Decides the appropriate directive and acts on it.
     *
     * @param childRef the child actor that failed
     * @param cause    the cause of the failure
     * @return the directive that was applied
     */
    public SupervisorDirective handleFailure(ActorRef childRef, Throwable cause) {
        SupervisorDirective directive = decideDirective(childRef, cause);

        switch (directive) {
            case RESTART:
                restartChild(childRef, cause);
                break;
            case STOP:
                stopChild(childRef, cause);
                break;
            case ESCALATE:
                escalateFailure(childRef, cause);
                break;
            case TERMINATE:
                terminateChild(childRef, cause);
                break;
            default:
                throw new IllegalStateException("Unexpected directive: " + directive);
        }

        return directive;
    }

    /**
     * Decides the directive to apply for a given child failure.
     * <p>
     * For the MVP, this always returns {@link SupervisorDirective#RESTART}.
     */
    protected SupervisorDirective decideDirective(ActorRef childRef, Throwable cause) {
        return SupervisorDirective.RESTART;
    }

    /**
     * Restarts the failed child actor.
     * Stub for MVP - full implementation will create a new instance.
     */
    protected void restartChild(ActorRef childRef, Throwable cause) {
        ChildActor child = findChildByRef(childRef);
        if (child != null) {
            child.incrementRestartCount();
            child.setState(ActorState.STARTING);
        }
    }

    /**
     * Stops the failed child actor.
     * Stub for MVP.
     */
    protected void stopChild(ActorRef childRef, Throwable cause) {
        ChildActor child = findChildByRef(childRef);
        if (child != null) {
            child.setState(ActorState.STOPPING);
        }
    }

    /**
     * Escalates the failure to the parent supervisor.
     * Stub for MVP.
     */
    protected void escalateFailure(ActorRef childRef, Throwable cause) {
        // MVP: no parent supervisor yet; just mark as failed
        ChildActor child = findChildByRef(childRef);
        if (child != null) {
            child.setState(ActorState.FAILED);
        }
    }

    /**
     * Terminates the failed child actor and all its children.
     * Stub for MVP.
     */
    protected void terminateChild(ActorRef childRef, Throwable cause) {
        ChildActor child = findChildByRef(childRef);
        if (child != null) {
            child.setState(ActorState.TERMINATED);
            children.remove(child.getName());
        }
    }

    /**
     * Returns the strategy used by this supervisor.
     */
    public SupervisorStrategy getStrategy() {
        return strategy;
    }

    /**
     * Returns an unmodifiable view of the children managed by this supervisor.
     */
    public Map<String, ChildActor> getChildren() {
        return Map.copyOf(children);
    }

    private ChildActor findChildByRef(ActorRef ref) {
        for (ChildActor child : children.values()) {
            if (child.getRef() != null && child.getRef().equals(ref)) {
                return child;
            }
        }
        return null;
    }
}
