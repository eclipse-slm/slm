<template>
  <v-container>
    <aas-circular-chart-filter-bar
        v-if="showBreadcrumbs"
        :filter-values="this.labelsForVSelect"
        :show-filter-selector="this.showFilterSelector"
        @selectedFilterValuesChanged="onSelectedFilterValuesChanged"
    ></aas-circular-chart-filter-bar>
    <v-card-text v-if="this.chartType=='Pie'">
      <Pie
          :data="chartData"
          :options="chartOptions"
      />
    </v-card-text>
    <v-card-text v-if="this.chartType=='Doughnut'">
      <Doughnut
          :data="chartData"
          :options="chartOptions"
      />
    </v-card-text>
    <v-footer class="mt-8 white">
      <v-spacer></v-spacer>
      <v-tooltip top>
        <template #activator="{ on, attrs }">
          <v-icon
              v-bind="attrs"
              v-on="on"
          >
            mdi-information-outline
          </v-icon>
        </template>
        <span>Chart is based on "{{ this.submodelIdShort }}" submodel</span>
      </v-tooltip>
    </v-footer>
  </v-container>
</template>

<script>
  import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";
  import { Pie, Doughnut } from "vue-chartjs";
  import AasCircularChartFilterBar from "@/components/charts/AasCircularChartFilterBar.vue";

  ChartJS.register(ArcElement, Tooltip, Legend)

  export default {
    name: 'AasCircularChart',
    components: {AasCircularChartFilterBar, Pie, Doughnut},
    props: {
      chartType: {
        default: "Pie",
        type: String,
      },
      showUnknown: {
        default: true,
        type: Boolean
      },
      aas: {
        default: () => {
          return []
        },
        type: Array
      },
      submodelIdShort: {
        default: "",
        type: String,
      },
      submodelElementKeys: {
        default: () => {
          return []
        },
        type: Array
      },
    },
    data() {
      return {
        chartBackgroundColors: ["#004263", "#00A0E3", "#1E1E1E", "#71BD86", "#179C7D", "#FF7A5A"],
        selectedFilterValues: [],
        unkownLabel: "unknown",
        chartOptions: {
          responsive: true,
          maintainAspectRatio: false
        }
      }
    },
    computed: {
      showBreadcrumbs() {
        return this.submodelElementKeys.length > 1;
      },
      showFilterSelector() {
        return this.submodelElementKeys.length != this.selectedFilterValues.length
      },
      currentSubmodelElementKeyIndex() {
        return this.submodelElementKeys.length > this.selectedFilterValues.length ? this.selectedFilterValues.length : this.submodelElementKeys.length - 1
      },
      currentSubmodelElementKey() {
        return this.submodelElementKeys[this.currentSubmodelElementKeyIndex]
      },
      submodels() {
        return this.aas
            .flatMap(({submodels}) => submodels)
            .filter(sm => sm.idShort === this.submodelIdShort)
      },
      targetSubmodelElementValues() {
        let tsm = []

        this.submodels.forEach(
            sm => {
              // tsm.push(Object.entries(sm.submodelElements))
              Object.entries(sm.submodelElements).forEach(entry => {
                const [k, v] = entry
                if(k == this.currentSubmodelElementKey)
                  tsm.push(v.value)
              })
            }
        )

        return tsm
      },
      labels() {
        let labelValues = new Set()
        this.submodels.forEach(sm => {
          labelValues.add(sm.submodelElements[this.currentSubmodelElementKey].value)
        })

        if(this.addUnkown && this.unknownData > 0)
          labelValues.add(this.unkownLabel)

        return labelValues
      },
      labelsForVSelect() {
        let labelsWoUnknown = new Set(this.labels)
        labelsWoUnknown.delete(this.unkownLabel)
        return [...labelsWoUnknown]
      },
      data() {
        let data = []
        this.labels.forEach(l => {
          if(l !== this.unkownLabel)
            data.push(
                this.targetSubmodelElementValues.filter(tsev => tsev === l).length
            )
          else
            data.push(this.unknownData)
        })
        return data
      },
      unknownData() {
        return this.aas.length - this.submodels.length
      },
      chartData() {
        return {
          labels: [...this.labels],
          datasets: [
            {
              data: this.data,
              backgroundColor: this.chartBackgroundColors
            }
          ]
        }
      },
      addUnkown() {
        return this.showUnknown && this.selectedFilterValues.length == 0
      }
    },
    methods: {
      onSelectedFilterValuesChanged(value) {
        this.selectedFilterValues=value
      }
    }
  }
</script>