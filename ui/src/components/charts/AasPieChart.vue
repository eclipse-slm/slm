<template>
  <v-container>
    <aas-pie-chart-filter-bar
        v-if="showBreadcrumbs"
        :filter-values="this.labelsForVSelect"
        :show-filter-selector="this.showFilterSelector"
        @selectedFilterValuesChanged="onSelectedFilterValuesChanged"
    ></aas-pie-chart-filter-bar>
    <v-card-text>
      <Pie
          :data="chartData"
          :options="chartOptions"
      />
    </v-card-text>
  </v-container>
</template>

<script>
  import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";
  import {Pie} from "vue-chartjs";
  import AasPieChartFilterBar from "@/components/charts/AasPieChartFilterBar.vue";

  ChartJS.register(ArcElement, Tooltip, Legend)

  export default {
    name: 'AasPieChart',
    components: {AasPieChartFilterBar, Pie},
    props: {
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

        if(this.addUnkown)
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
            data.push(this.aas.length - this.submodels.length)
        })
        return data
      },
      chartData() {
        return {
          labels: [...this.labels],
          datasets: [
            {
              data: this.data
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