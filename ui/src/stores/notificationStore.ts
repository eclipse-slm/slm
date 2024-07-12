import notificationServiceRestApi from '@/api/notification-service/notificationServiceRestApi'
import {app} from "@/main";
import {defineStore} from "pinia";
import {useJobsStore} from "@/stores/jobsStore";
import {useResourcesStore} from "@/stores/resourcesStore";
import {useServicesStore} from "@/stores/servicesStore";

export interface NotificationStoreState{
  notifications_: any[],
  notifications_unread_: any[],
}

export const useNotificationStore = defineStore('notificationStore', {
  persist: true,
  state: (): NotificationStoreState => ({
    notifications_: [],
    notifications_unread_: [],
  }),
  getters: {
    notifications: (state) => {
      return state.notifications_
    },
    notifications_unread: (state) => {
      return state.notifications_.filter((note) => note.read === false)
    },
  },
  actions: {

    setNotifications(notifications: any[]){
      this.notifications_ = notifications
      this.notifications_unread_ = notifications.filter(n => n.read === false)
    },

    getNotifications () {
      notificationServiceRestApi.getNotifications().then(notifications => {
        this.setNotifications(notifications);
      })
    },

    processIncomingNotification (notification: any) {
      this.getNotifications();

      if (notification.category !== undefined) {
        app.config.globalProperties.$toast.info(notification.text)
        const resourcesStore = useResourcesStore();
        const servicesStore = useServicesStore();
        switch (notification.category) {
          case 'Jobs':
            // console.log('Update jobs store')
              const jobsStore = useJobsStore();

              jobsStore.updateJobsStore().then();
              resourcesStore.updateResourcesStore();
              servicesStore.updateServicesStore();
            break
          case 'Resources':
            resourcesStore.updateResourcesStore();
            break
          case 'Services':
            servicesStore.updateServicesStore();
            break
          default:
            console.log(`Update ${notification.category} store`)
            break
        }
      }
    },
    markAsRead () {
      const unreadNotifications = this.notifications_unread_

      notificationServiceRestApi.markNotificationsAsRead(unreadNotifications).then(response => {
        this.getNotifications()
      })
    },
  },
});
