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
          :value="resources.length.toString()"
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

      <!--      <v-col
        cols="12"
        sm="6"
        lg="3"
      >
        <base-material-stats-card
          color="green"
          icon="mdi-account-hard-hat"
          title="Jobs"
          :value="jobs.length.toString()"
          @click.native="onResourcesCardClicked"
        />
      </v-col>-->

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
          icon="mdi-apps"
          title="Services"
          :value="services.length.toString()"
          @click.native="onServicesCardClicked"
        />
      </v-col>
    </v-row>
    <v-row>
      <v-col
        cols="12"
        lg="8"
      >
        <base-material-card>
          <template #heading>
            <v-container
              fluid
              class="ma-0 pa-0"
            >
              <v-row class="bg-info text-h3 font-weight-light">
                <v-col>
                  <v-icon
                    size="large"
                    class="ml-2 mr-4"
                    style="font-size: 36px;"
                    color="white"
                  >
                    mdi-run-fast
                  </v-icon>
                  <span style="color: white">
                    Latest {{ jobCount }} Jobs
                  </span>
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
              :hide-default-footer="true"
              :footer-props="{'items-per-page-options':[jobCount]}"
              :headers="DataTableHeaders"
              item-key="id"
              :items="jobs"
              style="border-bottom:1px solid #E0E0E0"
            >
              <template #header.id="{ column }">
                <v-icon size="small">
                  mdi-pound
                </v-icon> {{ column.title }}
              </template>
              <template #header.name="{ column }">
                <v-icon size="small">
                  mdi-form-textbox
                </v-icon>
                {{ column.title }}
              </template>
              <template #header.started="{ column }">
                <v-icon size="small">
                  mdi-clock-start
                </v-icon> {{ column.title }}
              </template>
              <template #header.finished="{ column }">
                <v-icon size="small">
                  mdi-clock-end
                </v-icon> {{ column.title }}
              </template>
              <template #header.elapsed="{ column }">
                <v-icon size="small">
                  mdi-alarm
                </v-icon> {{ column.title }}
              </template>
              <template #header.status="{ column }">
                <v-icon size="small">
                  mdi-list-status
                </v-icon> {{ column.title }}
              </template>

              <template #item.id="{item}">
                {{ item.id }}
              </template>

              <template #item.name="{item}">
                {{ item.name }}
              </template>

              <template #item.started="{item}">
                {{ getFormattedDate(item.started) }}
              </template>

              <template #item.finished="{item}">
                {{ getFormattedDate(item.finished) }}
              </template>

              <template #item.status="{item}">
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
                      v-bind="props"
                    >
                      mdi-run-fast
                    </v-icon>
                    <v-icon
                      v-else-if="item.status === 'pending'"
                      v-bind="props"
                    >
                      mdi-timer-sand-empty
                    </v-icon>
                    <v-icon
                      v-else-if="item.status === 'canceled' || item.status === 'failed'|| item.status === 'error'"
                      v-bind="props"
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
            <v-card-subtitle>Total Job Count: {{ jobs.length }}</v-card-subtitle>
          </v-card-text>
        </base-material-card>
      </v-col>
      <v-col
        cols="12"
        lg="4"
      >
        <dashboard-resource-statistics />
      </v-col>
    </v-row>
  </v-container>
</template>

<script>

import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import DashboardResourceStatistics from "@/components/dashboard/DashboardResourceStatistics.vue";
import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";
import {useServiceInstancesStore} from "@/stores/serviceInstancesStore";
import {useResourceClustersStore} from "@/stores/resourceClustersStore";
import {useUserStore} from "@/stores/userStore";
import {useJobsStore} from "@/stores/jobsStore";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";

export default {
    name: 'DashboardDashboard',
    components: {
      DashboardResourceStatistics,
      NoItemAvailableNote,
    },

    setup(){
      const serviceOfferingsStore = useServiceOfferingsStore();
      const serviceInstancesStore = useServiceInstancesStore();
      const resourceClustersStore = useResourceClustersStore();
      const userStore = useUserStore();
      const jobsStore = useJobsStore();
      const resourceDevicesStore = useResourceDevicesStore();
      return {serviceInstancesStore, serviceOfferingsStore, resourceClustersStore, userStore, jobsStore, resourceDevicesStore};
    },

    data () {
      return {
        sortBy: [{key: 'id'}],
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
            // lineSmooth: this.$chartist.Interpolation.cardinal({
            //   tension: 0,
            // }),
            low: 0,
            high: 1000,
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
            title: 'ID',
            value: 'id',
          },
          {
            sortable: false,
            title: 'Name',
            value: 'name',
          },
          {
            sortable: false,
            title: 'Salary',
            value: 'salary',
            align: 'right',
          },
          {
            sortable: false,
            title: 'Country',
            value: 'country',
            align: 'right',
          },
          {
            sortable: false,
            title: 'City',
            value: 'city',
            align: 'right',
          },
        ],
        tabs: 0,
      }
    },
    computed: {
      userGroups() {
        return this.userStore.userGroups
      },
      resources() {
        return this.resourceDevicesStore.resources
      },
      clusters() {
        return this.resourceClustersStore.clusters;
      },
      services() {
        return this.serviceInstancesStore.services
      },
      serviceOfferings() {
        return this.serviceOfferingsStore.serviceOfferings
      },
      jobs() {
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
    },

    mounted () {
      this.resourceDevicesStore.updateStore()
      this.resourceClustersStore.updateStore()
      this.serviceOfferingsStore.updateStore();
      this.serviceInstancesStore.updateStore();
    },

    methods: {
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
