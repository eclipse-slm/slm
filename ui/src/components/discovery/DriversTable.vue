1<template>
  <div>
    <v-container fluid>
      <base-material-card>
        <template #heading>
          <overview-heading text="Drivers" />
        </template>

        <no-item-available-note
          v-if="!drivers.length"
          item="drivers"
        />

        <v-card-text v-else>
          <v-row
              dense
              class="ml-8 mb-8"
              align="center"
          >
            <v-text-field
                v-model="searchDrivers"
                label="Search drivers"
                append-inner-icon="mdi-magnify"
                clearable
                variant="underlined"
            />
            <v-spacer />
          </v-row>
          <v-row>
            <v-data-table
              id="table-discovered-resources"
              v-model="selectedDiscoveredResourceIds"
              :headers="tableHeaders"
              :items="drivers"
              :search="searchDrivers"
              :loading="apiState === ApiState.INIT || apiState === ApiState.LOADING || apiState === ApiState.UPDATING"
            />
          </v-row>
        </v-card-text>
      </base-material-card>
    </v-container>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useDiscoveryStore } from "@/stores/discoveryStore";
import {storeToRefs} from "pinia";
import ApiState from "@/api/apiState";
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import OverviewHeading from "@/components/base/OverviewHeading.vue";

const discoveryStore = useDiscoveryStore();
const { drivers, apiState } = storeToRefs(discoveryStore);

const selectedDiscoveredResourceIds = ref([]);
const searchDrivers = ref(undefined);

const tableHeaders = [
  { title: 'Name', key: 'name', value: 'name' },
  { title: 'Vendor', key: 'vendorName', value: 'vendorName' },
  { title: 'Version', key: 'version', value: 'version' },
  { title: 'Id', key: 'instanceId', value: 'instanceId' },
  { title: 'IP address', key: 'ipv4Address', value: 'ipv4Address' },
  { title: 'Port ', key: 'portNumber', value: 'portNumber' },
];
</script>

<style>
</style>