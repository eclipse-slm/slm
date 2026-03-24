<template>
  <div>
    <!-- Version !-->
    <versions-overview />
  </div>
</template>
<script>

import VersionsOverview from '@/components/admin/VersionsOverview'
import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";

export default {
  name: 'AdminComponentsPage',
  components: { VersionsOverview, },
  setup(){
    const serviceOfferingsStore = useServiceOfferingsStore();
    return {serviceOfferingsStore};
  },
  computed: {
    serviceVendors() {
      return this.serviceOfferingsStore.serviceVendors
    },
  },
  created () {
    this.serviceOfferingsStore.getServiceVendors();
  },
  methods: {
  },
}
</script>

<style scoped>

</style>