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
      <template #item.value="{ item }">
        <div v-if="item.unit === '%'">
          {{ parseFloat(item.value).toFixed(2) }} %
        </div>
        <div v-else-if="item.unit === 'bytes'">
          {{ parseFloat(item.value/1000000000).toFixed(2) }} GB
        </div>
        <div v-else-if="item.unit === 'kilobytes'">
          {{ parseFloat(item.value/1000000).toFixed(2) }} GB
        </div>
        <div v-else-if="item.unit === 'seconds'">
          {{ parseFloat(item.value/(1000*60)).toFixed(2) }} h
        </div>
        <div v-else>
          {{ item.value }}
        </div>
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
  </div>
</template>
<script>
import MetricsRestApi from '@/api/resource-management/metricsRestApi'

export default {
    name: 'ResourceMetrics',
    components: {
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
          { title: 'Metric', value: 'name' },
          { title: 'Value', value: 'value' },
        ],
        data: [30, 40, 60, 70, 5],
        cpuUsage: 0,
        metrics: [],
        pollMetrics: true,
      }
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
          this.metrics.push({ category: 'System', name: 'OS', value: response.OS })
          this.metrics.push({ category: 'CPU', name: 'Architecture', value: response.ProArc })
          this.metrics.push({ category: 'CPU', name: 'Usage', value: response.CPULoad, unit: '%' })
          this.metrics.push({ category: 'RAM', name: 'Installed', value: response.SizOfTheRam, unit: 'kilobytes' })
          this.metrics.push({ category: 'RAM', name: 'Usage', value: response.AllocatedMemory, unit: '%' })
        }).catch((e) => {
          console.log(e)
          this.metrics = []
        })
      },
    },
  }
</script>

<style scoped>
</style>
