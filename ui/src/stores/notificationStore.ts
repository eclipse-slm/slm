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

    processIncomingNotification (notification: any) {
      const $toast = useToast();

      this.getNotifications();

      if (notification.category !== undefined) {
        $toast.info(notification.text)
        const resourceDevicesStore = useResourceDevicesStore();
        const resourceClustersStore = useResourceClustersStore();
        const serviceInstancesStore = useServiceInstancesStore();
        const discoveryStore = useDiscoveryStore();
        switch (notification.category) {
          case 'JOBS':
            const jobsStore = useJobsStore();

            jobsStore.updateStore();
            resourceDevicesStore.updateStore();
            resourceClustersStore.updateStore();
            serviceInstancesStore.updateStore();
            break
          case 'RESOURCES':
            if (notification.target === 'DISCOVERY') {
              discoveryStore.updateDiscoveryStore();
            }
            else {
              resourceDevicesStore.updateStore();
              resourceClustersStore.updateStore();
            }
            break
          case 'SERVICES':
            serviceInstancesStore.updateStore();
          case 'PROJECTS':
            serviceInstancesStore.updateStore();
            break
          default:
            console.debug(`Update ${notification.category} store`)
            break
        }
      }
    },
    markAsRead () {
      NotificationServiceClient.api.setReadOfNotifications(true, this.notifications_unread)
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
