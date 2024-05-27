import SockJS from 'sockjs-client'
import Stomp from 'stomp-websocket'
import store from '@/store/store'
import Vue from 'vue'
import {app} from "@/main";

const API_URL = '/notification-service'

class NotificationServiceWebsocketClient {
  socket = null

  connect () {
      this.socket = new SockJS(API_URL + `/ws/notifications/?access_token=${app.config.globalProperties.$keycloak.token}`)
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
              Realm: 'fabos',
          },
          () => {
              this.stompClient.subscribe(subscriptionName, response => {
                  const content = JSON.parse(response.body)
                  app.config.globalProperties.$store.dispatch('processIncomingNotification', content)
              })
          })
  }

   disconnect () {
     this.socket.close()
  }
}

export default new NotificationServiceWebsocketClient()
