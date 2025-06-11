<template>
  <base-material-card>
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
      v-if="!resourceDevicesStore.resourceAasDescriptors.length"
      item="Device statistics"
    />
    <!--    <aas-circular-chart-->
    <!--      v-else-->
    <!--      chart-type="Doughnut"-->
    <!--      :show-unknown="true"-->
    <!--      :aas="resourceStore.resourceAASs"-->
    <!--      submodel-id-short="operating_system"-->
    <!--      :submodel-element-keys="['system', 'distribution', 'distribution_major_version']"-->
    <!--    />-->
  </base-material-card>
</template>

<script>
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";

// import AasCircularChart from "@/components/charts/AasCircularChart.vue";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";

export default {
    name: 'DashboardResourceStatistics',
    components: {NoItemAvailableNote },
    setup(){
      const resourceDevicesStore = useResourceDevicesStore();
      return {resourceDevicesStore}
    },
    mounted () {
      this.resourceDevicesStore.getResourceAasDescriptors();
    }
  }
</script>