<template>
  <v-container fluid>
    <div>
      <base-material-card
        class="px-5 py-3"
      >
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
              {{ getFormattedTime(new Date().getTime() - new Date(item.started).getTime()) }}
            </span>
            <span v-else>
              {{ getFormattedTime(new Date(item.finished).getTime() - new Date(item.started).getTime()) }}
            </span>
            <v-tooltip location="right">
              <template #activator="{ props }">
                <v-icon
                  v-if="item.status === 'successful'"
                  v-bind="props"
                >
                  mdi-check-circle-outline
                </v-icon>
                <v-icon
                  v-if="item.status === 'running'"
                  v-bind="attrs"
                  v-on="on"
                >
                  mdi-run-fast
                </v-icon>
                <v-icon
                  v-else-if="item.status === 'pending'"
                  v-bind="attrs"
                  v-on="on"
                >
                  mdi-timer-sand-empty
                </v-icon>
                <v-icon
                  v-else-if="item.status === 'canceled' || item.status === 'failed'|| item.status === 'error'"
                  v-bind="attrs"
                  v-on="on"
                >
                  mdi-alert-circle-outline
                </v-icon>
              </template>
              <span>
                {{ item.status }}
              </span>
            </v-tooltip>
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
