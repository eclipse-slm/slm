<template>
  <div>
    <!-- Service Categories !-->
    <service-categories-table />
  </div>
</template>
<script>

import ServiceCategoriesTable from '@/components/service_offerings/ServiceCategoriesTable'
import {useServicesStore} from "@/stores/servicesStore";

export default {
  name: 'AdminServiceCategoriesPage',
  components: { ServiceCategoriesTable, },
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