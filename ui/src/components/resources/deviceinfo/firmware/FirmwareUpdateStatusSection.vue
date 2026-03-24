<script setup lang="ts">
import { computed, nextTick, watch } from "vue";
import { useToast } from "vue-toast-notification";
import RowWithLabel from "@/components/base/RowWithLabel.vue";
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import FirmwareUpdateStatusIcon from "@/components/updates/FirmwareUpdateStatusIcon.vue";
import { FirmwareUpdateJobState } from "@/api/resource-management/client";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import { getVersionTextOfSoftwareNameplate } from "@/utils/firmwareUtils";
import type { PropType } from "vue";
import { SectionState, type SectionStateChangeEvent } from "@/components/resources/deviceinfo/firmware/sectionState";

const $toast = useToast();

const props = defineProps({
  resourceId: {
    type: String,
    required: true,
  },
  firmwareUpdateInformation: {
    type: Object as PropType<any>,
    required: true,
  },
  firmwareUpdateJobs: {
    type: Array as PropType<any[]>,
    required: true,
  },
});


const emit = defineEmits<{ "activate-started": []; stateChanged: [SectionStateChangeEvent] }>();

const firstJob = computed(() => props.firmwareUpdateJobs?.[0]);
const availableVersions = computed(
  () => props.firmwareUpdateInformation?.availableFirmwareVersions ?? []
);
const hasUpdateInfo = computed(() => !!props.firmwareUpdateInformation);

watch(
  () => [props.resourceId, props.firmwareUpdateInformation, props.firmwareUpdateJobs],
  async () => {
    emit("stateChanged", { state: SectionState.Loading });
    await nextTick();
    if (hasUpdateInfo.value) {
      emit("stateChanged", { state: SectionState.Loaded });
    }
  },
  { immediate: true }
);

function activateFirmwareUpdate(firmwareUpdateJobId: string) {
  ResourceManagementClient.resourcesUpdatesApi
    .activateFirmwareUpdateOnResource(props.resourceId, firmwareUpdateJobId)
    .then(() => {
      $toast.info("Firmware update activation started");
      emit("activate-started");
    })
    .catch((e) => {
      console.error("Error starting firmware update activation:", e);
      $toast.error("Failed to start firmware update activation");
    });
}
</script>

<template>
  <RowWithLabel label="Update status">
    <template #content>
      <v-row
        v-if="firmwareUpdateInformation.isUpdateInProgress"
        class="ma-1"
      >
        <div v-if="firstJob?.state !== FirmwareUpdateJobState.Prepared">
          <progress-circular
            size="20"
            width="2"
          />
          <v-chip
            color="primary"
            variant="elevated"
            class="mx-8"
            label
            size="small"
          >
            {{ firstJob?.state }}
          </v-chip>
        </div>
        <div v-else>
          Firmware update to version
          {{ getVersionTextOfSoftwareNameplate(availableVersions, firstJob?.softwareNameplateId) }}
          prepared

          <v-icon class="mx-2">
            mdi-arrow-right
          </v-icon>
          <v-btn
            class="mx-2"
            color="primary"
            size="small"
            @click="activateFirmwareUpdate(firstJob.id)"
          >
            Activate
          </v-btn>
        </div>
      </v-row>

      <v-row
        v-else
        class="ma-1"
      >
        <FirmwareUpdateStatusIcon
          :firmware-update-status="firmwareUpdateInformation.firmwareUpdateStatus"
          :clickable="false"
        />
      </v-row>
    </template>
  </RowWithLabel>
</template>
