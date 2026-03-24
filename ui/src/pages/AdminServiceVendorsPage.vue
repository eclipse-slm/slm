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
import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";

export default {
  name: 'AdminServiceVendorsPage',
  components: {ServiceVendorTable, ServiceVendorsDevelopersTable,},
  setup() {
    const serviceOfferingsStore = useServiceOfferingsStore();
    return {serviceOfferingsStore};
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
      return this.serviceOfferingsStore.serviceVendors
    },
  },
  created() {
    this.serviceOfferingsStore.getServiceVendors();
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
