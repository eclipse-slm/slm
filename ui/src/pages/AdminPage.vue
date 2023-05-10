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
  import { mapGetters } from 'vuex'
  import VersionsOverview from '@/components/admin/VersionsOverview'
  import ServiceCategoriesTable from '@/components/service_offerings/ServiceCategoriesTable'
  import ServiceVendorsDevelopersTable from '@/components/service_vendors/ServiceVendorDevelopersTable'
  import ServiceVendorTable from '@/components/service_vendors/ServiceVendorTable'

  export default {
    name: 'AdminPage',
    components: { ServiceCategoriesTable, ServiceVendorTable, ServiceVendorsDevelopersTable, VersionsOverview, },
    data () {
      return {
        selectedServiceVendor: null,
        editServiceVendor: false,
        showCreateOrEditServiceVendorDialog: false,
      }
    },
    computed: {
      ...mapGetters([
        'serviceVendors',
      ]),
      ServiceVendorsTableHeaders () {
        return [
          { text: 'Id', value: 'serviceVendorId', sortable: true },
          { text: 'Name', value: 'serviceVendorName', sortable: true },
          { text: 'Description', value: 'serviceVendorDescription', sortable: false },
          { text: 'Actions', value: 'serviceVendorActions', sortable: false },
        ]
      },
    },
    created () {
      this.$store.dispatch('getServiceVendors')
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
