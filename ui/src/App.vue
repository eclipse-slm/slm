<template>
  <router-view />
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
      NotificationServiceWebsocketClient.connect()
      this.$store.dispatch('updateCatalogStore')
      this.$store.dispatch('initServiceStore')
      this.$store.dispatch('getVirtualResourceProviders')
      this.$store.dispatch('getServiceHosters')
      this.$store.dispatch('getServiceInstanceGroups')
      this.$store.dispatch('getDeploymentCapabilities')
      this.$store.dispatch('getResourcesFromBackend')
      this.$store.dispatch('getResourceAASFromBackend')
      this.$store.dispatch('getLocations')
      this.$store.dispatch('getProfiler')
      this.$store.dispatch('getCluster')
      this.$store.dispatch('getNotifications')
      this.$store.dispatch('getClusterTypes')
    },
    destroyed () {
      NotificationServiceWebsocketClient.disconnect()
    },
  }
</script>

<style lang="scss">
.v-toast__text {
  font-family: "Roboto", sans-serif;
}
</style>
