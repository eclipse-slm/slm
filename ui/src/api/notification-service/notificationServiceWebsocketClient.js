import SockJS from 'sockjs-client'
import Stomp from 'stomp-websocket'
import store from '@/store/store'
import Vue from 'vue'

const API_URL = '/notification-service'

class NotificationServiceWebsocketClient {
  socket = null

  connect () {
    this.socket = new SockJS(API_URL + `/ws/notifications/?access_token=${Vue.prototype.$keycloak.token}`)
    this.stompClient = Stomp.over(this.socket)

    this.stompClient.reconnect_delay = 5000
    // remove for stump debug messages
    this.stompClient.debug = (message) => {
      // console.log(message)
    }
    const subscriptionName = `/topic/notifications/${Vue.prototype.$keycloak.subject}`

    this.stompClient.connect(
        {
          Authorization: 'Bearer ' + Vue.prototype.$keycloak.token,
          Realm: 'fabos',
        },
        () => {
          this.stompClient.subscribe(subscriptionName, response => {
            const content = JSON.parse(response.body)
            store.dispatch('processIncomingNotification', content)
          })
        })
  }

   disconnect () {
     this.socket.close()
  }
}

export default new NotificationServiceWebsocketClient()
