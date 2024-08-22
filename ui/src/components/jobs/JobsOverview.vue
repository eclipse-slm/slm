<template>
  <v-container fluid>
    <div>
      <base-material-card>
        <template #heading>
          <overview-heading text="Jobs" />
        </template>

        <no-item-available-note
          v-if="!jobs.length"
          item="jobs"
        />

        <v-data-table
          v-else
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

          <template #item.status="{ item }">
            <span v-if="item.status === 'running'">
              {{ getFormattedTime(jobs_running.find(j => j.id === item.id).elapsed) }}
            </span>
            <span v-else-if="item.elapsed > 60">
              {{ Math.floor(item.elapsed/60) }}m {{ Math.round(item.elapsed % 60) }}s
            </span>
            <span v-else>
              {{ Math.round(item.elapsed % 60) }}s
            </span>
            {{ item.status }}
          </template>
        </v-data-table>
      </base-material-card>
    </div>
  </v-container>
</template>

<script>

import OverviewHeading from "@/components/base/OverviewHeading.vue";
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import getEnv from '@/utils/env'
import {useJobsStore} from "@/stores/jobsStore";

export default {
    name: 'JobsOverview',
    components: {
      OverviewHeading,
      NoItemAvailableNote
    },
    setup(){
      const jobsStore = useJobsStore();
      return {jobsStore}
    },
    data: function () {
      return {
        observer: null,
        sortBy: [{key: 'id', order: 'asc'}],
        sortDesc: true,
      }
    },
    computed: {
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
        return getEnv('VUE_APP_AWX_URL')
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
      getFormattedTime (time) {
        // const milliseconds = parseInt((time % 1000))
        const seconds = parseInt((time / 1000) % 60)
        const minutes = parseInt((time / (1000 * 60)) % 60)
        // const hours = parseInt((time / (1000 * 60 * 60)) % 24)

        return (minutes > 0) ? `${minutes}m ${seconds}s` : `${seconds}s`
      },
    },
  }
</script>
