<template>
  <v-container
    id="resource types"
    fluid
    tag="section"
  >
    <div
      v-if="apiState === ApiState.LOADING"
      class="text-center"
    >
      <v-progress-circular
        :size="70"
        :width="7"
        color="primary"
        indeterminate
      />
    </div>
    <div v-if="apiState === ApiState.ERROR">
      Error
    </div>

    <div v-if="apiState === ApiState.LOADED">
      <div>
        <base-material-card>
          <template #heading>
            <overview-heading text="Device Types" />
          </template>

          <no-item-available-note
            v-if="resourceTypes.length === 0"
            item="Device Types"
          />

          <v-card-text v-else>
            <ResourcesTableTypes
              :resource-types="resourceTypes"
              @resource-type-selected="onResourceTypeSelected"
              @resource-instance-selected="onResourceInstanceSelected"
            />
          </v-card-text>
        </base-material-card>

        <DeviceInfoView
          :resource="selectedResource"
          :section="selectedSection"
          @closed="selectedResource = null"
        />

        <DeviceTypeInfoView
          :resource-type="selectedResourceType"
          @closed="selectedResourceType = null"
        />
      </div>
    </div>
  </v-container>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import ApiState from '@/api/apiState';
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import OverviewHeading from "@/components/base/OverviewHeading.vue";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import ResourcesTableTypes from "@/components/resources/ResourcesTableTypes.vue";
import DeviceInfoView from "@/components/resources/deviceinfo/DeviceInfoView.vue";
import DeviceTypeInfoView from "@/components/resources/devicetypeinfo/DeviceTypeInfoView.vue";

const resourceTypes = ref([]);
const selectedResourceType = ref(null);
const selectedResource = ref(null);
const selectedSection = ref("common");
const apiState = ref(ApiState.INIT);

onMounted(() => {
  apiState.value = ApiState.LOADING;
  ResourceManagementClient.resourceTypesApi.getResourceTypes()
      .then(
          response => {
            if (response.data){
              resourceTypes.value = response.data;
              apiState.value = ApiState.LOADED;
            }
          }
      )
      .catch(e => {
        console.debug(e)
        apiState.value = ApiState.ERROR;
        resourceTypes.value = [];
      })
});

const onResourceTypeSelected = (resourceType) => {
  selectedResourceType.value = resourceType;
};

const onResourceInstanceSelected = (resource, section) => {
  selectedResource.value = resource;
  selectedSection.value = section;
};
</script>

<style scoped />