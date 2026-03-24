<script setup lang="ts">

import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import {computed, onMounted, ref} from "vue";
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import RowWithLabel from "@/components/base/RowWithLabel.vue";
import {formatDateTime} from "@/utils/dateUtils";
import {useToast} from "vue-toast-notification";
import {storeToRefs} from "pinia";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import VCodeBlock from '@wdns/vue-code-block';
import {CapabilityJobDTO} from "@/api/resource-management/client";
import {useCapabilitiesStore} from "@/stores/capabilitiesStore";
import logRequestError from "@/api/restApiHelper";
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";

const $toast = useToast();

const props = defineProps({
  resourceId: {
    type: String,
    required: true,
  },
});

const resourceDevicesStore = useResourceDevicesStore();
const capabilitiesStore = useCapabilitiesStore();
const {resourceById} = storeToRefs(resourceDevicesStore)
const {capabilityById, capabilityServiceById} = storeToRefs(capabilitiesStore)

const loading = ref(false);
const capabilityJobs = ref<CapabilityJobDTO[]>([])
const showConfirmCapabilityDeleteDialog = ref(false);

onMounted(() => {
  loading.value = true;

  ResourceManagementClient.capabilityApi.getCapabilityJobsOfResource(props.resourceId).then((response) => {
    capabilityJobs.value = response.data
  }).catch(e => {
    logRequestError(e)
    console.error("Error loading capability jobs:", e)
  }).finally(() => {
    loading.value = false;
  })
});

const capabilityServicesTableHeaders = [
  { title: 'Capability Name', key: 'capabilityName', width: "20%" },
  { title: 'Status', key: 'status', width: "10%" },
  { title: 'Capability Id', key: 'capabilityId', width: "30%" },
  { title: 'Capability Service Id', key: 'capabilityServiceId', width: "30%" },
  { title: 'Actions', key: 'actions', width: "10%" },
];

const jobTableHeaders = [
  { title: 'Created', key: 'created', width: "20%" },
  { title: 'Capability', key: 'capability', width: "20%" },
  { title: 'State', key: 'state', width: "20%" },
  { title: 'Job Id', key: 'id', width: "40%" },
];

const logCollapsedStates = ref<Record<string, boolean>>({});
function toggleLogCollapse(firmwareUpdateJobId: string) {
  logCollapsedStates.value[firmwareUpdateJobId] = !logCollapsedStates.value[firmwareUpdateJobId];
}
const capabilityJobLogMessages = computed(() => (capabilityJobId: string) => {
  const job = capabilityJobs.value?.find(j => j.id === capabilityJobId);
  if (job?.logMessages?.length) {
    return job.logMessages.join('\n');
  }
  return "No messages available";
});

const capabilityServiceIdToDelete = ref<string>("");
const onCapabilityDeletedConfirmed = () => {
  showConfirmCapabilityDeleteDialog.value = false;
  ResourceManagementClient.capabilityApi.removeCapabilityFromSingleHost(props.resourceId, capabilityServiceById.value(capabilityServiceIdToDelete.value)?.capabilityId ?? "")
    .then(() => {
      $toast.info(`Capability '${capabilityById.value(capabilityServiceById.value(capabilityServiceIdToDelete.value)?.capabilityId)?.name}' removal initiated`);
    })
    .catch((e) => {
      logRequestError(e);
      $toast.error(`Failed to remove capability '${capabilityById.value(capabilityServiceById.value(capabilityServiceIdToDelete.value)?.capabilityId ?? "")?.name}'`);
    });
  capabilityServiceIdToDelete.value = ""
};

</script>

<template>
  <div>
    <div v-if="loading">
      <progress-circular />
    </div>

    <div v-else>
      <RowWithLabel
        label="Installed capabilities"
      >
        <template #content>
          <div v-if="resourceById(resourceId)?.capabilityServiceIds?.length === 0">
            No capabilities installed
          </div>

          <v-data-table
              v-else
              :headers="capabilityServicesTableHeaders"
              :items="resourceById(resourceId)?.capabilityServiceIds as string[]"
              hide-default-footer
              items-per-page="-1"
              class="elevation-0"
          >
            <template #item.capabilityName="{ item }">
              <v-icon color="black" class="mr-3">
                {{ capabilityById(capabilityServiceById(item)?.capabilityId)?.logo }}
              </v-icon>
              <span>{{ capabilityById(capabilityServiceById(item)?.capabilityId)?.name }}</span>
            </template>
            <template #item.status="{ item }">
              <span>{{ capabilityServiceById(item)?.status }}</span>
            </template>
            <template #item.capabilityId="{ item }">
              <span>{{ capabilityServiceById(item)?.capabilityId }}</span>
            </template>
            <template #item.capabilityServiceId="{ item }">
              <span>{{ item }}</span>
            </template>
            <template #item.actions="{ item }">
              <v-btn
                  color="error"
                  @click.stop="showConfirmCapabilityDeleteDialog = true; capabilityServiceIdToDelete = item;"
                  class="ma-2"
                  size="small"
              >
                <v-icon
                    icon="mdi-trash-can"
                    color="white"
                />
              </v-btn>
              <confirm-dialog
                  :show="showConfirmCapabilityDeleteDialog"
                  title="Remove capability"
                  :text="`Do you want to remove the capability '${capabilityById(capabilityServiceById(capabilityServiceIdToDelete)?.capabilityId)?.name}'?`"
                  confirm-button-label="Remove"
                  @canceled="showConfirmCapabilityDeleteDialog = false"
                  @confirmed="onCapabilityDeletedConfirmed"
              ></confirm-dialog>
            </template>
          </v-data-table>
        </template>
      </RowWithLabel>

      <RowWithLabel
        label="Capability jobs"
        :divider="true"
      >
        <template #content>
          <div v-if="capabilityJobs?.length === 0">
            No jobs available
          </div>
          <v-data-table
            v-else
            :headers="jobTableHeaders"
            :items="capabilityJobs"
            item-key="id"
            :sort-by="[{ key: 'createdAt', order: 'desc' }]"
            show-expand
            class="elevation-0"
          >
            <template #item.state="{ item }">
              <span>{{ item.state }}</span>
            </template>
            <template #item.capability="{ item }">
              <span>{{ capabilityById(item.capabilityId).name }}</span>
            </template>
            <template #item.created="{ item }">
              <span>{{ formatDateTime(item.createdAt) }}</span>
            </template>
            <template #expanded-row="{ item }">
              <td
                class="ma-4"
                :colspan="jobTableHeaders.length+1"
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

                  <v-row class="align-center mb-2">
                    <span class="mr-2">Log</span>
                    <v-icon
                        style="cursor: pointer;"
                        @click="toggleLogCollapse(item.id)"
                        size="small"
                    >
                      {{ !logCollapsedStates[item.id] ? 'mdi-chevron-down' : 'mdi-chevron-up' }}
                    </v-icon>
                  </v-row>
                  <v-expand-transition>
                    <div v-show="logCollapsedStates[item.id]">
                  <VCodeBlock
                    :code="capabilityJobLogMessages(item.id)"
                    prismjs
                    lang="html"
                    theme="coy"
                  />
                    </div>
                  </v-expand-transition>
                </div>
                <div v-else>
                  No state transitions
                </div>
              </td>
            </template>
          </v-data-table>
        </template>
      </RowWithLabel>
    </div>
  </div>
</template>

<style scoped>
.v-divider {
  border-color: #000000;
}
</style>