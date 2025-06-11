<template>
  <v-container fluid>
    <div
      v-if="apiState === ApiState.LOADING || apiState === ApiState.INIT || apiState === ApiState.UPDATING"
      class="text-center"
    >
      <ProgressCircular />
    </div>
    <div v-if="apiState === ApiState.ERROR">
      Error
    </div>

    <div v-if="apiState === ApiState.LOADED">
      <service-instances-overview />
    </div>
  </v-container>
</template>

<script setup>
import { onMounted } from 'vue';
import ApiState from '@/api/apiState.js';
import ServiceInstancesOverview from '@/components/services/ServiceInstancesOverview';
import { useServiceInstancesStore } from "@/stores/serviceInstancesStore";
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import {storeToRefs} from "pinia";

const serviceInstancesStore = useServiceInstancesStore();
const {apiState} = storeToRefs(serviceInstancesStore);

onMounted(() => {
  serviceInstancesStore.updateStore();
});
</script>