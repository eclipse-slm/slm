package org.eclipse.slm.resource_management.common.resources;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class ResourceEvent extends ApplicationEvent{
    private final UUID resourceId;
    private final Operation operation;

    public enum Operation {
        CREATE, DELETE
    }

    ResourceEvent(Object object, UUID resourceId, Operation operation) {
        super(object);
        this.resourceId = resourceId;
        this.operation = operation;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public Operation getOperation() { return operation; }

    public boolean isCreate() {
        return this.operation == Operation.CREATE;
    }

    public boolean isDelete() {
        return this.operation == Operation.DELETE;
    }

}
