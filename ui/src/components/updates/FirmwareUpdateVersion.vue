<script setup lang="ts">

import FirmwareUpdateStatusIcon from "@/components/updates/FirmwareUpdateStatusIcon.vue";
import {ref} from "vue";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";

const emit = defineEmits(['click']);

const props = defineProps({
  resourceId: {
    type: String,
    required: true,
  },
});

const resourceDevicesStore = useResourceDevicesStore();

const firmwareUpdateInformation = ref(undefined);

firmwareUpdateInformation.value = resourceDevicesStore.getFirmwareUpdateInformationOfResource(props.resourceId);


</script>

<template>
  <v-row
    v-if="firmwareUpdateInformation"
    @click="$emit('click')"
  >
    <v-col cols="8">
      {{ firmwareUpdateInformation.currentFirmwareVersion.version }}
    </v-col>
    <v-col cols="3">
      <FirmwareUpdateStatusIcon
        :firmware-update-status="firmwareUpdateInformation.firmwareUpdateStatus"
        @click="$emit('click')"
      />
    </v-col>
  </v-row>
</template>

<style scoped>

</style>