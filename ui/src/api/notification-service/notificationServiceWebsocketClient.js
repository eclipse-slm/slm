import SockJS from 'sockjs-client/dist/sockjs';
import Stomp from 'stomp-websocket';
import {useNotificationStore} from "@/stores/notificationStore";
import { globals } from '@/app/appInstance';

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
             // The backend publishes every notification to both destinations, so subscribe to only one
             // of them, otherwise each notification is processed (and toasted) twice
             if (userId) {
                 this.stompClient.subscribe(subscriptionName, NotificationServiceWebsocketClient.processNotificationMessage);
             } else {
                 console.warn('Unable to resolve websocket user id, falling back to /user/topic/notifications subscription')
                 this.stompClient.subscribe('/user/topic/notifications', NotificationServiceWebsocketClient.processNotificationMessage);
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
