import notificationServiceRestApi from '@/api/notification-service/notificationServiceRestApi'
import Vue from 'vue'

export default {
  state: {
    notifications: [],
    notifications_unread: [],
  },

  getters: {
    notifications: (state) => {
      return state.notifications
    },
    notifications_unread: (state) => {
      return state.notifications.filter((note) => note.read === false)
    },
  },

  actions: {
    getNotifications (context) {
      notificationServiceRestApi.getNotifications().then(notifications => {
        context.commit('SET_NOTIFICATIONS', notifications)
      })
    },

    processIncomingNotification (context, notification) {
      context.dispatch('getNotifications')

      if (notification.category !== undefined) {
        Vue.$toast.info(notification.text)
        switch (notification.category) {
          case 'Jobs':
            // console.log('Update jobs store')
            context.dispatch('updateJobsStore')
            context.dispatch('updateResourcesStore')
            context.dispatch('updateServicesStore')
            break
          case 'Resources':
            context.dispatch('updateResourcesStore')
            break
          case 'Services':
            context.dispatch('updateServicesStore')
            break
          default:
            console.log(`Update ${notification.category} store`)
            break
        }
      }
    },
    markAsRead (context) {
      const unreadNotifications = context.state.notifications_unread

      notificationServiceRestApi.markNotificationsAsRead(unreadNotifications).then(response => {
        context.dispatch('getNotifications')
      })
    },
  },

  mutations: {
    SET_NOTIFICATIONS (state, notifications) {
      state.notifications = notifications
      state.notifications_unread = notifications.filter(n => n.read === false)
    },
  },
}
