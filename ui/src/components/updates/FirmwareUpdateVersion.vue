<script setup lang="ts">

import FirmwareUpdateStatusIcon from "@/components/updates/FirmwareUpdateStatusIcon.vue";
import {onMounted, ref} from "vue";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import {storeToRefs} from "pinia";
import ProgressCircular from "@/components/base/ProgressCircular.vue";

const emit = defineEmits(['click']);

const props = defineProps({
  resourceId: {
    type: String,
    required: true,
  },
});

const resourceDevicesStore = useResourceDevicesStore();
const {firmwareUpdateInformationOfResource} = storeToRefs(resourceDevicesStore)

var loading = ref(false);

onMounted(() => {
  if (!firmwareUpdateInformationOfResource.value(props.resourceId)) {
    loading.value = true;
    resourceDevicesStore.getFirmwareUpdateInformationOfResource(props.resourceId).then(() => {
      loading.value = false;
    }).catch((error) => {
      console.error("Error fetching firmware update information:", error);
      loading.value = false;
    });
  }
});
</script>

<template>
  <div>
    <div v-if="loading">
      <progress-circular
        size="20"
        width="2"
      />
    </div>
    <div v-else>
      <v-row
        v-if="firmwareUpdateInformationOfResource(props.resourceId)"
        @click="$emit('click')"
      >
        <v-col cols="8">
          {{ firmwareUpdateInformationOfResource(props.resourceId).currentFirmwareVersion?.version }}
        </v-col>
        <v-col cols="3">
          <FirmwareUpdateStatusIcon
            :firmware-update-status="firmwareUpdateInformationOfResource(props.resourceId).firmwareUpdateStatus"
            @click="$emit('click')"
          />
        </v-col>
      </v-row>
    </div>
  </div>
</template>

<style scoped>

</style>