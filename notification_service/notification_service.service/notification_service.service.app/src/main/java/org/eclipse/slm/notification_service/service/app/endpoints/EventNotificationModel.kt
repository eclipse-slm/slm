package org.eclipse.slm.notification_service.service.app.endpoints

import org.eclipse.slm.notification_service.service.app.messaging.EventNotification
import org.eclipse.slm.notification_service.service.app.messaging.EVENT_CLASS
import org.eclipse.slm.notification_service.service.app.messaging.resources.DiscoveryJobEventNotification
import org.eclipse.slm.notification_service.service.app.messaging.resources.CapabilityJobEventNotification
import org.eclipse.slm.notification_service.service.app.messaging.resources.FirmwareUpdateJobEventNotification
import org.eclipse.slm.notification_service.service.app.messaging.resources.ResourceEventNotification
import org.eclipse.slm.notification_service.service.app.messaging.services.ServiceInstanceEventNotification

class EventNotificationModel(

    var eventClass: EVENT_CLASS,
    var eventNotification: EventNotification? = null,
    var resourceEventNotification: ResourceEventNotification? = null,
    var capabilityJobEventNotification: CapabilityJobEventNotification? = null,
    var discoveryEventNotification: DiscoveryJobEventNotification? = null,
    var firmwareUpdateJobEventNotification: FirmwareUpdateJobEventNotification? = null,
    var serviceInstanceEventNotification: ServiceInstanceEventNotification? = null,

)