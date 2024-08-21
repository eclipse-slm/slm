<template>
  <div>
    <base-material-card
      class="px-5 py-3"
    >
      <template #heading>
        <overview-heading text="Service Instances" />
      </template>

      <no-item-available-note
        v-if="!Object.keys(services).length"
        item="services"
      />
      <v-card-text v-else>
        <v-row>
          <!--            <v-select-->
          <!--              :value="selectedServiceType"-->
          <!--              :items="availableServiceTypes"-->
          <!--              label="Resource Type"-->
          <!--              clearable-->
          <!--              @update:modelValue="updateSelectedResourceType"-->
          <!--            />-->
          <v-spacer />
          <!--            <v-text-field-->
          <!--              v-model="searchServices"-->
          <!--              label="Search services"-->
          <!--              append-icon="search"-->
          <!--              clearable-->
          <!--            />-->
        </v-row>

        <v-row>
          <v-col>
            <service-instances-table
              class="mt-0"
              @service-instance-clicked="onServiceInstanceClicked"
            />
          </v-col>
        </v-row>
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
import ServiceInstanceDetailsDialog from "@/components/services/dialog/ServiceInstanceDetailsDialog";
import {useServicesStore} from "@/stores/servicesStore";

export default {
    name: 'ServiceInstancesOverview',
    components: {
      ServiceInstanceDetailsDialog,
      OverviewHeading,
      ServiceInstancesTable,
      NoItemAvailableNote
    },
    setup(){
      const servicesStore = useServicesStore();
      return {servicesStore};
    },
    data () {
      return {
        selectedServiceInstance: null
      }
    },
    computed: {
      services () {
        return this.servicesStore.services
      },
    },
    methods: {
      onServiceInstanceClicked (serviceInstance) {
        this.selectedServiceInstance = serviceInstance
      },
    }
  }
</script>
