<template>
  <div>
    <div
      v-if="apiStateLoading"
      class="text-center"
    >
      <progress-circular />
    </div>
    <div v-if="apiStateError">
      Error
    </div>

    <div v-if="apiStateLoaded">
      <div v-if="serviceOfferings.length === 0">
        <base-material-card
          class="px-5 py-3"
        >
          <template #heading>
            <v-container
              fluid
              class="ma-0 pa-0"
            >
              <v-row class="secondary">
                <v-col
                  class="text-h3 font-weight-light"
                >
                  Service Offerings
                </v-col>
              </v-row>
            </v-container>
          </template>
          <no-item-available-note
            item="service offerings"
          >
            <!--
            <v-card-text
          >
            -->
            <!--            <v-alert-->
            <!--              outlined-->
            <!--              type="info"-->
            <!--            >-->
            <!--              No service offerings available-->
            <!--            </v-alert>-->
            <!--          </v-card-text>-->
          </no-item-available-note>
        </base-material-card>
      </div>

      <div v-else>
        <service-offering-overview-toolbar
          @service-offerings-filtered="onServiceOfferingsFiltered"
          @service-offerings-view-type-changed="onServiceOfferingsViewTypeChanged"
        />

        <v-container
          fluid
        >
          <v-row
            no-gutters
          >
            <v-col
              v-for="serviceOffering in filteredServiceOfferings ? filteredServiceOfferings : serviceOfferings"
              :key="serviceOffering.id"
              class="xs"
              sm="12"
              md="6"
              lg="4"
              xl="3"
            >
              <service-offering-card-grid
                :service-offering="serviceOffering"
                :show-only-latest-version="true"
                @click="onServiceOfferingClicked"
              />
            </v-col>
          </v-row>
        </v-container>
      </div>
    </div>
  </div>
</template>

<script>
  import { mapGetters } from 'vuex'
  import ApiState from '@/api/apiState.js'
  import ServiceOfferingCardGrid from '@/components/service_offerings/ServiceOfferingCardGrid'
  import ServiceOfferingOverviewToolbar from '@/components/service_offerings/ServiceOfferingToolbar'
  import ProgressCircular from "@/components/base/ProgressCircular";
  import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";

  export default {
    components: {
      NoItemAvailableNote,
      ProgressCircular,
      ServiceOfferingOverviewToolbar,
      ServiceOfferingCardGrid,
    },
    data () {
      return {
        filteredServiceOfferings: null,
        serviceOfferingsLoaded: false,
        serviceVendorsLoaded: false,
      }
    },
    created () {
      this.$store.dispatch('getServiceOfferingCategories')
      this.$store.dispatch('getServiceOfferingDeploymentTypes')
      this.$store.dispatch('getServiceOfferings')
      this.$store.dispatch('getServiceVendors')
    },
    computed: {
      ...mapGetters([
        'apiStateServices',
        'serviceOfferings',
      ]),
      apiStateLoaded () {
        const apiStateLoaded =
          this.apiStateServices.serviceOfferingCategories === ApiState.LOADED &&
          this.apiStateServices.serviceOfferingDeploymentTypes === ApiState.LOADED &&
          this.apiStateServices.serviceOfferings === ApiState.LOADED &&
          this.apiStateServices.serviceVendors === ApiState.LOADED
        return apiStateLoaded
      },
      apiStateLoading () {
        if (this.apiStateServices.serviceOfferingCategories === ApiState.INIT) {
          this.$store.dispatch('getServiceOfferingCategories')
        }
        if (this.apiStateServices.serviceOfferingDeploymentTypes === ApiState.INIT) {
          this.$store.dispatch('getServiceOfferingDeploymentTypes')
        }
        if (this.apiStateServices.serviceOfferings === ApiState.INIT) {
          this.$store.dispatch('getServiceOfferings')
        }
        if (this.apiStateServices.serviceVendors === ApiState.INIT) {
          this.$store.dispatch('getServiceVendors')
        }
        const apiStateLoading =
          this.apiStateServices.serviceOfferingCategories === ApiState.LOADING || this.apiStateServices.serviceOfferingCategories === ApiState.INIT ||
          this.apiStateServices.serviceOfferingDeploymentTypes === ApiState.LOADING || this.apiStateServices.serviceOfferingDeploymentTypes === ApiState.INIT ||
          this.apiStateServices.serviceOfferings === ApiState.LOADING || this.apiStateServices.serviceOfferings === ApiState.INIT ||
          this.apiStateServices.serviceVendors === ApiState.LOADING || this.apiStateServices.serviceVendors === ApiState.INIT
        return apiStateLoading
      },
      apiStateError () {
        const apiStateError = this.apiStateServices.serviceOfferingCategories === ApiState.ERROR &&
          this.apiStateServices.serviceOfferingDeploymentTypes === ApiState.ERROR &&
          this.apiStateServices.serviceOfferings === ApiState.ERROR &&
          this.apiStateServices.serviceVendors === ApiState.ERROR
        return apiStateError
      },
    },
    methods: {
      onServiceOfferingClicked (selectedService) {
        this.$router.push({ path: `/services/offerings/${selectedService.id}` })
      },
      onServiceOfferingsFiltered (filteredServiceOfferings) {
        this.filteredServiceOfferings = filteredServiceOfferings
      },
      onServiceOfferingsViewTypeChanged (viewType) {
        this.viewType = viewType
        console.log(this.serviceOfferings)
      },
    },
  }

</script>
