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
        </v-row>
      </v-col>
    </v-row>

    <v-data-table
      id="resource-table-devices"
      :headers="tableHeaders"
      :items="filteredResources"
      :search="searchResources"
      :sort-by.sync="sortBy"
      :row-props="rowClass"
      item-key="id"
      :items-per-page="25"
      :loading="apiState === ApiState.LOADING || apiState === ApiState.UPDATING"
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

      <template #item.vendor="{ item }">
        <div>
          {{ DeviceUtils.getVendor(item.id) }}
        </div>
      </template>

      <template #item.capabilityServices="{ item }">
        <v-tooltip
          v-for="capabilityService in getDeploymentCapabilityServices(item.capabilityServices)"
          :key="capabilityService.capability.name"
          location="top"
        >
          <template #activator="{ props }">
            <CapabilityIcon
              v-bind="props"
              :capability-service="capabilityService"
            />
          </template>
          <span>Status: {{ capabilityService.status }}</span>
        </v-tooltip>
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
          v-if="!item.markedForDelete"
          class="ma-2"
        >
          <CapabilitiesButton :resource="item" />
          <v-btn
            :disabled="item.clusterMember"
            color="error"
            class="mx-2"
            size="small"
            @click.stop="resourceToDelete = item"
          >
            <v-icon icon="mdi-delete" />
          </v-btn>
        </v-row>
      </template>
    </v-data-table>
    <confirm-dialog
      :show="resourceToDelete != null"
      :title="'Delete resource ' + (resourceToDelete == null ? '' : resourceToDelete.hostname)"
      text="Do you really want to delete this resource?"
      :attention="true"
      @confirmed="deleteResource(resourceToDelete)"
      @canceled="resourceToDelete = null"
    />
  </v-container>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import ConfirmDialog from '@/components/base/ConfirmDialog';
import { useResourceDevicesStore } from "@/stores/resourceDevicesStore";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";
import CapabilitiesButton from "@/components/resources/capabilities/CapabilitiesButton.vue";
import CapabilityIcon from "@/components/resources/capabilities/CapabilityIcon.vue";
import {storeToRefs} from "pinia";
import FirmwareUpdateVersion from "@/components/updates/FirmwareUpdateVersion.vue";
import ApiState from "@/api/apiState";
import DeviceUtils from '@/utils/deviceUtils';


const emit = defineEmits(['resource-selected']);

const resourceDevicesStore = useResourceDevicesStore();
const {resources} = storeToRefs(resourceDevicesStore);

const tableHeaders = [
  { title: "Product", key: "product", width: "20%" },
  { title: "Vendor", key: "vendor", width: "20%" },
  { title: 'Capabilities', key: 'capabilityServices', value: 'capabilityServices', width: "10%" },
  { title: 'Hostname', key: 'hostname', value: 'hostname', width: "10%" },
  { title: 'IP', key: 'ip', value: 'ip', width: "10%" },
  { title: 'Location', key: 'location.name', value: 'location.name', width: "10%"},
  { title: 'Firmware', key: 'firmware', width: "10%" },
  { title: 'Actions', value: 'actions', sortable: false, width: "10%" },
];

const groupBy = ref([]);
const sortBy = ref([{ key: 'product', order: 'asc' }]);
const resourceToDelete = ref(null);
const filterResourcesByLocations = ref([]);
const searchResources = ref(undefined);
const apiState = computed(() => resourceDevicesStore.apiState);

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

const getDeploymentCapabilityServices = (capabilityServices) => {
  return capabilityServices.filter(cs => cs.capability.capabilityClass !== "BaseConfigurationCapability");
};

const deleteResource = (resource) => {
  const resourceId = resource.id;
  ResourceManagementClient.resourcesApi.deleteResource(resourceId).then();
  resourceDevicesStore.setResourceMarkedForDelete(resource);
  resourceToDelete.value = null;
};

const setSelectedResource = (event, { item, section }) => {
  emit('resource-selected', item, section);
};

const rowClass = (resource) => {
  return {
    class: {
      'text-grey text--lighten-1 row-pointer': resource.markedForDelete,
      'row-pointer': resource.markedForDelete
    }
  };
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
  this.$toast.info('Started Profiler for all devices.');
};

</script>

<style scoped>

</style>