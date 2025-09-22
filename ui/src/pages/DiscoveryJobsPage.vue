<template>
  <v-container
    id="discovery"
    fluid
    tag="section"
  >
    <div v-if="apiState === ApiState.ERROR">
      Error
    </div>

    <div v-else>
      <DiscoveryJobsTable/>
    </div>
  </v-container>
</template>

<script setup>
import { onMounted } from 'vue';
import {storeToRefs} from "pinia";
import { useDiscoveryStore } from "@/stores/discoveryStore";
import ApiState from '@/api/apiState';
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import DriversTable from "@/components/discovery/DriversTable.vue";
import DiscoveryJobsTable from "@/components/discovery/DiscoveryJobsTable.vue";

const discoveryStore = useDiscoveryStore();
const {apiState, drivers} = storeToRefs(discoveryStore);

onMounted(() => {
  discoveryStore.updateStore();
});
</script>

<style scoped />