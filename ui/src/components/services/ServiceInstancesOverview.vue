<template>
  <div>
    <base-material-card>
      <template #heading>
        <overview-heading text="Service Instances" />
      </template>

      <no-item-available-note
        v-if="!Object.keys(services).length"
        item="services"
      />
      <v-card-text v-else>
        <service-instances-table
          class="mt-0 flex"
          @service-instance-clicked="onServiceInstanceClicked"
        />
      </v-card-text>
    </base-material-card>

    <service-instance-details-dialog
      :service-instance="selectedServiceInstance"
      @closed="selectedServiceInstance = null"
    />
  </div>
</template>

<script>

import ServiceInstancesTable from '@/components/services/ServiceInstancesTable'
import OverviewHeading from "@/components/base/OverviewHeading.vue";
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import ServiceInstanceDetailsDialog from "@/components/services/dialog/ServiceInstanceDetailsDialog.vue";
import {useServiceInstancesStore} from "@/stores/serviceInstancesStore";

export default {
    name: 'ServiceInstancesOverview',
    components: {
      ServiceInstanceDetailsDialog,
      OverviewHeading,
      ServiceInstancesTable,
      NoItemAvailableNote
    },
    setup(){
      const serviceInstancesStore = useServiceInstancesStore();
      return {serviceInstancesStore};
    },
    data () {
      return {
        selectedServiceInstance: null
      }
    },
    computed: {
      services () {
        return this.serviceInstancesStore.services
      },
    },
    methods: {
      onServiceInstanceClicked (serviceInstance) {
        this.selectedServiceInstance = serviceInstance
      },
    }
  }
</script>
