<script setup lang="ts">

import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import {computed, onMounted, Ref, ref} from "vue";
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import RowWithLabel from "@/components/base/RowWithLabel.vue";
import FirmwareUpdateStatusIcon from "@/components/updates/FirmwareUpdateStatusIcon.vue";
import formatDate, {formatDateTime} from "@/utils/dateUtils";
import axios from 'axios'
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";
import {useToast} from "vue-toast-notification";
import {storeToRefs} from "pinia";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import VCodeBlock from '@wdns/vue-code-block';
import {FirmwareUpdateJobState} from "@/api/resource-management/client";

const $toast = useToast();

const props = defineProps({
  resourceId: {
    type: String,
    required: true,
  },
});

const resourceDevicesStore = useResourceDevicesStore();

const loading = ref(false);
const {firmwareUpdateInformationOfResource, firmwareUpdateJobsOfResource} = storeToRefs(resourceDevicesStore)

onMounted(() => {
  loading.value = true;
  resourceDevicesStore.getFirmwareUpdateInformationOfResource(props.resourceId).then(
      response => {
        loading.value = false;
      }
  ).catch(e => {
    loading.value = false;
  });
  resourceDevicesStore.getFirmwareUpdateJobsOfResource(props.resourceId);
});

const filesTableHeaders = [
  { title: 'Name', key: 'fileName', value: 'fileName' },
  { title: 'Size', key: 'fileSize', value: 'fileSizeBytes' },
  { title: 'Date', key: 'fileUploadDate', value: 'uploadDate' },
  { title: 'Actions', key: 'fileActions', value: 'fileActions' },
];

const jobTableHeaders = [
  { title: 'Created', key: 'create', value: 'createdAt', width: "20%" },
  { title: 'Target Version', key: 'version', value: 'version', width: "20%" },
  { title: 'State', key: 'state',  value: 'firmwareUpdateState', width: "20%" },
  { title: 'Job Id', key: 'id', value: 'id', width: "40%" },
];

const installedVersionText = computed(() => {
  const version = firmwareUpdateInformationOfResource.value(props.resourceId).currentFirmwareVersion?.version;
  if (!version) {
    return "N/A"
  }
  const date = firmwareUpdateInformationOfResource.value(props.resourceId).currentFirmwareVersion?.date;
  return date ? `${version} (${formatDate(date)})` : version;
});

function humanFileSize(size) {
  var i = size == 0 ? 0 : Math.floor(Math.log(size) / Math.log(1024));
  return +((size / Math.pow(1024, i)).toFixed(2)) * 1 + ' ' + ['B', 'kB', 'MB', 'GB', 'TB'][i];
}

function downloadItem (item) {
  axios.get(item.downloadUrl, { responseType: 'blob' })
      .then(response => {
        const blob = new Blob([response.data])
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = item.fileName
        link.click()
        URL.revokeObjectURL(link.href)
      }).catch(console.error)
}

const selectedFirmwareVersion = ref(undefined);
const showConfirmFirmwareUpdateInstallation = ref(false);
function installFirmwareUpdate() {
  let softwareNameplateSubmodelIdBase64Encoded = btoa(selectedFirmwareVersion.value.softwareNameplateSubmodelId);
  ResourceManagementClient.resourcesUpdatesApi.startFirmwareUpdateOnResource(
      props.resourceId,
      softwareNameplateSubmodelIdBase64Encoded
  ).then(() => {
    resourceDevicesStore.getFirmwareUpdateJobsOfResource(props.resourceId);     // Reload jobs
    $toast.info("Firmware update preparation started");
  }).catch(e => {;
    console.error("Error starting firmware update preparation:", e);
    $toast.error("Failed to start firmware update preparation");
  });

  selectedFirmwareVersion.value = undefined;
}

function activateFirmwareUpdate(firmwareUpdateJobId) {
  ResourceManagementClient.resourcesUpdatesApi.activateFirmwareUpdateOnResource(
      props.resourceId,
      firmwareUpdateJobId
  ).then(() => {
    $toast.info("Firmware update activation started");
  }).catch(e => {
    console.error("Error starting firmware update activation:", e);
    $toast.error("Failed to start firmware update activation");
  });
}

function getVersionTextOfSoftwareNameplate(softwareNameplateSubmodelId) {
  const version = firmwareUpdateInformationOfResource.value(props.resourceId).availableFirmwareVersions?.find(
      firmwareVersion =>
          firmwareVersion.softwareNameplateSubmodelId?.trim() === softwareNameplateSubmodelId?.trim()
  )?.version;
  return version ?? "N/A";
}

const logCollapsedStates = ref<Record<string, boolean>>({});
function toggleLogCollapse(firmwareUpdateJobId: string) {
  logCollapsedStates.value[firmwareUpdateJobId] = !logCollapsedStates.value[firmwareUpdateJobId];
}
const firmwareUpdateJobLogMessages = computed(() => (firmwareUpdateJobId: string) => {
  const jobs = firmwareUpdateJobsOfResource.value(props.resourceId);
  const job = jobs?.find(j => j.id === firmwareUpdateJobId);
  if (job?.logMessages?.length) {
    return job.logMessages.join('\n');
  }
  return "No messages available";
});
</script>

<template>
  <div>
    <div v-if="loading">
      <progress-circular />
    </div>

    <div v-else>
      <RowWithLabel
        label="Installed version"
        :text="installedVersionText"
      />

      <RowWithLabel
        label="Update status"
      >
        <template #content>
          <v-row
            v-if="firmwareUpdateInformationOfResource(props.resourceId).isUpdateInProgress"
            class="ma-1"
          >
            <div v-if="firmwareUpdateJobsOfResource(props.resourceId)[0].state !== FirmwareUpdateJobState.Prepared">
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
                {{ firmwareUpdateJobsOfResource(props.resourceId)[0].state }}
              </v-chip>
            </div>
            <div v-else>
              Firmware update to version {{ getVersionTextOfSoftwareNameplate(firmwareUpdateJobsOfResource(props.resourceId)[0].softwareNameplateId) }} prepared

              <v-icon class="mx-2">
                mdi-arrow-right
              </v-icon>
              <v-btn
                class="mx-2"
                color="primary"
                size="small"
                @click="activateFirmwareUpdate(firmwareUpdateJobsOfResource(props.resourceId)[0].id)"
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
              :firmware-update-status="firmwareUpdateInformationOfResource(props.resourceId).firmwareUpdateStatus"
              :clickable="false"
            />
          </v-row>
        </template>
      </RowWithLabel>

      <RowWithLabel
        label="Available versions"
        :divider="true"
      >
        <template #content>
          <div v-if="firmwareUpdateInformationOfResource(props.resourceId).availableFirmwareVersions?.length == 0">
            No updates available
          </div>
          <v-expansion-panels
            v-else
            variant="accordion"
            flat
          >
            <v-expansion-panel
              v-for="firmwareVersion in firmwareUpdateInformationOfResource(props.resourceId).availableFirmwareVersions"
              :key="firmwareVersion.version"
              expand
            >
              <template #title>
                <div style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
                  <v-row>
                    <v-col cols="11">
                      <span>{{ firmwareVersion.version }}</span>
                    </v-col>
                    <v-col cols="1">
                      <v-btn
                        v-if="firmwareVersion.version === firmwareUpdateInformationOfResource(props.resourceId)?.currentFirmwareVersion?.version"
                        color="secondary"
                        size="small"
                      >
                        Installed
                      </v-btn>
                      <div v-else>
                        <v-btn
                          v-if="firmwareVersion.firmwareUpdateFile"
                          color="primary"
                          size="small"
                          :disabled="firmwareUpdateInformationOfResource(props.resourceId).isUpdateInProgress"
                          @click.stop="selectedFirmwareVersion = firmwareVersion; showConfirmFirmwareUpdateInstallation = true;"
                        >
                          Install
                          <ConfirmDialog
                            :show="showConfirmFirmwareUpdateInstallation"
                            :title="`Install firmware update`"
                            :text="`Do you want to update the firmware of the device to version '${selectedFirmwareVersion?.version}'?`"
                            @confirmed="showConfirmFirmwareUpdateInstallation = false; installFirmwareUpdate()"
                            @canceled="showConfirmFirmwareUpdateInstallation = false"
                          />
                        </v-btn>
                      </div>
                    </v-col>
                  </v-row>
                </div>
              </template>
              <template #text>
                <RowWithLabel
                  label="Version"
                  :text="firmwareVersion.version"
                />
                <RowWithLabel
                  label="Date"
                  :text="firmwareVersion.date"
                />
                <RowWithLabel
                  label="Installation URI"
                >
                  <template #content>
                    <a
                      :href="firmwareVersion.installationUri"
                      target="_blank"
                    >{{ firmwareVersion.installationUri }}</a>
                  </template>
                </RowWithLabel>
                <RowWithLabel
                  label="Checksum"
                  :text="firmwareVersion.installationChecksum"
                />
                <RowWithLabel
                  label="File"
                >
                  <template #content>
                    <v-data-table
                      v-if="firmwareVersion.firmwareUpdateFile"
                      :headers="filesTableHeaders"
                      :items="[ firmwareVersion.firmwareUpdateFile ]"
                      hide-default-footer
                    >
                      <template #item.fileSize="{ item }">
                        {{ humanFileSize(item.fileSizeBytes) }}
                      </template>
                      <template #item.fileUploadDate="{ item }">
                        {{ formatDate(item.uploadDate) }}
                      </template>
                      <template #item.fileActions="{ item }">
                        <v-icon
                          class="ml-4"
                          color="secondary"
                          @click.prevent="downloadItem(item)"
                        >
                          mdi-download
                        </v-icon>
                      </template>
                    </v-data-table>
                    <div v-else>
                      No file available
                    </div>
                  </template>
                </RowWithLabel>
              </template>
            </v-expansion-panel>
          </v-expansion-panels>
        </template>
      </RowWithLabel>

      <RowWithLabel
        label="Update jobs"
        :divider="true"
      >
        <template #content>
          <div v-if="firmwareUpdateJobsOfResource(props.resourceId)?.length === 0">
            No jobs available
          </div>
          <v-data-table
            v-else
            :headers="jobTableHeaders"
            :items="firmwareUpdateJobsOfResource(props.resourceId)"
            item-key="id"
            :sort-by="[{ key: 'createdAt', order: 'desc' }]"
            show-expand
            class="elevation-0"
          >
            <template #item.state="{ item }">
              <span>{{ item.firmwareUpdateState }}</span>
            </template>
            <template #item.version="{ item }">
              <span>{{ getVersionTextOfSoftwareNameplate(item.softwareNameplateId) }}</span>
            </template>
            <template #item.create="{ item }">
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
                    :code="firmwareUpdateJobLogMessages(item.id)"
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