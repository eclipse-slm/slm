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
        v-if="!platform_resources.length"
        item="Device statistics">
    </no-item-available-note>
    <v-container v-else>
      <v-breadcrumbs class="grey lighten-5">
        <v-breadcrumbs-item><v-icon>mdi-home</v-icon></v-breadcrumbs-item>
        <v-breadcrumbs-item v-for="v in selected_filter_values" v-bind:key="v">{{ v }}</v-breadcrumbs-item>
        <v-breadcrumbs-divider></v-breadcrumbs-divider>
        <v-breadcrumbs-item v-if="this.showValueSelector">
          <v-select :items="chartLabels" clearable @change="addFilterValue" v-model="current_filter_value" />
        </v-breadcrumbs-item>
      </v-breadcrumbs>
      <v-card-text>
        <Pie
            :data="chartData"
            :options="options"
        />
      </v-card-text>
    </v-container>

  </base-material-card>
</template>

<script>
  import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
  import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";
  import { Pie } from 'vue-chartjs'

  ChartJS.register(ArcElement, Tooltip, Legend)

  export default {
    name: 'DashboardResourceStatistics',
    components: { NoItemAvailableNote, Pie },
    computed: {
      platform_resources() {
        return this.aas.filter(aas => aas.asset == this.target_asset)
      },
      target_submodels() {
        return this.platform_resources
            .flatMap(({submodels}) => submodels)
            .filter(sm => sm.id_short == this.target_submodel)
      },
      target_submodel_elements() {
        var tsm = this.target_submodels

        for(var i = 0; i < this.selected_filter_values.length; i++) {

          tsm = this.target_submodels.filter(
              sm => sm.submodel_elements.filter(
                  sme => sme.key == this.filter_key_sequence[i] && sme.value == this.selected_filter_values[i]
              ).length > 0
          )
        }

        return tsm
            .flatMap(({submodel_elements}) => submodel_elements)
            .filter(sme => sme.key == this.current_filter_key)
      },
      chartDatasets() {
        // return this.target_submodel_elements.filter(sme => sme.value == this.chartLabels[0]).length
        let data = []
         this.chartLabels.forEach(l =>
            data.push(
                this.target_submodel_elements.filter(sme => sme.value == l).length
            )
        )
        return data;
      },
      chartLabels() {
        return [...new Set(
            this.target_submodel_elements.flatMap(({value}) => value)
        )]
      },
      chartData() {
        return {
          labels: this.chartLabels,
          datasets: [
            {
              data: this.chartDatasets
            }
          ]
        }
      },
      showValueSelector() {
        if(this.getNextFilterKey() != undefined)
          return true
        else
          return false
      }
    },
    data() {
      return {
        target_asset: "platform_resource",
        target_submodel: "operating_system",
        current_filter_key: "system",
        filter_key_sequence: ["system", "distribution", "distribution_major_version"],
        aas: [
          {
            asset: "platform_resource",
            submodels: [
              {
                id_short: "operating_system",
                submodel_elements: [
                  {
                    key: "system",
                    value: "linux"
                  },
                  {
                    key: "distribution",
                    value: "Ubuntu"
                  },
                  {
                    key: "distribution_major_version",
                    value: "22"
                  }
                ]
              }
            ]
          },
          {
            asset: "platform_resource",
            submodels: [
              {
                id_short: "operating_system",
                submodel_elements: [
                  {
                    key: "system",
                    value: "linux"
                  },
                  {
                    key: "distribution",
                    value: "Ubuntu"
                  },
                  {
                    key: "distribution_major_version",
                    value: "22"
                  }
                ]
              }
            ]
          },
          {
            asset: "platform_resource",
            submodels: [
              {
                id_short: "operating_system",
                submodel_elements: [
                  {
                    key: "system",
                    value: "linux"
                  },
                  {
                    key: "distribution",
                    value: "Ubuntu"
                  },
                  {
                    key: "distribution_major_version",
                    value: "18"
                  }
                ]
              }
            ]
          },
          {
            asset: "platform_resource",
            submodels: [
              {
                id_short: "operating_system",
                submodel_elements: [
                  {
                    key: "system",
                    value: "windows"
                  },
                  {
                    key: "distribution",
                    value: "Windows"
                  },
                  {
                    key: "distribution_major_version",
                    value: "10"
                  }
                ]
              }
            ]
          },
          {
            asset: "service_offering",
            submodels: []
          }
        ],
        selected_filter_values: [],
        data: {
          labels: ['VueJs', 'EmberJs', 'ReactJs', 'AngularJs'],
          datasets: [
            {
              backgroundColor: ['#41B883', '#E46651', '#00D8FF', '#DD1B16'],
              data: [40, 20, 80, 10]
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false
        }
      }
    },
    methods: {
      addFilterValue(value) {
        this.selected_filter_values.push(value)
        this.current_filter_value = 0
        this.current_filter_key = this.getNextFilterKey()
      },
      getNextFilterKey() {
        return this.filter_key_sequence[
            this.filter_key_sequence.indexOf(this.current_filter_key) + 1
          ]
      }
    }
  }
</script>