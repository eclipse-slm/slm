<template>
  <v-container
    id="discovery"
    fluid
  >
    <div v-if="apiState === ApiState.ERROR">
      Error
    </div>

    <div v-else>
      <discovery-overview />
    </div>
  </v-container>
</template>

<script setup>
import { onMounted } from 'vue';
import ApiState from '@/api/apiState';
import DiscoveryOverview from "@/components/discovery/DiscoveryOverview.vue";
import { useDiscoveryStore } from "@/stores/discoveryStore";
import {storeToRefs} from "pinia";

const discoveryStore = useDiscoveryStore();
const {apiState} = storeToRefs(discoveryStore);

onMounted(() => {
  discoveryStore.updateStore();
});
</script>

<style scoped />