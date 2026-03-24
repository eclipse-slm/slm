<template>
  <router-view />
</template>

<script>
  import updateToken from '@/utils/updateToken'
  import NotificationServiceWebsocketClient from '@/api/notification-service/notificationServiceWebsocketClient'

  export default {
    name: 'App',
    data() {
      return {
        tokenRefreshTimer: null
      }
    },
    watch: {
      $route () {
        updateToken()
      },
    },
    created () {
      this.timer = setInterval(() => {
        updateToken()
      }, 60000)
    },
    mounted () {

    },
    unmounted () {
      NotificationServiceWebsocketClient.disconnect()
      clearInterval(this.timer)
    },
  }
</script>

<style lang="scss">
.v-toast__text {
  font-family: "Roboto", sans-serif;
}
</style>
