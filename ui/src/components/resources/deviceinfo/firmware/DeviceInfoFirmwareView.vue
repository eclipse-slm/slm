<script setup lang="ts">
import {computed, ref, watch} from "vue";
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import RowWithLabel from "@/components/base/RowWithLabel.vue";
import formatDate from "@/utils/dateUtils";
import {storeToRefs} from "pinia";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import FirmwareUpdateStatusSection from "@/components/resources/deviceinfo/firmware/FirmwareUpdateStatusSection.vue";
import FirmwareCredentialsSection from "@/components/resources/deviceinfo/firmware/FirmwareCredentialsSection.vue";
import FirmwareAvailableVersionsSection from "@/components/resources/deviceinfo/firmware/FirmwareAvailableVersionsSection.vue";
import FirmwareUpdateJobsSection from "@/components/resources/deviceinfo/firmware/FirmwareUpdateJobsSection.vue";
import { SectionState, type SectionStateChangeEvent } from "@/components/resources/deviceinfo/firmware/sectionState";

const props = defineProps({
  resourceId: {
    type: String,
    required: true,
  },
});

const resourceDevicesStore = useResourceDevicesStore();


const sectionStates = ref({
  status: { state: SectionState.Loading } as SectionStateChangeEvent,
  credentials: { state: SectionState.Loading } as SectionStateChangeEvent,
  versions: { state: SectionState.Loading } as SectionStateChangeEvent,
  jobs: { state: SectionState.Loading } as SectionStateChangeEvent,
});
const {firmwareUpdateInformationOfResource, firmwareUpdateJobsOfResource} = storeToRefs(resourceDevicesStore)

const allSectionsLoaded = computed(() =>
  Object.values(sectionStates.value).every((value) => value.state === SectionState.Loaded)
);
const showOverallLoading = computed(() => !allSectionsLoaded.value);

function resetSectionStatus() {
  sectionStates.value = {
    status: { state: SectionState.Loading },
    credentials: { state: SectionState.Loading },
    versions: { state: SectionState.Loading },
    jobs: { state: SectionState.Loading },
  };
}

watch(
  () => props.resourceId,
  () => {
    resetSectionStatus();
    resourceDevicesStore.getFirmwareUpdateInformationOfResource(props.resourceId);
    resourceDevicesStore.getFirmwareUpdateJobsOfResource(props.resourceId);
  },
  { immediate: true }
);

const installedVersionText = computed(() => {
  const version = firmwareUpdateInformationOfResource.value(props.resourceId).currentFirmwareVersion?.version;
  if (!version) {
    return "N/A"
  }
  const date = firmwareUpdateInformationOfResource.value(props.resourceId).currentFirmwareVersion?.date;
  return date ? `${version} (${formatDate(date)})` : version;
});

function onSectionStateChanged(section: keyof typeof sectionStates.value, status: SectionStateChangeEvent) {
  sectionStates.value[section] = status;
}

</script>

<template>
  <div>
    <div v-if="showOverallLoading">
      <progress-circular />
    </div>

    <div v-show="!showOverallLoading">
      <RowWithLabel
        label="Installed version"
        :text="installedVersionText"
      />

      <FirmwareUpdateStatusSection
        :resource-id="props.resourceId"
        :firmware-update-information="firmwareUpdateInformationOfResource(props.resourceId)"
        :firmware-update-jobs="firmwareUpdateJobsOfResource(props.resourceId)"
        @activate-started="resourceDevicesStore.getFirmwareUpdateJobsOfResource(props.resourceId)"
        @state-changed="onSectionStateChanged('status', $event)"
      />

      <FirmwareCredentialsSection
        :resource-id="props.resourceId"
        @state-changed="onSectionStateChanged('credentials', $event)"
      />

      <FirmwareAvailableVersionsSection
        :resource-id="props.resourceId"
        :firmware-update-information="firmwareUpdateInformationOfResource(props.resourceId)"
        @update-started="resourceDevicesStore.getFirmwareUpdateJobsOfResource(props.resourceId)"
        @state-changed="onSectionStateChanged('versions', $event)"
      />

      <FirmwareUpdateJobsSection
        :firmware-update-jobs="firmwareUpdateJobsOfResource(props.resourceId)"
        :available-firmware-versions="firmwareUpdateInformationOfResource(props.resourceId).availableFirmwareVersions ?? []"
        @state-changed="onSectionStateChanged('jobs', $event)"
      />
    </div>
  </div>
</template>

<style scoped>
</style>