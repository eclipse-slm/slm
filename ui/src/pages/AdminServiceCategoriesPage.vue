<template>
  <div>
    <!-- Service Categories !-->
    <service-categories-table />
  </div>
</template>
<script>

import ServiceCategoriesTable from '@/components/service_offerings/ServiceCategoriesTable'
import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";

export default {
  name: 'AdminServiceCategoriesPage',
  components: { ServiceCategoriesTable, },
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