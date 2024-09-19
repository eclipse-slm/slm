package org.eclipse.slm.service_management.service.rest.service_offerings;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class ServiceOfferingVersionEvent extends ApplicationEvent {
    private final UUID serviceOfferingVersionId;
    private final Operation operation;

    public enum Operation {
        CREATE, UPDATE, DELETE
    }

    ServiceOfferingVersionEvent(Object object, UUID serviceOfferingVersionId, Operation operation) {
        super(object);
        this.serviceOfferingVersionId = serviceOfferingVersionId;
        this.operation = operation;
    }

    public UUID getServiceOfferingVersionId() {
        return serviceOfferingVersionId;
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
