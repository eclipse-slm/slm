package org.eclipse.slm.notification_service.service.rest.endpoints

import org.eclipse.slm.notification_service.model.EventNotification
import org.eclipse.slm.notification_service.model.EVENT_CLASS
import org.eclipse.slm.notification_service.model.resources.DiscoveryJobEventNotification
import org.eclipse.slm.notification_service.service.rest.messaging.resources.CapabilityJobEventNotification
import org.eclipse.slm.notification_service.service.rest.messaging.resources.FirmwareUpdateJobEventNotification
import org.eclipse.slm.notification_service.service.rest.messaging.resources.ResourceEventNotification
import org.eclipse.slm.notification_service.service.rest.messaging.resources.ServiceInstanceEventNotification

class EventNotificationModel(

    var eventClass: EVENT_CLASS,
    var eventNotification: EventNotification? = null,
    var resourceEventNotification: ResourceEventNotification? = null,
    var capabilityJobEventNotification: CapabilityJobEventNotification? = null,
    var discoveryEventNotification: DiscoveryJobEventNotification? = null,
    var firmwareUpdateJobEventNotification: FirmwareUpdateJobEventNotification? = null,
    var serviceInstanceEventNotification: ServiceInstanceEventNotification? = null,

)