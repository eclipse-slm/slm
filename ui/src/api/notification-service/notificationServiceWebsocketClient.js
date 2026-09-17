import SockJS from 'sockjs-client/dist/sockjs';
import Stomp from 'stomp-websocket';
import {useNotificationStore} from "@/stores/notificationStore";
import { globals } from '@/main.ts';

const API_URL = '/notification-service';

class NotificationServiceWebsocketClient {
  socket = null

  static processNotificationMessage(response) {
      const content = JSON.parse(response.body);
      const notificationStore = useNotificationStore();
      notificationStore.processIncomingNotification(content);
  }

  static resolveUserId() {
      return globals.$keycloak?.tokenParsed?.sub
          || globals.$keycloak?.subject
          || globals.$keycloak?.keycloak?.subject
  }

  connect () {
      this.socket = new SockJS(API_URL + `/ws/notifications`);
      this.stompClient = Stomp.over(this.socket);

      this.stompClient.reconnect_delay = 5000;
      // remove for stump debug messages
      this.stompClient.debug = (message) => {
          // console.log(message)
      };
      const userId = NotificationServiceWebsocketClient.resolveUserId()
      const subscriptionName = `/topic/notifications/${userId}`

      this.stompClient.connect(
         {
             Authorization: 'Bearer ' + globals.$keycloak.token,
             Issuer: `${globals.$keycloak.keycloak.authServerUrl}/realms/${globals.$keycloak.keycloak.realm}`
         },
         () => {
             this.stompClient.subscribe('/user/topic/notifications', NotificationServiceWebsocketClient.processNotificationMessage);

             // direct topic fallback for convertAndSend("/topic/notifications/{userId}", ...)
             if (userId) {
                 this.stompClient.subscribe(subscriptionName, NotificationServiceWebsocketClient.processNotificationMessage);
             } else {
                 console.warn('Unable to resolve websocket user id for /topic/notifications/{userId} subscription')
             }
         },
         (error) => {
             console.error('Notification websocket connection failed', error)
         });
  }

   disconnect () {
     this.socket.close();
  }
}

export default new NotificationServiceWebsocketClient();
