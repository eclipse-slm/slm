<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useResourceDevicesStore } from "@/stores/resourceDevicesStore";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";
import CapabilitiesButton from "@/components/resources/capabilities/CapabilitiesButton.vue";
import CapabilityIcon from "@/components/resources/capabilities/CapabilityIcon.vue";
import {storeToRefs} from "pinia";
import FirmwareUpdateVersion from "@/components/updates/FirmwareUpdateVersion.vue";
import ApiState from "@/api/apiState";
import DeviceUtils from '@/utils/deviceUtils';
import {useCapabilitiesStore} from "@/stores/capabilitiesStore";
import {useToast} from "vue-toast-notification";
import ResourcesDeleteDialog from "@/components/resources/ResourcesDeleteDialog.vue";
import {useCapabilityUtils} from "@/utils/capabilityUtils";

const emit = defineEmits(['resourceClicked']);

const $toast = useToast();

const capabilityUtils = useCapabilityUtils()

const resourceDevicesStore = useResourceDevicesStore();
const {resources} = storeToRefs(resourceDevicesStore);
const capabilitiesStore = useCapabilitiesStore();
const { capabilityServiceById } = storeToRefs(capabilitiesStore);

const tableHeaders = [
  { title: "Product", key: "product", width: "20%" },
  { title: "Manufacturer", key: "manufacturer", width: "20%" },
  { title: 'Capabilities', key: 'deploymentCapabilityServices', value: 'deploymentCapabilityServices', width: "10%" },
  { title: 'Configurations', key: 'configurationCapabilityServices', value: 'configurationCapabilityServices', width: "10%" },
  { title: 'Hostname', key: 'hostname', value: 'hostname', width: "10%" },
  { title: 'IP', key: 'ip', value: 'ip', width: "5%" },
  { title: 'Location', key: 'location.name', value: 'location.name', width: "10%"},
  { title: 'Firmware', key: 'firmware', width: "10%" },
  { title: 'Actions', value: 'actions', sortable: false, width: "5%" },
];

const groupBy = ref([]);
const sortBy = ref([{ key: 'product', order: 'asc' }]);
const filterResourcesByLocations = ref([]);
const searchResources = ref(undefined);
const apiState = computed(() => resourceDevicesStore.apiState);

const selectedResourcesIds = ref([])

const showResourcesDeleteDialog = ref(false);

const filteredResources = computed(() => {
  if (filterResourcesByLocations.value.length === 0) {
    return resources.value
  }

  return resources.value.filter(r => {
    if (r.location == null)
      return false;

    return filterResourcesByLocations.value.includes(r.location.id);
  });
});


const locations = computed(() => resourceDevicesStore.locations);
const profiler = computed(() => resourceDevicesStore.profiler);

onMounted(() => {
  resourceDevicesStore.getResourceAasValues();
});

const setSelectedResource = (event, { item, section }) => {
  emit('resourceClicked', item, section);
};

const getLocationNameForGroupHeader = (items) => {
  if (items[0].location === null)
    return "no location";

  return items[0].location.name;
};

const resourcesHaveLocations = () => {
  const locationNames = [...new Set(
      resources.value.map(r => {
        if (r.location == null)
          return '';
        else
          return r.location.name;
      })
  )];
  const hasLocations = locationNames.length > 0;

  if (!hasLocations)
    groupBy.value = null;

  return hasLocations;
};

const runProfiler = () => {
  ResourceManagementClient.profilerApi.runProfiler1().then().catch(logRequestError);
  $toast.info('Started Profiler for all devices.');
};

const colorRowItem = (row) => {
  if (selectedResourcesIds.value.includes(row.item.id)) {
    return { class: 'v-data-table__selected' };
  }
};

</script>

<template>
  <v-container fluid>
    <v-row
      dense
      class="ml-8 mb-8"
      align="center"
    >
      <v-col cols="4">
        <v-text-field
          v-model="searchResources"
          label="Search resources"
          append-inner-icon="mdi-magnify"
          clearable
          variant="underlined"
        />
      </v-col>
      <v-col cols="3">
        <div v-if="resourcesHaveLocations()">
          <v-row class="mx-8">
            <!--            <div class="mr-10">-->
            <!--              <v-btn-toggle-->
            <!--                v-model="groupBy"-->
            <!--                mandatory-->
            <!--              >-->
            <!--                <v-btn-->
            <!--                  size="small"-->
            <!--                  :model-value="null"-->
            <!--                  :color="groupBy == null ? 'secondary' : 'disabled'"-->
            <!--                  style="height:40px"-->
            <!--                >-->
            <!--                  <v-icon>mdi-ungroup</v-icon>-->
            <!--                </v-btn>-->
            <!--                <v-btn-->
            <!--                  size="small"-->
            <!--                  model-value="location.name"-->
            <!--                  :color="groupBy === 'location.name' ? 'secondary' : 'disabled'"-->
            <!--                  style="height:40px"-->
            <!--                >-->
            <!--                  <v-icon>mdi-group</v-icon>-->
            <!--                </v-btn>-->
            <!--              </v-btn-toggle>-->
            <!--            </div>-->
            <div class="mr-10">
              <v-select
                v-model="filterResourcesByLocations"
                :items="locations"
                item-title="name"
                item-value="id"
                label="Location"
                density="compact"
                variant="outlined"
                hide-details
                closable-chips
                :width="200"
                multiple
                clearable
              />
            </div>
          </v-row>
        </div>
      </v-col>
      <v-col cols="2">
        <v-spacer />
      </v-col>
      <v-col cols="3">
        <v-row justify="end">
          <v-btn
            class="mx-4"
            color="secondary"
            @click="resourceDevicesStore.updateStore()"
          >
            <v-icon
              icon="mdi-refresh"
              color="white"
            />
          </v-btn>
          <v-tooltip
            start
            close-delay="2000"
          >
            <template #activator="{ props }">
              <v-btn
                v-if="profiler.length > 0"
                class="mx-4"
                color="secondary"
                v-bind="props"
                @click="runProfiler"
              >
                <v-icon
                  icon="mdi-tab-search"
                  color="white"
                />
              </v-btn>
            </template>
            <span>Run all available <a href="https://eclipse-slm.github.io/slm/docs/usage/profiler/">profilers</a> on all devices</span>
          </v-tooltip>
          <v-btn
              :disabled="selectedResourcesIds.length === 0"
              class="mx-4"
              color="secondary"
              @click="showResourcesDeleteDialog = true"
          >
            <v-icon
                icon="mdi-delete"
                color="white"
            />
            <ResourcesDeleteDialog
              :show="showResourcesDeleteDialog"
              :resource-ids="selectedResourcesIds"
              @completed="showResourcesDeleteDialog = false; selectedResourcesIds = [];"
              @canceled="showResourcesDeleteDialog = false"
            ></ResourcesDeleteDialog>
          </v-btn>
        </v-row>
      </v-col>
    </v-row>

    <v-data-table
      id="resource-table-devices"
      :model-value="selectedResourcesIds"
      @update:model-value="val => selectedResourcesIds = val"
      :headers="tableHeaders"
      :items="filteredResources"
      :search="searchResources"
      :sort-by.sync="sortBy"
      item-key="id"
      :items-per-page="25"
      :loading="apiState === ApiState.LOADING || apiState === ApiState.UPDATING"
      show-select
      :row-props="colorRowItem"
      @click:row="setSelectedResource"
    >
      <template #group.header="{items, isOpen, toggle}">
        <th
          :colspan="tableHeaders.length"
        >
          <v-icon @click="toggle">
            {{ isOpen ? 'mdi-minus' : 'mdi-plus' }}
          </v-icon>
          {{ getLocationNameForGroupHeader(items) }}
        </th>
      </template>

      <template #item.product="{ item }">
        <div>
          {{ DeviceUtils.getProduct(item.id) }}
        </div>
      </template>

      <template #item.manufacturer="{ item }">
        <div>
          {{ DeviceUtils.getManufacturer(item.id) }}
        </div>
      </template>

      <template #item.deploymentCapabilityServices="{ item }">
        <v-row>
          <v-tooltip
            v-for="capabilityServiceId in capabilityUtils.filterDeploymentCapabilityServices(item.capabilityServiceIds)"
            :key="capabilityServiceId"
            location="top"
          >
            <template #activator="{ props }">
              <CapabilityIcon
                  v-if="capabilityServiceById(capabilityServiceId)"
                  v-bind="props"
                  :capability-service="capabilityServiceById(capabilityServiceId)"
              />
            </template>
            <span>Status: {{ capabilityServiceById(capabilityServiceId)?.status }}</span>
          </v-tooltip>
        </v-row>
      </template>

      <template #item.configurationCapabilityServices="{ item }">
      <v-row>
        <v-tooltip
            v-for="capabilityServiceId in capabilityUtils.filterConfigurationCapabilityServices(item.capabilityServiceIds)"
            :key="capabilityServiceId"
            location="top"
        >
          <template #activator="{ props }">
            <CapabilityIcon
                v-if="capabilityServiceById(capabilityServiceId)"
                v-bind="props"
                :capability-service="capabilityServiceById(capabilityServiceId)"
            />
          </template>
          <span>Status: {{ capabilityServiceById(capabilityServiceId)?.status }}</span>
        </v-tooltip>
      </v-row>
    </template>

      <template #item.firmware="{ item }">
        <FirmwareUpdateVersion
          :resource-id="item.id"
          @click="setSelectedResource( undefined, { item, section: 'firmware' })"
        />
      </template>

      <!-- Column: Actions -->
      <template
        #item.actions="{ item }"
      >
        <v-row
          class="ma-2"
        >
          <CapabilitiesButton :resourceId="item.id" />
        </v-row>
      </template>
    </v-data-table>

  </v-container>
</template>

<style>
tr.v-data-table__selected {
  background: rgb(var(--v-theme-secondary), 0.05) !important;
}
</style>