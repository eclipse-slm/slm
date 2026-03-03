<script setup lang="ts">
import { ref, computed } from "vue";
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import { SectionState, type SectionStateChangeEvent } from "@/components/resources/deviceinfo/firmware/sectionState";
import DeviceInfoCommonBasicsSection from "@/components/resources/deviceinfo/common/DeviceInfoCommonBasicsSection.vue";
import DeviceInfoCommonRemoteAccessSection from "@/components/resources/deviceinfo/common/DeviceInfoCommonRemoteAccessSection.vue";

const props = defineProps({
  resourceId: {
    type: String,
    default: "",
  },
});

const sectionStatus = ref({
  basics: { state: SectionState.Loaded } as SectionStateChangeEvent,
  remoteAccess: { state: SectionState.Loading } as SectionStateChangeEvent,
});

const allSectionsLoaded = computed(() =>
  Object.values(sectionStatus.value).every((value) => value.state === SectionState.Loaded)
);

function setSectionStatus(section: keyof typeof sectionStatus.value, status: SectionStateChangeEvent) {
  sectionStatus.value[section] = status;
}
</script>

<template>
  <v-container fluid>
    <div v-if="!allSectionsLoaded">
      <progress-circular />
    </div>

    <div v-show="allSectionsLoaded">
      <DeviceInfoCommonBasicsSection :resource-id="resourceId" />
      <DeviceInfoCommonRemoteAccessSection
        :resource-id="resourceId"
        @state-changed="setSectionStatus('remoteAccess', $event)"
      />
    </div>
  </v-container>
</template>
