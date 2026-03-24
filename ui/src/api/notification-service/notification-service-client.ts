import {NotificationRestControllerApi} from "@/api/notification-service/client";

class NotificationServiceClient {
    api = new NotificationRestControllerApi(undefined, "/notification-service");
}

export default new NotificationServiceClient();