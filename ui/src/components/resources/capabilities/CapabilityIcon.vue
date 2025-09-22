<script setup lang="ts">
import { computed } from 'vue'
import {useCapabilitiesStore} from "@/stores/capabilitiesStore";
import {CapabilityServiceDTO} from "@/api/resource-management/client";
import {storeToRefs} from "pinia";

const props = defineProps<{
  capabilityService: CapabilityServiceDTO
}>()

const capabilitiesStore = useCapabilitiesStore();
const { capabilityById } = storeToRefs(capabilitiesStore);

const chipIcon = computed(() => {
  switch (props.capabilityService.status) {
    case 'READY':
      return 'mdi-check'
    case 'INSTALL':
      return 'mdi-plus'
    case 'UNINSTALL':
      return 'mdi-minus'
    case 'UNKNOWN':
      return 'mdi-help'
    case 'FAILED':
      return 'mdi-alert-circle-outline'
    default:
      return ''
  }
})
</script>

<template>
  <div v-if="capabilityById(props.capabilityService?.capabilityId)?.name">
    <v-chip class="ma-1">
      <v-icon start>
        {{ chipIcon }}
      </v-icon>
      {{ capabilityById(props.capabilityService.capabilityId).name }}
    </v-chip>
  </div>
</template>

<style scoped>
</style>