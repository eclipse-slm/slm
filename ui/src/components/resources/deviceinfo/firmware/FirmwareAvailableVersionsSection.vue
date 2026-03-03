<script setup lang="ts">
import { computed, nextTick, ref, watch } from "vue";
import { useToast } from "vue-toast-notification";
import RowWithLabel from "@/components/base/RowWithLabel.vue";
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import formatDate from "@/utils/dateUtils";
import { downloadItem, humanFileSize } from "@/utils/firmwareUtils";
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
});


const emit = defineEmits<{ "update-started": []; stateChanged: [SectionStateChangeEvent] }>();

const hasUpdateInfo = computed(() => !!props.firmwareUpdateInformation);

watch(
  () => [props.resourceId, props.firmwareUpdateInformation],
  async () => {
    emit("stateChanged", { state: SectionState.Loading });
    await nextTick();
    if (hasUpdateInfo.value) {
      emit("stateChanged", { state: SectionState.Loaded });
    }
  },
  { immediate: true }
);

const filesTableHeaders = [
  { title: "Name", key: "fileName", value: "fileName" },
  { title: "Size", key: "fileSize", value: "fileSizeBytes" },
  { title: "Date", key: "fileUploadDate", value: "uploadDate" },
  { title: "Actions", key: "fileActions", value: "fileActions" },
];

const availableVersions = computed(
  () => props.firmwareUpdateInformation?.availableFirmwareVersions ?? []
);

const selectedFirmwareVersion = ref<any | null>(null);
const showConfirmFirmwareUpdateInstallation = ref(false);

function installFirmwareUpdate() {
  if (!selectedFirmwareVersion.value?.softwareNameplateSubmodelId) {
    $toast.error("No firmware version selected");
    return;
  }
  const softwareNameplateSubmodelIdBase64Encoded = btoa(
    selectedFirmwareVersion.value.softwareNameplateSubmodelId
  );
  ResourceManagementClient.resourcesUpdatesApi
    .startFirmwareUpdateOnResource(
      props.resourceId,
      softwareNameplateSubmodelIdBase64Encoded
    )
    .then(() => {
      emit("update-started");
      $toast.info("Firmware update preparation started");
    })
    .catch((e) => {
      console.error("Error starting firmware update preparation:", e);
      $toast.error("Failed to start firmware update preparation");
    })
    .finally(() => {
      selectedFirmwareVersion.value = null;
    });
}
</script>

<template>
  <RowWithLabel
    label="Available versions"
    :divider="true"
  >
    <template #content>
      <div v-if="availableVersions?.length === 0">
        No updates available
      </div>
      <v-expansion-panels
        v-else
        variant="accordion"
        flat
      >
        <v-expansion-panel
          v-for="firmwareVersion in availableVersions"
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
                    v-if="firmwareVersion.version === firmwareUpdateInformation?.currentFirmwareVersion?.version"
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
                      :disabled="firmwareUpdateInformation.isUpdateInProgress"
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
            <RowWithLabel label="Installation URI">
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
            <RowWithLabel label="File">
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
</template>
