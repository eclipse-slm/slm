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
        <base-material-card>
          <template #heading>
            <v-container
              fluid
              class="ma-0 pa-0"
            >
              <v-row class="bg-secondary">
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
                @click="onServiceOfferingClicked(serviceOffering)"
              />
            </v-col>
          </v-row>
        </v-container>
      </div>
    </div>
  </div>
</template>

<script>

import ApiState from '@/api/apiState.js'
import ServiceOfferingCardGrid from '@/components/service_offerings/ServiceOfferingCardGrid'
import ServiceOfferingOverviewToolbar from '@/components/service_offerings/ServiceOfferingToolbar'
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";

export default {
    components: {
      NoItemAvailableNote,
      ProgressCircular,
      ServiceOfferingOverviewToolbar,
      ServiceOfferingCardGrid,
    },
    setup(){
      const serviceOfferingsStore = useServiceOfferingsStore();

      return {serviceOfferingsStore}
    },
    data () {
      return {
        filteredServiceOfferings: null,
        serviceOfferingsLoaded: false,
        serviceVendorsLoaded: false,
      }
    },
    computed: {
      apiStateServices() {
        return this.serviceOfferingsStore.apiState
      },
      serviceOfferings () {
        return this.serviceOfferingsStore.serviceOfferings
      },

      apiStateLoaded () {
        return this.apiStateServices === ApiState.LOADED
      },
      apiStateLoading () {
        if (this.apiStateServices === ApiState.INIT) {
          this.serviceOfferingsStore.updateStore();
        }
        return this.apiStateServices === ApiState.LOADING || this.apiStateServices === ApiState.INIT
      },
      apiStateError () {
        return this.apiStateServices === ApiState.ERROR
      },
    },
    created () {
      const serviceOfferingsStore = useServiceOfferingsStore();
      serviceOfferingsStore.updateStore();
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
