<script setup lang="ts">

import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";
import {onMounted, ref} from "vue";
import {UpdateInformationResourceType} from "@/api/resource-management/client";
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import RowWithLabel from "@/components/base/RowWithLabel.vue";
import formatDate from "@/utils/dateUtils";
import axios from 'axios'
import {useToast} from "vue-toast-notification";
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";

const $toast = useToast();

const props = defineProps({
  resourceType: {
    type: Object,
    required: true,
  },
});

const loading = ref(false);
const updateInformation = ref<UpdateInformationResourceType>({});
onMounted(() => {
  loading.value = true;
  ResourceManagementClient.resourcesUpdatesApi.getUpdateInformationOfResourceType(props.resourceType.typeName)
      .then(
          response => {
            updateInformation.value = response.data;
            loading.value = false;
          }
      ).catch(e => {
        logRequestError(e)
        loading.value = false;
      }
  )
});

const tableHeadersFiles = [
  { title: 'Name', key: 'fileName', value: 'fileName' },
  { title: 'Size', key: 'fileSize', value: 'fileSizeBytes' },
  { title: 'Date', key: 'fileUploadDate', value: 'uploadDate' },
  { title: 'Actions', key: 'fileActions', value: 'fileActions' },
];

function humanFileSize(size) {
  var i = size == 0 ? 0 : Math.floor(Math.log(size) / Math.log(1024));
  return +((size / Math.pow(1024, i)).toFixed(2)) * 1 + ' ' + ['B', 'kB', 'MB', 'GB', 'TB'][i];
}

function downloadFile (item) {
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


const firmwareDownloadFromVendorActive = ref(false);
const showConfirmFirmwareUpdateDownloadFromVendorDialog = ref(false);
const firmwareUpdateDownloadFromVendorConfirmed = ref(false);
function triggerFileDownloadFromVendor (firmwareVersion) {
  if (firmwareVersion.firmwareUpdateFile && !firmwareUpdateDownloadFromVendorConfirmed.value) {
    showConfirmFirmwareUpdateDownloadFromVendorDialog.value = true
    return;
  }

  let softwareNameplateIdBase64Encoded = btoa(firmwareVersion.softwareNameplateSubmodelId)
  firmwareDownloadFromVendorActive.value = true;
  ResourceManagementClient.resourcesUpdatesApi.downloadFirmwareUpdateFileFromVendor(softwareNameplateIdBase64Encoded)
      .then(() => {
        firmwareDownloadFromVendorActive.value = false;
        $toast.info("Firmware successfully downloaded from vendor");
        refreshUpdateInformation();   // Refresh the update information after file download
      })
      .catch(error => {
        firmwareDownloadFromVendorActive.value = false;
        $toast.error("Failed to download firmware update from vendor");
        logRequestError(error)
      })

  firmwareUpdateDownloadFromVendorConfirmed.value = false;
}

// File handling

const selectedFile = ref<File | null>(null);

function onFileSelected(file: File) {
  selectedFile.value = file;
}

async function uploadFile(softwareNameplateId) {
  if (!selectedFile.value) return;
  const formData = new FormData();
  formData.append("file", selectedFile.value);

  try {
    let softwareNameplateIdBase64Encoded = btoa(softwareNameplateId)
    ResourceManagementClient.resourcesUpdatesApi.addOrUpdateFirmwareUpdateFile(softwareNameplateIdBase64Encoded, selectedFile.value)
        .then(() => {
          $toast.info("File uploaded successfully");
          selectedFile.value = null;
          refreshUpdateInformation();   // Refresh the update information after upload
        })
  } catch (e) {
    $toast.error("Failed to upload file");
    console.log(e)
  }
}

function deleteItem (softwareNameplateSubmodelId) {
  let softwareNameplateIdBase64Encoded = btoa(softwareNameplateSubmodelId)
  ResourceManagementClient.resourcesUpdatesApi.deleteFirmwareUpdateFile(softwareNameplateIdBase64Encoded).then(
      () => {
        refreshUpdateInformation();   // Refresh the update information after deletion
      }
  )
}

function refreshUpdateInformation() {
  ResourceManagementClient.resourcesUpdatesApi.getUpdateInformationOfResourceType(props.resourceType.typeName)
      .then(response => {
        updateInformation.value = response.data;
      }).catch(logRequestError);
}

</script>

<template>
  <div>
    <div v-if="loading">
      <progress-circular />
    </div>

    <div v-else>
      <RowWithLabel
        label="Available versions"
        :divider="false"
      />
      <v-expansion-panels
        variant="accordion"
        flat
      >
        <v-expansion-panel
          v-for="firmwareVersion in updateInformation.availableFirmwareVersions"
          :key="firmwareVersion.version"
          :title="firmwareVersion.version"
          expand
        >
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

                <v-icon
                    v-if="!firmwareDownloadFromVendorActive"
                    class="ml-4"
                    color="secondary"
                    @click.prevent="triggerFileDownloadFromVendor(firmwareVersion)"
                >
                  mdi-download
                </v-icon>
                <v-progress-circular
                    v-if="firmwareDownloadFromVendorActive"
                    indeterminate
                    class="mx-5"
                    color="secondary"
                    size="16"
                    width="2"/>
                <ConfirmDialog
                    :show="showConfirmFirmwareUpdateDownloadFromVendorDialog"
                    title="Firmware update file already exists"
                    text="Do you want to replace the existing firmware update file?"
                    @confirmed="showConfirmFirmwareUpdateDownloadFromVendorDialog = false; firmwareUpdateDownloadFromVendorConfirmed = true; triggerFileDownloadFromVendor(firmwareVersion)"
                    @canceled="showConfirmFirmwareUpdateDownloadFromVendorDialog = false; firmwareUpdateDownloadFromVendorConfirmed = false;"
                />
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
                  :headers="tableHeadersFiles"
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
                      @click.prevent="downloadFile(item)"
                    >
                      mdi-download
                    </v-icon>

                    <v-icon
                      class="ml-4"
                      color="error"
                      @click="deleteItem(firmwareVersion.softwareNameplateSubmodelId)"
                    >
                      mdi-delete
                    </v-icon>
                  </template>
                </v-data-table>
                <div v-else>
                    <v-row align="center">
                      <v-col>
                        <v-file-input
                            v-model="selectedFile"
                            label="Upload firmware update file"
                            variant="underlined"
                        />
                      </v-col>
                      <v-col>
                        <v-btn
                            color="info"
                            :disabled="!selectedFile"
                            @click="uploadFile(firmwareVersion.softwareNameplateSubmodelId)"
                        >
                          <v-icon
                              icon="mdi-upload"
                              color="white"
                          />
                        </v-btn>
                      </v-col>
                    </v-row>
                </div>
              </template>
            </RowWithLabel>
          </template>
        </v-expansion-panel>
      </v-expansion-panels>
    </div>
  </div>
</template>

<style scoped>
</style>