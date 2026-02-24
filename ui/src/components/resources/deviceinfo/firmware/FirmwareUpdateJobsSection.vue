<script setup lang="ts">
import { computed, nextTick, ref, watch } from "vue";
import RowWithLabel from "@/components/base/RowWithLabel.vue";
import VCodeBlock from "@wdns/vue-code-block";
import { formatDateTime } from "@/utils/dateUtils";
import { getVersionTextOfSoftwareNameplate } from "@/utils/firmwareUtils";
import { SectionState, type SectionStateChangeEvent } from "@/components/resources/deviceinfo/firmware/sectionState";
import type { PropType } from "vue";

const props = defineProps({
  firmwareUpdateJobs: {
    type: Array as PropType<any[]>,
    required: true,
  },
  availableFirmwareVersions: {
    type: Array as PropType<any[]>,
    required: true,
  },
});

const emit = defineEmits<{ stateChanged: [SectionStateChangeEvent] }>();

const hasJobs = computed(() => Array.isArray(props.firmwareUpdateJobs));

watch(
  () => [props.firmwareUpdateJobs, props.availableFirmwareVersions],
  async () => {
    emit("stateChanged", { state: SectionState.Loading });
    await nextTick();
    if (hasJobs.value) {
      emit("stateChanged", { state: SectionState.Loaded });
    }
  },
  { immediate: true }
);

const jobTableHeaders = [
  { title: "Created", key: "create", value: "createdAt", width: "20%" },
  { title: "Target Version", key: "version", value: "version", width: "20%" },
  { title: "State", key: "state", value: "firmwareUpdateState", width: "20%" },
  { title: "Job Id", key: "id", value: "id", width: "40%" },
];

const logCollapsedStates = ref<Record<string, boolean>>({});
function toggleLogCollapse(firmwareUpdateJobId: string) {
  logCollapsedStates.value[firmwareUpdateJobId] =
    !logCollapsedStates.value[firmwareUpdateJobId];
}

const firmwareUpdateJobLogMessages = computed(() => (firmwareUpdateJobId: string) => {
  const job = props.firmwareUpdateJobs?.find((j) => j.id === firmwareUpdateJobId);
  if (job?.logMessages?.length) {
    return job.logMessages.join("\n");
  }
  return "No messages available";
});
</script>

<template>
  <RowWithLabel
    label="Update jobs"
    :divider="true"
  >
    <template #content>
      <div v-if="firmwareUpdateJobs?.length === 0">
        No jobs available
      </div>
      <v-data-table
        v-else
        :headers="jobTableHeaders"
        :items="firmwareUpdateJobs"
        item-key="id"
        :sort-by="[{ key: 'createdAt', order: 'desc' }]"
        show-expand
        class="elevation-0"
      >
        <template #item.state="{ item }">
          <span>{{ item.firmwareUpdateState }}</span>
        </template>
        <template #item.version="{ item }">
          <span>{{ getVersionTextOfSoftwareNameplate(availableFirmwareVersions, item.softwareNameplateId) }}</span>
        </template>
        <template #item.create="{ item }">
          <span>{{ formatDateTime(item.createdAt) }}</span>
        </template>
        <template #expanded-row="{ item }">
          <td
            class="ma-4"
            :colspan="jobTableHeaders.length + 1"
          >
            <div
              v-if="item.stateTransitions?.length > 0"
              class="mx-4"
            >
              <v-timeline direction="horizontal">
                <v-timeline-item
                  v-for="transition in item.stateTransitions"
                  :key="transition.id"
                  dot-color="primary"
                >
                  <div>
                    <strong>{{ transition.toState }}</strong>
                    <div>{{ formatDateTime(transition.timestamp) }}</div>
                  </div>
                </v-timeline-item>
              </v-timeline>

              <v-divider></v-divider>
              <RowWithLabel
                  v-if="item.failureReason"
                label="Failure Reason"
                :text="item.failureReason || 'N/A'"
              ></RowWithLabel>

              <RowWithLabel
                label="Driver Log"
              >
                <template #content>
                  <span v-if="!logCollapsedStates[item.id]">Show</span>
                  <span v-else>Hide</span>
                  <v-icon
                    style="cursor: pointer;"
                    @click="toggleLogCollapse(item.id)"
                    size="small"
                  >
                    {{ !logCollapsedStates[item.id] ? "mdi-chevron-down" : "mdi-chevron-up" }}
                  </v-icon>
                <v-expand-transition>
                  <div v-show="logCollapsedStates[item.id]">
                    <VCodeBlock
                      :code="firmwareUpdateJobLogMessages(item.id)"
                      prismjs
                      lang="html"
                      theme="coy"
                    />
                  </div>
              </v-expand-transition>
                </template>
              </RowWithLabel>
            </div>
            <div v-else>
              No state transitions
            </div>
          </td>
        </template>
      </v-data-table>
    </template>
  </RowWithLabel>
</template>
