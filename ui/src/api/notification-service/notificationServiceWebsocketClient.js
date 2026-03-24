import SockJS from 'sockjs-client/dist/sockjs';
import Stomp from 'stomp-websocket';
import {useNotificationStore} from "@/stores/notificationStore";
import { globals } from '@/main.ts';

const API_URL = '/notification-service';

class NotificationServiceWebsocketClient {
  socket = null

  connect () {
      this.socket = new SockJS(API_URL + `/ws/notifications`);
      this.stompClient = Stomp.over(this.socket);

      this.stompClient.reconnect_delay = 5000;
      // remove for stump debug messages
      this.stompClient.debug = (message) => {
          // console.log(message)
      };
      const subscriptionName = `/topic/notifications/${globals.$keycloak.subject}`;

      this.stompClient.connect(
          {
              Authorization: 'Bearer ' + globals.$keycloak.token,
              Issuer: `${globals.$keycloak.keycloak.authServerUrl}/realms/${globals.$keycloak.keycloak.realm}`
          },
          () => {
              this.stompClient.subscribe(subscriptionName, response => {
                  const content = JSON.parse(response.body);
                  const notificationStore = useNotificationStore();
                  notificationStore.processIncomingNotification(content);
              });
          });
  }

   disconnect () {
     this.socket.close();
  }
}

export default new NotificationServiceWebsocketClient();
