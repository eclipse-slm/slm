<template>
  <div class="Site">
    <v-main class="Site-content">
      <router-view />
    </v-main>
    <dashboard-core-footer />
  </div>
</template>

<script>
  export default {
    name: 'DashboardCoreView',

    components: {
      DashboardCoreFooter: () => import('./Footer'),
    },
  }
</script>

<style scoped>
.Site {
  display: flex;
  min-height: 100vh;
  flex-direction: column;
}

.Site-content {
  flex: 1;
}
</style>
