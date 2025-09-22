<template>
  <v-container
    id="resources"
    fluid
    tag="section"
  >
    <div
      v-if="apiStateLoading"
      class="text-center"
    >
      <v-progress-circular
        :size="70"
        :width="7"
        color="primary"
        indeterminate
      />
    </div>
    <div v-if="apiStateError">
      Error
    </div>

    <div v-if="apiStateLoaded">
      <div>
        <base-material-card>
          <template #heading>
            <overview-heading text="Devices" />
          </template>

          <no-item-available-note
            v-if="resources.length == 0"
            item="Resource"
          />

          <v-card-text v-else>
            <v-row>
              <ResourcesTableDevices
                v-if="resources.length > 0"
                class="mt-0 flex"
                @resourceClicked="onResourceClicked"
              />
            </v-row>
          </v-card-text>
        </base-material-card>

        <DeviceInfoView
          :resource="selectedResource"
          :section="selectedSection"
          @closed="selectedResource = null"
        />
        <resources-create-dialog
          :show="showCreateDialog"
          @canceled="showCreateDialog = false"
        />
        <v-fab
          :active="!showCreateButton"
          icon="mdi-plus"
          class="mx-4"
          elevation="15"
          color="primary"
          location="right bottom"
          :app="true"
          @click="showCreateDialog = true"
        />
      </div>
    </div>
  </v-container>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue';
import ApiState from '@/api/apiState';
import { useResourceDevicesStore } from "@/stores/resourceDevicesStore";
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import ResourcesCreateDialog from "@/components/resources/dialogs/create/ResourcesCreateDialog.vue";
import DeviceInfoView from "@/components/resources/deviceinfo/DeviceInfoView.vue";
import OverviewHeading from "@/components/base/OverviewHeading.vue";
import ResourcesTableDevices from "@/components/resources/ResourcesTableDevices.vue";
import {useCapabilitiesStore} from "@/stores/capabilitiesStore";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";

const resourceDevicesStore = useResourceDevicesStore();
const capabilitiesStore = useCapabilitiesStore();

const selectedResource = ref(null);
const selectedSection = ref("common");
const showCreateDialog = ref(false);
const showCreateButton = ref(false);

const resources = computed(() => resourceDevicesStore.resources);

const apiStateResources = computed(() => resourceDevicesStore.apiState);
const apiStateLoaded = computed(() => apiStateResources.value === ApiState.LOADED || apiStateResources.value === ApiState.UPDATING);
const apiStateLoading = computed(() => apiStateResources.value === ApiState.LOADING || apiStateResources.value === ApiState.INIT);
const apiStateError = computed(() => apiStateResources.value === ApiState.ERROR);

const onResourceClicked = (resource, section) => {
  selectedResource.value = resource;
  selectedSection.value = section;
};

onMounted(() => {
  capabilitiesStore.getCapabilities();
});
</script>

<style scoped />