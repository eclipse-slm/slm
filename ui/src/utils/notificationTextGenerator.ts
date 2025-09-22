import {
    EventNotification,
    EVENTCLASS,
    ResourceEventNotification,
    ResourceEventType,
    FirmwareUpdateJobEventNotification,
    DiscoveryJobEventNotification,
    CapabilityJobEventNotification,
    ServiceInstanceEventNotification, ServiceInstanceEventType
} from "@/api/notification-service/client";
import {useCapabilitiesStore} from "@/stores/capabilitiesStore";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";

class NotificationTextGenerator {

    static generateLocalizedText(eventNotification: EventNotification, t) {
        switch (eventNotification.type) {

            case EVENTCLASS.ResourceEvent: {
                const resourceEvent = eventNotification as unknown as ResourceEventNotification

                switch (resourceEvent.eventType) {
                    case ResourceEventType.Created: {
                        return `Resource '${resourceEvent.resource.hostname}' created`
                    }
                    case ResourceEventType.Deleted: {
                        return `Resource '${resourceEvent.resource.hostname}' deleted`
                    }
                    case ResourceEventType.Updated: {
                        return `Resource '${resourceEvent.resource.hostname}' updated`
                    }
                }
            }

            case EVENTCLASS.CapabilityJobEvent: {
                const capabilityJobEventNotification = eventNotification  as unknown as CapabilityJobEventNotification;

                const capabilitiesStore = useCapabilitiesStore();
                const capability = capabilitiesStore.capabilityById(capabilityJobEventNotification.capabilityJob.capabilityId);

                const resourceDevicesStore = useResourceDevicesStore();
                const resource = resourceDevicesStore.resourceById(capabilityJobEventNotification.capabilityJob.resourceId);

                return `Capability '${capability.name}' ${capabilityJobEventNotification.capabilityJob.state?.toLowerCase()} on device '${resource.hostname}'`
            }

            case EVENTCLASS.DiscoveryEvent: {
                const discoveryEventNotification = eventNotification  as unknown as DiscoveryJobEventNotification;
                return `Discovery job finished`
            }

            case EVENTCLASS.FirmwareUpdateJobEvent: {
                const firmwareUpdateJobEventNotification = eventNotification  as unknown as FirmwareUpdateJobEventNotification;
                const resourceDevicesStore = useResourceDevicesStore();
                const resource = resourceDevicesStore.resourceById(firmwareUpdateJobEventNotification.firmwareUpdateJob.resourceId);
                return `Firmware update job on device '${resource.hostname}' turned into state '${firmwareUpdateJobEventNotification.firmwareUpdateJob.state}'`
            }

            case EVENTCLASS.ServiceInstanceEvent: {
                const serviceInstanceEventNotification = eventNotification  as unknown as ServiceInstanceEventNotification;
                switch (serviceInstanceEventNotification.eventType) {
                    case ServiceInstanceEventType.Created: {
                        return `Service '${serviceInstanceEventNotification.serviceInstance.id}' created`
                    }
                    case ServiceInstanceEventType.Deleted: {
                        return `Service '${serviceInstanceEventNotification.serviceInstance.id}' deleted`
                    }
                    case ServiceInstanceEventType.Updated: {
                        return `Service '${serviceInstanceEventNotification.serviceInstance.id}' updated`
                    }
                }
            }

            default:
                return "Message not defined"
        }

        return `Unhandled notification event of type '${eventNotification.type}`
    }
}

export default NotificationTextGenerator;