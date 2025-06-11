<script setup lang="ts">

const props = defineProps({
  capabilityService: {
    type: Object,
    required: true,
  },
});

const getChipIconByCapabilityService = (status) => {
  if(status === "READY")
    return "mdi-check"

  if(status === "INSTALL")
    return "mdi-plus"

  if(status === "UNINSTALL")
    return "mdi-minus"

  if(status === "UNKNOWN")
    return "mdi-help"

  if(status === "FAILED")
    return "mdi-alert-circle-outline"
}

</script>

<template>
  <v-chip
    class="ma-1"
  >
    <v-icon start>
      {{ getChipIconByCapabilityService(capabilityService.status) }}
    </v-icon>
    {{ capabilityService.capability.name }}
  </v-chip>
</template>

<style scoped>

</style>