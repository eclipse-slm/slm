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
                large
                class="ml-2 mr-4"
            >
              mdi-chart-arc
            </v-icon>
            Device Statistics
          </v-col>
        </v-row>
      </v-container>
    </template>
    <no-item-available-note
        v-if="!resourceAAS.length"
        item="Device statistics">
    </no-item-available-note>
    <aas-pie-chart
        v-else
        :show-unknown=true
        :aas="resourceAAS"
        submodel-id-short="operating_system"
        :submodel-element-keys="['system', 'distribution', 'distribution_major_version']">
    </aas-pie-chart>
  </base-material-card>
</template>

<script>
  import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
  import {mapGetters} from "vuex";
  import AasPieChart from "@/components/charts/AasPieChart.vue";

  export default {
    name: 'DashboardResourceStatistics',
    components: {AasPieChart, NoItemAvailableNote },
    computed: {
      ...mapGetters(['resourceAAS'])
    },
    mounted () {
      this.$store.dispatch('getResourceAASFromBackend')
    }
  }
</script>