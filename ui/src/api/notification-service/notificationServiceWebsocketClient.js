import SockJS from 'sockjs-client'
import Stomp from 'stomp-websocket'
import {app} from "@/main";
import {useNotificationStore} from "@/stores/notificationStore";

const API_URL = '/notification-service'

class NotificationServiceWebsocketClient {
  socket = null

  connect () {
      this.socket = new SockJS(API_URL + `/ws/notifications`)
      this.stompClient = Stomp.over(this.socket)

      this.stompClient.reconnect_delay = 5000
      // remove for stump debug messages
      this.stompClient.debug = (message) => {
          // console.log(message)
      }
      const subscriptionName = `/topic/notifications/${app.config.globalProperties.$keycloak.subject}`

      this.stompClient.connect(
          {
              Authorization: 'Bearer ' + app.config.globalProperties.$keycloak.token,
              Issuer: `${app.config.globalProperties.$keycloak.keycloak.authServerUrl}/realms/${app.config.globalProperties.$keycloak.keycloak.realm}`
          },
          () => {
              this.stompClient.subscribe(subscriptionName, response => {
                  const content = JSON.parse(response.body)
                  const notificationStore = useNotificationStore();
                  notificationStore.processIncomingNotification(content);
              })
          })
  }

   disconnect () {
     this.socket.close()
  }
}

export default new NotificationServiceWebsocketClient()
