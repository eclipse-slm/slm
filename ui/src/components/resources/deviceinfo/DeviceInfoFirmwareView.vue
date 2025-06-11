<script setup lang="ts">

import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";
import {computed, onMounted, ref} from "vue";
import {UpdateInformation} from "@/api/resource-management/client";
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import RowWithLabel from "@/components/base/RowWithLabel.vue";
import FirmwareUpdateStatusIcon from "@/components/updates/FirmwareUpdateStatusIcon.vue";
import formatDate from "@/utils/dateUtils";

const props = defineProps({
  resourceId: {
    type: String,
    required: true,
  },
});

const loading = ref(false);
const updateInformation = ref<UpdateInformation>({});
onMounted(() => {
  loading.value = true;
  ResourceManagementClient.resourcesUpdatesApi.getUpdateInformationOfResource(props.resourceId)
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

const tableHeaders = [
  { title: 'Version', key: 'version', value: 'version' },
  { title: 'Date', key: 'date', value: 'date' },
  { title: 'Installation URI', key: 'installationUri', value: 'installationUri' },
  { title: 'Installation Checksum', key: 'installationChecksum', value: 'installationChecksum' },
];

const installedVersionText = computed(() => {
  const version = updateInformation.value.currentFirmwareVersion?.version;
  if (!version) {
    return "N/A"
  }
  const date = updateInformation.value.currentFirmwareVersion?.date;
  return date ? `${version} (${formatDate(date)})` : version;
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
        :text="updateInformation.firmwareUpdateStatus"
      >
        <template #content>
          <FirmwareUpdateStatusIcon
            :firmware-update-status="updateInformation.firmwareUpdateStatus"
            :clickable="false"
          />
        </template>
      </RowWithLabel>

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
              </template>
            </RowWithLabel>
            <RowWithLabel
              label="Checksum"
              :text="firmwareVersion.installationChecksum"
            />
          </template>
        </v-expansion-panel>
      </v-expansion-panels>
    </div>
  </div>
</template>