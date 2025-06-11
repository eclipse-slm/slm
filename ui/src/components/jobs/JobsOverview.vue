<template>
  <v-container fluid>
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
      <base-material-card>
        <template #heading>
          <overview-heading text="Jobs" />
        </template>

        <v-data-table
          v-if="jobs && jobs.length > 0"
          id="jobsTable"
          :sort-by.sync="sortBy"
          :footer-props="{
            'items-per-page-options': [5, 10, 20, -1],
          }"
          :headers="DataTableHeaders"
          item-key="id"
          :items="jobs"
        >
          <template #item.id="{ item }">
            <a
              :href="`${awxURL}/#/jobs/playbook/${item.id}`"
              target="_blank"
            >{{ item.id }}</a>
          </template>

          <template #item.name="{ item }">
            {{ item.name }}
          </template>

          <template #item.started="{ item }">
            {{ getFormattedDate(item.started) }}
          </template>

          <template #item.finished="{ item }">
            {{ getFormattedDate(item.finished) }}
          </template>

          <template #item.elapsed="{ item }">
            {{ getFormattedTime(item.elapsed) }}
          </template>

          <template #item.status="{ item }">
            {{ item.status }}
          </template>
        </v-data-table>

        <no-item-available-note
          v-else
          item="jobs"
        />
      </base-material-card>
    </div>
  </v-container>
</template>

<script>

import OverviewHeading from "@/components/base/OverviewHeading.vue";
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import {useJobsStore} from "@/stores/jobsStore";
import ApiState from "@/api/apiState";
import {useEnvStore} from "@/stores/environmentStore";

export default {
    name: 'JobsOverview',
    components: {
      OverviewHeading,
      NoItemAvailableNote
    },
    setup(){
      const envStore = useEnvStore();
      const jobsStore = useJobsStore();
      return {envStore, jobsStore}
    },
    data: function () {
      return {
        observer: null,
        sortBy: [{key: 'id', order: 'desc'}],
        sortDesc: true,
      }
    },
    computed: {
      apiStateJobs() {
        return this.jobsStore.apiState
      },
      apiStateLoaded () {
        return this.apiStateJobs === ApiState.LOADED
      },
      apiStateLoading () {
        if (this.apiStateJobs === ApiState.INIT) {
          this.jobsStore.updateStore();
        }
        return this.apiStateJobs === ApiState.LOADING || this.apiStateJobs === ApiState.INIT
      },
      apiStateError () {
        return this.apiStateJobs === ApiState.ERROR
      },
      jobs () {
        return this.jobsStore.jobs
      },
      jobs_running () {
        return this.jobsStore.jobs_running
      },
      DataTableHeaders () {
        return [
          { title: 'ID', value: 'id', sortable: true },
          { title: 'Name', value: 'name', sortable: true },
          { title: 'Started at', value: 'started', sortable: true },
          { title: 'Finished at', value: 'finished', sortable: true },
          { title: 'Duration', value: 'elapsed', sortable: true },
          { title: 'Status', value: 'status', sortable: false },
        ]
      },
      awxURL () {
        return this.envStore.awxUrl
      }
    },
    methods: {
      getFormattedDate (time) {
        const options = { weekday: 'short', year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' }
        const location = 'de-DE'

        if (time !== null) {
          return new Date(time).toLocaleDateString(location, options)
        } else {
          return ''
        }
      },
      getFormattedTime (duration) {
        // Hours, minutes and seconds
        const hrs = ~~(duration / 3600);
        const mins = ~~((duration % 3600) / 60);
        const secs = ~~duration % 60;

        // Output like "1:01" or "4:03:59" or "123:03:59"
        let ret = "";

        if (hrs > 0) {
          ret += "" + hrs + ":" + (mins < 10 ? "0" : "");
        }

        ret += "" + mins + ":" + (secs < 10 ? "0" : "");
        ret += "" + secs;

        return ret;

        // // const milliseconds = parseInt((time % 1000))
        // const seconds = parseInt((time / 1000) % 60)
        // const minutes = parseInt((time / (1000 * 60)) % 60)
        // // const hours = parseInt((time / (1000 * 60 * 60)) % 24)
        //
        // return (minutes > 0) ? `${minutes}m ${seconds}s` : `${seconds}s`
      },
    },
  }
</script>
