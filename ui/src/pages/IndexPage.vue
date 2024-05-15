<template>
  <v-app>
    <dashboard-core-app-bar />

    <dashboard-core-drawer />

    <dashboard-core-view />
  </v-app>
</template>

<script>
  import DashboardCoreAppBar from "@/components/core/AppBar.vue";
  import DashboardCoreDrawer from "@/components/core/Drawer.vue";
  import DashboardCoreView from "@/components/core/View.vue";

  export default {
    name: 'IndexPage',

    components: {
      DashboardCoreView,
      DashboardCoreDrawer,
      DashboardCoreAppBar
    },

    data: () => ({
      expandOnHover: false,
    }),
  }
</script>
