<template>
  <router-view>
  </router-view>
</template>

<script>
  import updateToken from '@/utils/updateToken'
  import NotificationServiceWebsocketClient from '@/api/notification-service/notificationServiceWebsocketClient'

  export default {
    name: 'App',
    watch: {
      $route () {
        updateToken()
      },
    },
    created () {
      this.$store.dispatch('getUserDetails')
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
