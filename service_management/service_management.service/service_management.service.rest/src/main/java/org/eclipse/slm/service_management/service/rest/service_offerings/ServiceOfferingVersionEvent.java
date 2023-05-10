package org.eclipse.slm.service_management.service.rest.service_offerings;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class ServiceOfferingVersionEvent extends ApplicationEvent {
    private final UUID id;
    private final Operation operation;

    public enum Operation {
        CREATE, UPDATE, DELETE
    }

    ServiceOfferingVersionEvent(Object object, UUID id, Operation operation) {
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

    public boolean isUpdate() {
        return this.operation == Operation.UPDATE;
    }

    public boolean isDelete() {
        return this.operation == Operation.DELETE;
    }
}
