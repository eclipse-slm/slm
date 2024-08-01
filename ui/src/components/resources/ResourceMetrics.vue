<template>
  <div>
    <v-data-table
      v-if="metrics.length > 0"
      :headers="headers"
      :items-per-page="50"
      class="elevation-1"
      item-key="name"
      :items="metrics"
      hide-default-footer
    >
      <template
        #body="{ items }"
      >
        <tbody
          v-for="metric in items"
          :key="metric.category + '|' + metric.name"
        >
          <tr>
            <td>{{ metric.category }}</td>
            <td>{{ metric.name }}</td>
            <td v-if="metric.unit === '%'">
              {{ parseFloat(metric.value).toFixed(2) }} %
            </td>
            <td v-else-if="metric.unit === 'bytes'">
              {{ parseFloat(metric.value/1000000000).toFixed(2) }} GB
            </td>
            <td v-else-if="metric.unit === 'kilobytes'">
              {{ parseFloat(metric.value/1000000).toFixed(2) }} GB
            </td>
            <td v-else-if="metric.unit === 'seconds'">
              {{ parseFloat(metric.value/(1000*60)).toFixed(2) }} h
            </td>
            <td v-else>
              {{ metric.value }}
            </td>
          </tr>
        </tbody>
      </template>
    </v-data-table>
    <div v-else>
      <v-alert
        variant="outlined"
        type="info"
      >
        No hardware information available
      </v-alert>
    </div>

    <!--    <line-chart :chart-data="chartData" />-->
  </div>
</template>
<script>
// import { LineChart } from 'vue-chart-3'
import MetricsRestApi from '@/api/resource-management/metricsRestApi'

export default {
    name: 'ResourceMetrics',
    components: {
      // LineChart,
    },
    props: ['resourceId'],
    data () {
      return {
        headers: [
          { title: 'Category', value: 'category' },
          { title: 'Metric', value: 'metric' },
          { title: 'Value', value: 'value' },
        ],
        data: [30, 40, 60, 70, 5],
        cpuUsage: 0,
        metrics: [],
        pollMetrics: true,
      }
    },
    computed: {
      chartData () {
        return {
          labels: ['Paris', 'Nîmes', 'Toulon', 'Perpignan', 'Autre'],
          datasets:
            [
              {
                data: this.data.value,
                backgroundColor: [
                  '#77CEFF',
                  '#0079AF',
                  '#123E6B',
                  '#97B0C4',
                  '#A5C8ED',
                ],
              },
            ],
        }
      },
    },

    mounted () {
      this.getMetric()
    },
    methods: {
      onClose () {
        this.pollMetrics = false
      },

      getMetric () {
        MetricsRestApi.getMetricsOfResource(this.resourceId).then(response => {
          if (Object.keys(response).length === 0) {
            return
          }

          if (this.pollMetrics) {
            setTimeout(this.getMetric.bind(this), 3000)
          }
          this.metrics = []
          // this.metrics.push({ category: 'System', name: 'Uptime', value: response.systemUptime, unit: 'seconds' })
          this.metrics.push({ category: 'System', name: 'OS', value: response.OS })
          // this.metrics.push({ category: 'CPU', name: 'Cores', value: response.cpuCores })
          this.metrics.push({ category: 'CPU', name: 'Architecture', value: response.ProArc })
          this.metrics.push({ category: 'CPU', name: 'Usage', value: response.CPULoad, unit: '%' })
          this.metrics.push({ category: 'RAM', name: 'Installed', value: response.SizOfTheRam, unit: 'kilobytes' })
          this.metrics.push({ category: 'RAM', name: 'Usage', value: response.AllocatedMemory, unit: '%' })
          // this.metrics.push({ category: 'Filesystem', name: 'Root - Type', value: response.rootFsType })
          // this.metrics.push({ category: 'Filesystem', name: 'Root - Usage', value: response.rootFsUsage, unit: '%' })
          // this.metrics.push({ category: 'Docker', name: 'Running Containers', value: response.dockerRunningContainers })
        }).catch((e) => {
          console.log(e)
          this.metrics = []
        })
      },
    },
  }
</script>

<style scoped>
.small {
  max-width: 600px;
  margin:  150px auto;
}
</style>
