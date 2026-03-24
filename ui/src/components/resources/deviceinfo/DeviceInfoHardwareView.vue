<template>
  <div>
    <progress-circular v-if="apiState===ApiState.LOADING" />

    <div v-else>
      <v-row>
        <v-col>
          <v-spacer />
        </v-col>
        <v-col
          cols="2"
          class="text-right"
        >
          <v-select
            v-model="selectedInterval"
            :items="intervals"
            :label="`Refresh interval (remaining: ${remainingTime} s)`"
            class="mx-4"
            variant="underlined"
            item-title="text"
            item-value="value"
          />
        </v-col>
        <v-col
          cols="1"
          class="text-right"
        >
          <v-btn
            class="mx-4"
            color="secondary"
            @click="getMetric"
          >
            <v-icon
              icon="mdi-refresh"
              color="white"
            />
          </v-btn>
        </v-col>
      </v-row>
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
      <div
        v-else
        class="my-4"
      >
        <v-alert
          variant="outlined"
          type="info"
        >
          No hardware information available
        </v-alert>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import ApiState from "@/api/apiState";
import ProgressCircular from "@/components/base/ProgressCircular.vue";

const props = defineProps({
  resourceId: {
    type: String,
    default: ""
  }
});

const headers = ref([
  { title: 'Category', value: 'category' },
  { title: 'Metric', value: 'name' },
  { title: 'Value', value: 'value' },
]);

const metrics = ref([]);
const pollMetrics = ref(true);
const apiState = ref(ApiState.LOADING);
const intervals = ref([
  { text: '1 s', value: 1 },
  { text: '5 s', value: 5 },
  { text: '10 s', value: 10 },
  { text: '30 s', value: 30 },
  { text: '60 s', value: 60 }
]);
const selectedInterval = ref(5);
const remainingTime = ref(selectedInterval.value);

const getMetric = () => {
  ResourceManagementClient.metricsApi.getMetric(props.resourceId).then(response => {
    apiState.value = ApiState.LOADED;

    const metric = response.data;

    if (Object.keys(metric).length === 0) {
      return;
    }

    if (pollMetrics.value) {
      setTimeout(getMetric, selectedInterval.value * 1000);
    }
    metrics.value = [];
    metrics.value.push({ category: 'System', name: 'OS', value: metric.OS });
    metrics.value.push({ category: 'CPU', name: 'Architecture', value: metric.ProArc });
    metrics.value.push({ category: 'CPU', name: 'Usage', value: metric.CPULoad, unit: '%' });
    metrics.value.push({ category: 'RAM', name: 'Installed', value: metric.SizOfTheRam, unit: 'kilobytes' });
    metrics.value.push({ category: 'RAM', name: 'Usage', value: metric.AllocatedMemory, unit: '%' });

    remainingTime.value = selectedInterval.value;
  }).catch((e) => {
    console.log(e);
    metrics.value = [];
  });
};

const updateRemainingTime = () => {
  if (remainingTime.value > 0) {
    remainingTime.value--;
  }
};

onMounted(() => {
  getMetric();
  setInterval(updateRemainingTime, 1000);
});

onBeforeUnmount(() => {
  pollMetrics.value = false;
});

watch(selectedInterval, (newInterval) => {
  remainingTime.value = newInterval;
});
</script>

<style scoped>
</style>