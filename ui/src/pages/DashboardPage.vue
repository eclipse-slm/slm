<template>
  <v-container
    id="dashboard"
    fluid
    tag="section"
  >
    <v-row>
      <v-col
        cols="12"
        sm="6"
        lg="3"
      >
        <base-material-stats-card
          color="primary"
          icon="mdi-desktop-classic"
          title="Resources"
          :value="overviewResources.length.toString()"
          @click.native="onResourcesCardClicked"
        />
      </v-col>

      <v-col
        cols="12"
        sm="6"
        lg="3"
      >
        <base-material-stats-card
          color="error"
          icon="mdi-server"
          title="Cluster"
          :value="clusters.length.toString()"
          @click.native="onResourcesCardClicked"
        />
      </v-col>

      <!--      <v-col-->
      <!--        cols="12"-->
      <!--        sm="6"-->
      <!--        lg="3"-->
      <!--      >-->
      <!--        <base-material-stats-card-->
      <!--          color="green"-->
      <!--          icon="mdi-account-hard-hat"-->
      <!--          title="Jobs"-->
      <!--          :value="jobs.length.toString()"-->
      <!--          @click.native="onResourcesCardClicked"-->
      <!--        />-->
      <!--      </v-col>-->

      <v-col
        cols="12"
        sm="6"
        lg="3"
      >
        <base-material-stats-card
          color="secondary"
          icon="mdi-offer"
          title="Service Offerings"
          :value="serviceOfferings.length.toString()"
          @click.native="onServicesCardClicked"
        />
      </v-col>

      <v-col
        cols="12"
        sm="6"
        lg="3"
      >
        <base-material-stats-card
          color="warn"
          icon="apps"
          title="Services"
          :value="services.length.toString()"
          @click.native="onServicesCardClicked"
        />
      </v-col>
    </v-row>
    <v-row>
      <v-col cols="12" lg="8">
        <base-material-card class="px-5 py-3">
          <template #heading>
            <v-container
              fluid
              class="ma-0 pa-0"
            >
              <v-row class="info text-h3 font-weight-light">
                <v-col>
                  <v-icon
                    large
                    class="ml-2 mr-4"
                  >
                    mdi-run-fast
                  </v-icon>
                  Latest {{ jobCount }} Jobs
                </v-col>
              </v-row>
            </v-container>
          </template>
          <v-spacer />

          <no-item-available-note
            v-if="!jobs.length"
            item="jobs"
          />

          <v-card-text v-else>
            <v-data-table
              id="jobsDashboardTable"
              :sort-by.sync="sortBy"
              :sort-desc.sync="sortDesc"
              :hide-default-footer="true"
              :footer-props="{'items-per-page-options':[jobCount]}"
              :headers="DataTableHeaders"
              item-key="id"
              :items="jobs"
              style="border-bottom:1px solid #E0E0E0"
            >
              <template #header.id="{ header }">
                <v-icon small>
                  mdi-pound
                </v-icon> {{ header.text }}
              </template>
              <template #header.name="{ header }">
                <v-icon small>
                  mdi-form-textbox
                </v-icon>
                {{ header.text }}
              </template>
              <template #header.started="{ header }">
                <v-icon small>
                  mdi-clock-start
                </v-icon> {{ header.text }}
              </template>
              <template #header.finished="{ header }">
                <v-icon small>
                  mdi-clock-end
                </v-icon> {{ header.text }}
              </template>
              <template #header.elapsed="{ header }">
                <v-icon small>
                  mdi-alarm
                </v-icon> {{ header.text }}
              </template>
              <template #header.status="{ header }">
                <v-icon small>
                  mdi-list-status
                </v-icon> {{ header.text }}
              </template>
              <template
                #body="{ items }"
              >
                <tbody
                  v-for="job in items"
                  :key="job.id"
                >
                  <tr>
                    <td>{{ job.id }}</td>
                    <td>{{ job.name }}</td>
                    <td>{{ getFormattedDate(job.started) }}</td>
                    <td>{{ getFormattedDate(job.finished) }}</td>
                    <td v-if="job.status === 'running'" />
                    <td v-else-if="job.elapsed > 60">
                      {{ Math.floor(job.elapsed/60) }}m {{ Math.round(job.elapsed % 60) }}s
                    </td>
                    <td v-else>
                      {{ Math.round(job.elapsed % 60) }}s
                    </td>
                    <td>
                      <v-tooltip right>
                        <template #activator="{ on, attrs }">
                          <v-icon
                            v-if="job.status == 'successful'"
                            v-bind="attrs"
                            v-on="on"
                          >
                            mdi-check-circle-outline
                          </v-icon>
                          <v-icon
                            v-if="job.status == 'running'"
                            v-bind="attrs"
                            v-on="on"
                          >
                            mdi-run-fast
                          </v-icon>
                          <v-icon
                            v-else-if="job.status == 'pending'"
                            v-bind="attrs"
                            v-on="on"
                          >
                            mdi-timer-sand-empty
                          </v-icon>
                          <v-icon
                            v-else-if="job.status == 'canceled' || job.status == 'failed'|| job.status == 'error'"
                            v-bind="attrs"
                            v-on="on"
                          >
                            mdi-alert-circle-outline
                          </v-icon>
                        </template>
                        <span>
                          {{ job.status }}
                        </span>
                      </v-tooltip>
                    </td>
                  </tr>
                </tbody>
              </template>
            </v-data-table>
            <v-card-subtitle>Total Job Count: {{ jobs.length }}</v-card-subtitle>
          </v-card-text>
        </base-material-card>
      </v-col>
      <v-col cols="12" lg="4">
        <dashboard-resource-statistics></dashboard-resource-statistics>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
  import { mapGetters, mapActions } from 'vuex'
  import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
  import DashboardResourceStatistics from "@/components/dashboard/DashboardResourceStatistics.vue";

  export default {
    name: 'DashboardDashboard',
    components: {
      DashboardResourceStatistics,
      NoItemAvailableNote,
    },

    data () {
      return {
        sortBy: 'id',
        sortDesc: true,
        jobCount: 5,
        dataCompletedTasksChart: {
          data: {
            labels: ['12am', '3pm', '6pm', '9pm', '12pm', '3am', '6am', '9am'],
            series: [
              [230, 750, 450, 300, 280, 240, 200, 190],
            ],
          },
          options: {
            lineSmooth: this.$chartist.Interpolation.cardinal({
              tension: 0,
            }),
            low: 0,
            high: 1000, // creative tim: we recommend you to set the high sa the biggest value + something for a better look
            chartPadding: {
              top: 0,
              right: 0,
              bottom: 0,
              left: 0,
            },
          },
        },
        headers: [
          {
            sortable: false,
            text: 'ID',
            value: 'id',
          },
          {
            sortable: false,
            text: 'Name',
            value: 'name',
          },
          {
            sortable: false,
            text: 'Salary',
            value: 'salary',
            align: 'right',
          },
          {
            sortable: false,
            text: 'Country',
            value: 'country',
            align: 'right',
          },
          {
            sortable: false,
            text: 'City',
            value: 'city',
            align: 'right',
          },
        ],
        tabs: 0,
      }
    },

    computed: {
      ...mapGetters([
        'userGroups',
        'overviewResources',
        'services',
        'serviceOfferings',
        'jobs',
        'clusters'
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
    },

    mounted () {
      this.getResourcesOverview()
      this.$store.dispatch('updateServicesStore')
    },

    methods: {
      ...mapActions(['getResourcesOverview']),
      complete (index) {
        this.list[index] = !this.list[index]
      },
      onResourcesCardClicked () {
        this.$router.push({ path: '/resources' })
      },
      onServicesCardClicked () {
        this.$router.push({ path: '/services/instances' })
      },
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
        const seconds = parseInt((time / 1000) % 60)
        const minutes = parseInt((time / (1000 * 60)) % 60)
        return (minutes > 0) ? `${minutes}m ${seconds}s` : `${seconds}s`
      },
    },
  }
</script>
