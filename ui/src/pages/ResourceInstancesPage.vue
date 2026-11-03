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
        <DeviceCreateDialog
          :show="showCreateDialog"
          @canceled="showCreateDialog = false"
          @confirmed="showCreateDialog = false"
        />
        <CapabilityCreateDialog
          :show="showCapabilityDialog"
          @canceled="showCapabilityDialog = false"
          @confirmed="showCapabilityDialog = false"
        />
        <AddItemChooserDialog
          :show="showChooserDialog"
          @canceled="showChooserDialog = false"
          @device="showChooserDialog = false; showCreateDialog = true"
          @capability="showChooserDialog = false; showCapabilityDialog = true"
        />
        <!-- Speed dial sits inside the fab with activator="parent": with :app the fab's root element is the
             full-height layout wrapper, so using it as activator would anchor the sub-buttons at the top of the page -->
        <v-fab
          v-if="isAdmin"
          :active="!showCreateButton"
          icon
          class="mx-4"
          elevation="15"
          color="primary"
          location="right bottom"
          :app="true"
          @click="showChooserDialog = true"
        >
          <v-icon>mdi-plus</v-icon>
          <v-speed-dial
            activator="parent"
            open-on-hover
            location="left center"
          >
            <v-fab
              key="device"
              icon
              size="small"
              color="primary"
              @click="showCreateDialog = true"
            >
              <v-icon>mdi-devices</v-icon>
              <v-tooltip
                activator="parent"
                location="top"
              >
                Add device
              </v-tooltip>
            </v-fab>
            <v-fab
              key="capability"
              icon
              size="small"
              color="primary"
              @click="showCapabilityDialog = true"
            >
              <v-icon>mdi-puzzle</v-icon>
              <v-tooltip
                activator="parent"
                location="top"
              >
                Add capability
              </v-tooltip>
            </v-fab>
          </v-speed-dial>
        </v-fab>
        <v-fab
          v-else
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
import DeviceCreateDialog from "@/components/resources/dialogs/DeviceCreateDialog.vue";
import DeviceInfoView from "@/components/resources/deviceinfo/DeviceInfoView.vue";
import OverviewHeading from "@/components/base/OverviewHeading.vue";
import ResourcesTableDevices from "@/components/resources/ResourcesTableDevices.vue";
import {useCapabilitiesStore} from "@/stores/capabilitiesStore";
import {useUserStore} from "@/stores/userStore";
import AddItemChooserDialog from "@/components/resources/dialogs/AddItemChooserDialog.vue";
import CapabilityCreateDialog from "@/components/capabilities/CapabilityCreateDialog.vue";

const resourceDevicesStore = useResourceDevicesStore();
const capabilitiesStore = useCapabilitiesStore();
const userStore = useUserStore();

const isAdmin = computed(() => userStore.userRoles.includes('slm-admin'));

const selectedResource = ref(null);
const selectedSection = ref("common");
const showCreateDialog = ref(false);
const showCreateButton = ref(false);
const showChooserDialog = ref(false);
const showCapabilityDialog = ref(false);

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