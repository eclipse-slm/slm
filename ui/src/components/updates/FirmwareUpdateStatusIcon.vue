<script setup lang="ts">

import {FirmwareUpdateStatus} from "@/api/resource-management/client";

const emit = defineEmits(['click']);

const props = defineProps({
  firmwareUpdateStatus: {
    type: String,
    required: false,
    default: FirmwareUpdateStatus.Unknown,
  },
  clickable: {
    type: Boolean,
    default: true,
  },
});

</script>

<template>
  <v-btn
    variant="text"
    size="x-small"
    :class="{ 'non-clickable': !clickable }"
    :tabindex="clickable ? 0 : -1"
    @click.stop="$emit('click')"
  >
    <v-icon
      v-if="firmwareUpdateStatus === FirmwareUpdateStatus.UpToDate"
      color="green"
    >
      mdi-check-circle-outline
    </v-icon>
    <v-icon
      v-else-if="firmwareUpdateStatus === FirmwareUpdateStatus.UpdateAvailable"
      color="orange"
    >
      mdi-upload-circle-outline
    </v-icon>
    <v-icon
      v-else
      color="blue"
    >
      mdi-help-circle-outline
    </v-icon>
  </v-btn>
</template>

<style scoped>
.non-clickable {
  pointer-events: none;
}
</style>