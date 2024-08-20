package org.eclipse.slm.resource_management.service.rest.resources;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class ResourceEvent extends ApplicationEvent{
    private final UUID id;
    private final Operation operation;

    public enum Operation {
        CREATE, DELETE
    }

    ResourceEvent(Object object, UUID id, Operation operation) {
        super(object);
        this.id = id;
        this.operation = operation;
    }

    public UUID getId() {
        return id;
    }

    public Operation getOperation() { return operation; }

    public boolean isCreate() {
        return this.operation == Operation.CREATE;
    }

    public boolean isDelete() {
        return this.operation == Operation.DELETE;
    }

}
