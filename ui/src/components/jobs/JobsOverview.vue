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
          <template
            #body="{ items }"
          >
            <tbody
              v-for="job in items"
              :key="job.id"
            >
              <tr>
                <td>
                  <a
                    :href="`${awxURL}/#/jobs/playbook/${job.id}`"
                    target="_blank"
                  >{{ job.id }}</a>
                </td>
                <td>{{ job.name }}</td>
                <td>{{ getFormattedDate(job.started) }}</td>
                <td>{{ getFormattedDate(job.finished) }}</td>
                <td v-if="job.status === 'running'">
                  {{ getFormattedTime(jobs_running.find(j => j.id === job.id).elapsed) }}
                </td>
                <td v-else-if="job.elapsed > 60">
                  {{ Math.floor(job.elapsed/60) }}m {{ Math.round(job.elapsed % 60) }}s
                </td>
                <td v-else>
                  {{ Math.round(job.elapsed % 60) }}s
                </td>
                <td>{{ job.status }}</td>
              </tr>
            </tbody>
          </template>
        </v-data-table>
      </base-material-card>
    </div>
  </v-container>
</template>

<script>
  import { mapGetters } from 'vuex'
  import OverviewHeading from "@/components/base/OverviewHeading.vue";
  import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
  import getEnv from '@/utils/env'

  export default {
    name: 'JobsOverview',
    components: {
      OverviewHeading,
      NoItemAvailableNote
    },
    data: function () {
      return {
        observer: null,
        sortBy: 'id',
        sortDesc: true,
      }
    },
    computed: {
      ...mapGetters([
        'jobs',
        'jobs_running',
      ]),
      DataTableHeaders () {
        return [
          { text: 'ID', value: 'id', sortable: true },
          { text: 'Name', value: 'name', sortable: true },
          { text: 'Started at', value: 'started', sortable: true },
          { text: 'Finished at', value: 'finished', sortable: true },
          { text: 'Duration', value: 'elapsed', sortable: true },
          { text: 'Status', value: 'status', sortable: false },
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
