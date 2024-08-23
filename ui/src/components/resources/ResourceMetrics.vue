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
import ResourceManagementClient from "@/api/resource-management/resource-management-client";

export default {
    name: 'ResourceMetrics',
    components: {
      // LineChart,
    },
    props: {
      resourceId: {
        type: String,
        default: ""
      }
    },
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

        ResourceManagementClient.metricsApi.getMetric(this.resourceId).then(response => {
          const metric = response.data;
          if (Object.keys(metric).length === 0) {
            return
          }

          if (this.pollMetrics) {
            setTimeout(this.getMetric.bind(this), 3000)
          }
          this.metrics = []
          // this.metrics.push({ category: 'System', name: 'Uptime', value: metric.systemUptime, unit: 'seconds' })
          this.metrics.push({ category: 'System', name: 'OS', value: metric.OS })
          // this.metrics.push({ category: 'CPU', name: 'Cores', value: metric.cpuCores })
          this.metrics.push({ category: 'CPU', name: 'Architecture', value: metric.ProArc })
          this.metrics.push({ category: 'CPU', name: 'Usage', value: metric.CPULoad, unit: '%' })
          this.metrics.push({ category: 'RAM', name: 'Installed', value: metric.SizOfTheRam, unit: 'kilobytes' })
          this.metrics.push({ category: 'RAM', name: 'Usage', value: metric.AllocatedMemory, unit: '%' })
          // this.metrics.push({ category: 'Filesystem', name: 'Root - Type', value: metric.rootFsType })
          // this.metrics.push({ category: 'Filesystem', name: 'Root - Usage', value: metric.rootFsUsage, unit: '%' })
          // this.metrics.push({ category: 'Docker', name: 'Running Containers', value: metric.dockerRunningContainers })
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
