<template>

  <div>
    <!-- Version !-->
    <versions-overview />
  </div>
</template>
<script>

import VersionsOverview from '@/components/admin/VersionsOverview'
import {useServicesStore} from "@/stores/servicesStore";

export default {
  name: 'AdminComponentsPage',
  components: { VersionsOverview, },
  setup(){
    const servicesStore = useServicesStore();
    return {servicesStore};
  },
  computed: {
    serviceVendors() {
      return this.servicesStore.serviceVendors
    },
  },
  created () {
    this.servicesStore.getServiceVendors();
  },
  methods: {
  },
}
</script>

<style scoped>

</style>