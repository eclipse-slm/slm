<script setup>
import { ref } from 'vue';
import { useDiscoveryStore } from "@/stores/discoveryStore";
import {storeToRefs} from "pinia";
import ApiState from "@/api/apiState";
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import OverviewHeading from "@/components/base/OverviewHeading.vue";
import DiscoveryJobsDeleteDialog from "@/components/discovery/DiscoveryJobsDeleteDialog.vue";
import {formatDateTime} from "@/utils/dateUtils";
import DiscoveryJobDetailsDialog from "@/components/discovery/DiscoveryJobDetailsDialog.vue";

const discoveryStore = useDiscoveryStore();
const { discoveryJobs, apiState } = storeToRefs(discoveryStore);

const selectedDiscoveryJobIds = ref([]);
const sortBy = ref([{ key: 'startDate', order: 'desc' }])
const tableHeaders = [
  { title: 'Start', key: 'startDate' },
  { title: 'End', key: 'finishDate' },
  { title: 'Id', key: 'id' },
  { title: 'Driver', key: 'driverId' },
  { title: 'State', key: 'state' },
  { title: 'Discovered Resources', key: 'discoveredResources' },
];

const showDiscoveryJobsDeleteDialog = ref(false);
const showDiscoveryJobDetailsDialog = ref(false);
const clickedDiscoveryJobId = ref(undefined);

const onRowClicked = (event, { item }) => {
  clickedDiscoveryJobId.value = item.id;
  showDiscoveryJobDetailsDialog.value = true;
};

const colorRowItem = (row) => {
  if (selectedDiscoveryJobIds.value.includes(row.item.id)) {
    return { class: 'v-data-table__selected' };
  }
};
</script>

<template>
  <div>
    <v-container fluid>
      <base-material-card>
        <template #heading>
          <overview-heading text="Discovery Jobs" />
        </template>

        <no-item-available-note
          v-if="!discoveryJobs.length"
          item="discovery jobs"
        />

        <v-card-text v-else>
          <v-row
              dense
              class="ml-8 mb-8"
              align="center"
          >
            <v-col cols="11">
            </v-col>
            <v-col cols="1">
            <v-btn
                :disabled="selectedDiscoveryJobIds.length === 0"
                class="mx-4"
                color="secondary"
                @click="showDiscoveryJobsDeleteDialog = true"
            >
              <v-icon
                  icon="mdi-delete"
                  color="white"
              />
              <DiscoveryJobsDeleteDialog
                  :show="showDiscoveryJobsDeleteDialog"
                  :discovery-job-ids="selectedDiscoveryJobIds"
                  @completed="showDiscoveryJobsDeleteDialog = false; selectedDiscoveryJobIds = [];"
                  @canceled="showDiscoveryJobsDeleteDialog = false"
              ></DiscoveryJobsDeleteDialog>
            </v-btn>
            </v-col>
          </v-row>
          <v-row>
            <v-data-table
              id="table-discovery-jobs"
              :model-value="selectedDiscoveryJobIds"
              v-model="selectedDiscoveryJobIds"
              :headers="tableHeaders"
              :items="discoveryJobs"
              show-select
              :row-props="colorRowItem"
              :loading="apiState === ApiState.INIT || apiState === ApiState.LOADING || apiState === ApiState.UPDATING"
              :sort-by="sortBy"
              @click:row="onRowClicked"
            >
              <template #item.startDate="{ item }">
                <div>
                  {{ formatDateTime(item.startDate) }}
                </div>
              </template>
              <template #item.finishDate="{ item }">
                <div>
                  {{ formatDateTime(item.finishDate) }}
                </div>
              </template>
              <template #item.discoveredResources="{ item }">
                <v-icon v-if="item.state === 'CREATED'">
                  mdi-progress-clock
                  </v-icon>
                <div v-else>
                  {{ item.discoveryResult.length }}
                </div>
              </template>
            </v-data-table>
            <DiscoveryJobDetailsDialog
                :show="showDiscoveryJobDetailsDialog"
                :discovery-job-id="clickedDiscoveryJobId"
                @canceled="showDiscoveryJobDetailsDialog = false; clickedDiscoveryJobId = undefined"
            ></DiscoveryJobDetailsDialog>
          </v-row>
        </v-card-text>
      </base-material-card>
    </v-container>
  </div>
</template>

<style>
tr.v-data-table__selected {
  background: rgb(var(--v-theme-secondary), 0.05) !important;
}
</style>