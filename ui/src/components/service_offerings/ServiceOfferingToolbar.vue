<template>
  <v-toolbar
    id="ServiceHeaderFilterRow"
    flat
    height="75px"
  >
    <!-- Search field to filter by Service Offering Name -->
    <v-text-field
      v-model="filterServiceOfferingName"
      class="mr-10"
      prepend-inner-icon="mdi-magnify"
      :placeholder="$t('labels.SearchLabel')"
      single-line
      variant="outlined"
      density="compact"
      rounded
      hide-details
      @update:modelValue="filterServices()"
    />

    <!-- Dropdown to filter by Service Offering Category -->
    <v-select
      v-model="filterServiceOfferingCategoryIds"
      class="mr-10"
      :items="serviceOfferingCategories"
      :label="$t('labels.CategoryFilterLabel')"
      :menu-props="{ closeOnContentClick: true } "
      item-title="name"
      item-value="id"
      closable-chips
      density="compact"
      variant="outlined"
      hide-details
      multiple
      @update:modelValue="filterServices()"
    />
    <!-- Dropdown to filter by Service Vendor -->
    <v-select
      v-model="filterVendorId"
      class="mr-10"
      :items="serviceVendors"
      label="Service Vendors"
      :menu-props="{ closeOnContentClick: true } "
      item-title="name"
      item-value="id"
      clearable
      density="compact"
      variant="outlined"
      hide-details
      @update:modelValue="filterServices()"
    />


    <!--Button to remove all activated filters -->
    <v-btn
      variant="text"
      @click="removeFilter()"
    >
      <v-icon>mdi-close</v-icon>
    </v-btn>
  </v-toolbar>
</template>

<script>

  import {useServicesStore} from "@/stores/servicesStore";

  export default {
    name: 'ServiceOfferingOverviewToolbar',
    props: [],
    setup(){
      const servicesStore = useServicesStore();
      return {servicesStore};
    },
    data () {
      return {
        filterServiceOfferingName: '',
        filterServiceOfferingCategoryIds: [],
        filterVendorId: undefined,
        toggleExclusive: 1,
      }
    },

    computed: {
      apiStateServices() {
        return this.servicesStore.apiStateServices
      },
      serviceOfferings () {
        return this.servicesStore.serviceOfferings
      },
      serviceOfferingCategories() {
        return this.servicesStore.serviceOfferingCategories
      },
      serviceVendors () {
        return this.servicesStore.serviceVendors
      },
    },

    methods: {
      removeFilter () {
        this.filterServiceOfferingName = ''
        this.filterServiceOfferingCategoryIds = []
        this.filterVendorId = undefined
        this.filterServices()
      },
      filterServices () {
        let filteredServiceOfferings = this.serviceOfferings

        filteredServiceOfferings = filteredServiceOfferings.filter(so => so.name.toLowerCase().includes(this.filterServiceOfferingName.toLowerCase()))

        if (this.filterServiceOfferingCategoryIds.length > 0) {
          filteredServiceOfferings = filteredServiceOfferings.filter(so => this.filterServiceOfferingCategoryIds.findIndex(soc => soc === so.serviceCategoryId) > -1)
        }

        if (this.filterVendorId != undefined) {
          filteredServiceOfferings = filteredServiceOfferings.filter(so => so.serviceVendorId == this.filterVendorId)
        }
        // else {
        //   filteredServiceOfferings = this.serviceOfferings.filter(so => so.name.toLowerCase().includes(this.filterServiceOfferingName.toLowerCase())
        //       && this.filterServiceOfferingCategoryIds.findIndex(soc => soc === so.serviceCategoryId) > -1)
        // }

        this.$emit('service-offerings-filtered', filteredServiceOfferings)
      },
    },
  }
</script>

<style lang="scss" scoped>
#ServiceHeaderFilterRow{
  background-color: white;
}
</style>
