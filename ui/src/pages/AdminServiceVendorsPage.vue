<template>
  <div>
    <!-- Service Vendors !-->
    <service-vendor-table @serviceVendorClicked="onServiceVendorClicked" />

    <!-- Developers !-->
    <service-vendors-developers-table
      v-if="selectedServiceVendor != null"
      :service-vendor="selectedServiceVendor"
      @closed="selectedServiceVendor = null"
    />
  </div>
</template>

<script>


import ServiceVendorsDevelopersTable from '@/components/service_vendors/ServiceVendorDevelopersTable'
import ServiceVendorTable from '@/components/service_vendors/ServiceVendorTable'
import {useServicesStore} from "@/stores/servicesStore";

export default {
  name: 'AdminServiceVendorsPage',
  components: {ServiceVendorTable, ServiceVendorsDevelopersTable,},
  setup() {
    const servicesStore = useServicesStore();
    return {servicesStore};
  },
  data() {
    return {
      selectedServiceVendor: null,
      editServiceVendor: false,
      showCreateOrEditServiceVendorDialog: false,
    }
  },
  computed: {
    serviceVendors() {
      return this.servicesStore.serviceVendors
    },
  },
  created() {
    this.servicesStore.getServiceVendors();
  },
  methods: {
    onServiceVendorClicked(serviceVendor) {
      this.selectedServiceVendor = serviceVendor
    },
  },
}
</script>

<style scoped>
</style>
