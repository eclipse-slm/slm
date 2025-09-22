import {defineStore} from "pinia";
import {useJobsStore} from "@/stores/jobsStore";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import {useResourceClustersStore} from "@/stores/resourceClustersStore";
import {useServiceInstancesStore} from "@/stores/serviceInstancesStore";
import NotificationServiceClient from "@/api/notification-service/notification-service-client";
import logRequestError from "@/api/restApiHelper";
import {useDiscoveryStore} from "@/stores/discoveryStore";
import ApiState from "@/api/apiState";
import {useToast} from "vue-toast-notification";
import {
  CapabilityJobEventNotification,
  EventNotification,
  EventType, FirmwareUpdateJobEventNotification,
  Notification,
  NotificationCategory,
  NotificationSubCategory, EVENTCLASS, ResourceEventNotification, ResourceEventType
} from "@/api/notification-service/client";
import NotificationTextGenerator from "@/utils/notificationTextGenerator";
import i18n from '@/utils/i18n';
import {Resource} from "@/api/resource-management/client";
import {useCapabilitiesStore} from "@/stores/capabilitiesStore";

export interface NotificationStoreState {
  apiState: number,
  notifications: any[],
}

export const useNotificationStore = defineStore('notificationStore', {
  persist: true,

  state: (): NotificationStoreState => ({
    apiState: ApiState.INIT,
    notifications: [],
  }),

  getters: {
    notifications_unread: (state) => {
      return state.notifications.filter((note) => note.read === false)
    },
  },

  actions: {
    getNotifications () {
      NotificationServiceClient.api.getNotifications().then(response => {
        this.notifications = response.data;
      }).catch(logRequestError);
    },
    addNotification (notification: Notification) {
      this.notifications.push(notification)
    },

    processIncomingNotification (eventNotification: EventNotification) {
      console.log(eventNotification)

      const $toast = useToast();
      // this.addNotification(notification)

      switch (eventNotification.type) {
        case EVENTCLASS.ResourceEvent: {
          const resourceDevicesStore = useResourceDevicesStore();
          const resourceEventNotification = eventNotification  as unknown as ResourceEventNotification;
          console.log(resourceEventNotification)
          switch (resourceEventNotification.eventType) {
            case ResourceEventType.Created: {
              resourceDevicesStore.addOrUpdateResource(resourceEventNotification.resource);
              break;
            }
            case ResourceEventType.Updated: {
              resourceDevicesStore.addOrUpdateResource(resourceEventNotification.resource);
              break;
            }
            case ResourceEventType.Deleted: {
              resourceDevicesStore.deleteResource(resourceEventNotification.resource.id)
              break;
            }
            default: {
              resourceDevicesStore.updateStore();
              break;
            }
          }
          $toast.info(NotificationTextGenerator.generateLocalizedText(eventNotification, i18n.global))
          break;
        }

        case EVENTCLASS.CapabilityJobEvent: {
          const capabilityJobEventNotification = eventNotification  as unknown as CapabilityJobEventNotification;

          const capabilitiesStore = useCapabilitiesStore();
          const resourceDevicesStore = useResourceDevicesStore();
          capabilitiesStore.updateStore().then(() => {
            resourceDevicesStore.getResourceById(capabilityJobEventNotification.capabilityJob.resourceId).then(() => {
              $toast.info(NotificationTextGenerator.generateLocalizedText(eventNotification, i18n.global))
            })
          })
          break;
        }

        case EVENTCLASS.DiscoveryEvent: {
          const discoveryStore = useDiscoveryStore();
          discoveryStore.updateDiscoveryStore();
          $toast.info(NotificationTextGenerator.generateLocalizedText(eventNotification, i18n.global))
          break;
        }

        case EVENTCLASS.FirmwareUpdateJobEvent: {
          const resourceDevicesStore = useResourceDevicesStore();
          const firmwareUpdateJobEventNotification = eventNotification  as unknown as FirmwareUpdateJobEventNotification;
          resourceDevicesStore.getFirmwareUpdateInformationOfResource(firmwareUpdateJobEventNotification.firmwareUpdateJob.resourceId);
          resourceDevicesStore.getFirmwareUpdateJobsOfResource(firmwareUpdateJobEventNotification.firmwareUpdateJob.resourceId)
          $toast.info(NotificationTextGenerator.generateLocalizedText(eventNotification, i18n.global))
          break;
        }

        case EVENTCLASS.ServiceInstanceEvent: {
          const serviceInstancesStore = useServiceInstancesStore();
          serviceInstancesStore.updateStore();
          $toast.info(NotificationTextGenerator.generateLocalizedText(eventNotification, i18n.global))
        }

        default: {
          console.debug("Unknown event notification type: ", eventNotification.type);
          break;
        }
      }
    },

    markAllAsRead () {
      const unreadNotificationIds = this.notifications_unread.map((note) => note.id);
      NotificationServiceClient.api.setReadOfNotifications(true, unreadNotificationIds)
          .then(response => {
        this.updateStore();
      }).catch(logRequestError)
    },

    async updateStore () {
      if (this.apiState === ApiState.INIT) {
        this.apiState = ApiState.LOADING;
      } else {
        this.apiState = ApiState.UPDATING;
      }

      return Promise.all([
        this.getNotifications(),
      ]).then(() => {
        this.apiState = ApiState.LOADED;
        console.log("notificationStore updated")
      }).catch((e) => {
        this.apiState = ApiState.ERROR;
        console.log("Failed to update notificationStore: ", e)
      });
    },
  },
});
