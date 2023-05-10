import axios from 'axios'

class NotificationServiceRestApi {
    API_URL = '/notification-service'

    async getNotifications () {
        return axios.get(this.API_URL + '/notifications')
            .catch((error) => {
                console.log(error)
            })
            .then(response => {
                return response.data
            })
    }

    async markNotificationsAsRead (unreadNotifications) {
        return axios.patch(
            this.API_URL + '/notifications/read',
            unreadNotifications,
            {
                params: { read: true },
            },
        ).then(response => {
        })
    }
}

export default new NotificationServiceRestApi()
