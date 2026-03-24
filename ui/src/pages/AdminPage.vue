<template>
  <div>
    <!-- Version !-->
    <versions-overview />

    <!-- Service Categories !-->
    <service-categories-table />

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

import VersionsOverview from '@/components/admin/VersionsOverview'
import ServiceCategoriesTable from '@/components/service_offerings/ServiceCategoriesTable'
import ServiceVendorsDevelopersTable from '@/components/service_vendors/ServiceVendorDevelopersTable'
import ServiceVendorTable from '@/components/service_vendors/ServiceVendorTable'
import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";

export default {
    name: 'AdminPage',
    components: { ServiceCategoriesTable, ServiceVendorTable, ServiceVendorsDevelopersTable, VersionsOverview, },
    setup(){
      const serviceOfferingsStore = useServiceOfferingsStore();
      return {serviceOfferingsStore};
    },
    data () {
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
      ServiceVendorsTableHeaders () {
        return [
          { title: 'Id', value: 'serviceVendorId', sortable: true },
          { title: 'Name', value: 'serviceVendorName', sortable: true },
          { title: 'Description', value: 'serviceVendorDescription', sortable: false },
          { title: 'Actions', value: 'serviceVendorActions', sortable: false },
        ]
      },
    },
    created () {
      this.serviceOfferingsStore.getServiceVendors();
    },
    methods: {
      onServiceVendorClicked (serviceVendor) {
        this.selectedServiceVendor = serviceVendor
      },
    },
  }
</script>

<style scoped>
</style>
