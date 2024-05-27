<template>
  <router-view>
  </router-view>
</template>

<script>
  import updateToken from '@/utils/updateToken'
  import NotificationServiceWebsocketClient from '@/api/notification-service/notificationServiceWebsocketClient'
  import {app} from "@/main";

  export default {
    name: 'App',
    computed: {
      app() {
        return app
      }
    },
    watch: {
      $route () {
        updateToken()
      },
    },
    created () {
    },
    mounted () {

    },
    unmounted () {
      NotificationServiceWebsocketClient.disconnect()
    },
  }
</script>

<style lang="scss">
.v-toast__text {
  font-family: "Roboto", sans-serif;
}
</style>
