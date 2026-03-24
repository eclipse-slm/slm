<template>
  <v-container fluid>
    <div v-if="apiStateLoading">
      Loading
    </div>

    <div v-if="apiStateError">
      Error
    </div>

    <v-row v-if="apiStateLoaded">
      <v-col cols="4">
        <v-row class="mx-2 my-2">
          <service-card
            :service-offering="serviceOffering"
            :show-only-latest-version="false"
            :passive="true"
            @service-offering-version-selected="onServiceOfferingVersionSelected"
          />
        </v-row>
        <v-row>
          <v-col>
            <v-btn
              class="mx-3 my-3"
              style="background-color: #999; color: #fff"
              @click="hideServiceView"
            >
              {{ $t('buttons.Hide_info') }}
            </v-btn>
          </v-col>
          <v-spacer />
          <v-col>
            <v-btn
              class="mx-3 my-3"
              :color="$vuetify.theme.themes.light.colors.secondary"
              :disabled="selectedServiceOfferingVersionId == null"
              @click="showOrderView"
            >
              {{ $t('buttons.Order') }}
            </v-btn>
          </v-col>
        </v-row>
      </v-col>

      <v-col
        cols="8"
      >
        <!-- Displays the Buttons to switch between "Overview" and "Service Requirements" -->
        <v-row
          class="mx-3"
          justify="space-around"
        >
          <v-btn
            variant="text"
            @click="SelectedServiceOverview = true"
          >
            {{ $t('ServiceLabels.Overview') }}
          </v-btn>
          <v-btn
            variant="text"
            @click="SelectedServiceOverview = false"
          >
            {{ $t('ServiceLabels.Service_Requirements') }}
          </v-btn>
        </v-row>

        <!-- Displays the ServiceInfoOverview -->
        <v-col v-show="SelectedServiceOverview">
          <service-details-overview :service-offering="serviceOffering" />
        </v-col>

        <!-- Displays the ServiceInfoRequirements -->
        <v-col v-show="!SelectedServiceOverview">
          <service-details-requirements
            :service-offering="serviceOffering"
            :service-offering-version-id="selectedServiceOfferingVersionId"
          />
        </v-col>
      </v-col>
    </v-row>
    <div v-else>
      Loading...
    </div>
  </v-container>
</template>

<script>

import ServiceCard from '@/components/service_offerings/ServiceOfferingCardGrid'
import ServiceDetailsOverview from '@/components/service_offerings/ServiceOfferingDetailsOverview'
import ServiceDetailsRequirements from '@/components/service_offerings/ServiceOfferingDetailsRequirements.vue'
import ApiState from '@/api/apiState'
import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";

export default {
    name: 'ServiceOfferingDetailsPage',
    components: {
      ServiceCard,
      ServiceDetailsOverview,
      ServiceDetailsRequirements,
    },
    props: {
      serviceOfferingId: {
        type: String,
        default: null
      },
      selectedService: {
        type: Object,
        default: null
      }
    },
        // ['serviceOfferingId', 'selectedService'],
    setup(){
      const serviceOfferingsStore = useServiceOfferingsStore();

      return {serviceOfferingsStore}
    },
    data () {
      return {
        SelectedServiceOverview: true,
        apiState: ApiState.INIT,
        selectedServiceOfferingVersionId: null
      }
    },
    computed: {
      apiStateServices () {
        return this.serviceOfferingsStore.apiState
      },

      apiStateLoaded () {
        return this.apiStateServices === ApiState.LOADED
      },
      apiStateLoading () {
        if (this.apiStateServices === ApiState.INIT) {
          this.serviceOfferingsStore.getServiceOfferingCategories();
        }
        if (this.apiStateServices === ApiState.INIT) {
          this.serviceOfferingsStore.getServiceOfferingDeploymentTypes();
        }
        if (this.apiStateServices === ApiState.INIT) {
          this.serviceOfferingsStore.getServiceOfferings();
        }
        if (this.apiStateServices === ApiState.INIT) {
          this.serviceOfferingsStore.getServiceVendors();
        }
        return this.apiStateServices === ApiState.LOADING || this.apiStateServices === ApiState.INIT
      },
      apiStateError () {
        return this.apiStateServices === ApiState.ERROR
      },
      serviceOffering () {
        const serviceOffering = this.serviceOfferingById(this.serviceOfferingId)
        return serviceOffering
      },
    },
    methods: {
      hideServiceView () {
        this.$router.push({ path: '/services/offerings' })
      },
      showOrderView () {
        this.$router.push({ path: `/services/offerings/${this.serviceOfferingId}/versions/${this.selectedServiceOfferingVersionId}/order` })
      },
      onServiceOfferingVersionSelected (serviceOfferingVersionId) {
        this.selectedServiceOfferingVersionId = serviceOfferingVersionId
      },
      serviceOfferingById (id) {
        return this.serviceOfferingsStore.serviceOfferingById(id)
      },
    },

  }

</script>

<style rel="stylesheet"
       type="text/css"
       src="@/design/serviceOfferingCard.scss"
       lang="scss" scoped
>
</style>
