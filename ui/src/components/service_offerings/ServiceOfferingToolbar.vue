<template>
  <div>
    <v-toolbar
      id="ServiceHeaderFilterRow"
      flat
      height="75px"
    >
      <!-- Search field to filter by Service Offering Name -->
      <div class="mr-10">
        <v-text-field
          v-model="filterServiceOfferingName"
          prepend-inner-icon="search"
          :placeholder="$t('labels.SearchLabel')"
          single-line
          variant="outlined"
          density="compact"
          rounded
          hide-details
          @update:modelValue="filterServices()"
        />
      </div>

      <!-- Dropdown to filter by Service Offering Category -->
      <div class="mr-10">
        <v-select
          v-model="filterServiceOfferingCategoryIds"
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
          max-width="10px"
          @update:modelValue="filterServices()"
        />
      </div>

      <!-- Dropdown to filter by Service Vendor -->
      <div class="mr-10">
        <v-select
          v-model="filterVendorId"
          :items="serviceVendors"
          label="Service Vendors"
          :menu-props="{ closeOnContentClick: true } "
          item-title="name"
          item-value="id"
          clearable
          density="compact"
          variant="outlined"
          hide-details
          max-width="10px"
          @update:modelValue="filterServices()"
        />
      </div>

      <!--Button to remove all activated filters -->
      <v-btn
        variant="text"
        @click="removeFilter()"
      >
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-toolbar>
  </div>
</template>

<script>
  import { mapGetters } from 'vuex'

  export default {
    name: 'ServiceOfferingOverviewToolbar',
    props: [],

    data () {
      return {
        filterServiceOfferingName: '',
        filterServiceOfferingCategoryIds: [],
        filterVendorId: undefined,
        toggleExclusive: 1,
      }
    },

    computed: {
      ...mapGetters([
        'apiStateServices',
        'serviceOfferings',
        'serviceOfferingCategories',
        'serviceVendors',
      ]),
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
</style>
