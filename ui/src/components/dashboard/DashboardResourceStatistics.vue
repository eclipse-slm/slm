<template>
  <base-material-card class="px-5 py-3">
    <template #heading>
      <v-container
        fluid
        class="ma-0 pa-0"
      >
        <v-row class="warn text-h3 font-weight-light">
          <v-col>
            <v-icon
              icon="mdi-chart-arc"
              size="large"
              class="ml-2 mr-4"
              style="font-size: 36px;"
            />
            Device Statistics
          </v-col>
        </v-row>
      </v-container>
    </template>
    <no-item-available-note
      v-if="!resourceAAS.length"
      item="Device statistics"
    />
    <aas-circular-chart
      v-else
      chart-type="Doughnut"
      :show-unknown="true"
      :aas="resourceAAS"
      submodel-id-short="operating_system"
      :submodel-element-keys="['system', 'distribution', 'distribution_major_version']"
    />
  </base-material-card>
</template>

<script>
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";

import AasCircularChart from "@/components/charts/AasCircularChart.vue";
import {useResourcesStore} from "@/stores/resourcesStore";

export default {
    name: 'DashboardResourceStatistics',
    components: {AasCircularChart, NoItemAvailableNote },
    setup(){
      const resourceStore = useResourcesStore();
      return {resourceStore}
    },
    computed: {
      resourceAAS() {
        return this.resourceStore.resourceAAS
      },
    },
    mounted () {
      this.resourceStore.getResourceAASFromBackend();
    }
  }
</script>