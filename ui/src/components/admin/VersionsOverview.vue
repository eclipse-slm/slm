<template>
  <v-container
    fluid
    tag="section"
  >
    <base-material-card color="bg-secondary">
      <template #heading>
        <overview-heading text="Components" />
      </template>

      <v-data-table
        :footer-props="{
          'items-per-page-options': [5, 10, 20, -1],
          pageText: $t('vuetify.dataIterator.pageText'),
          'items-per-page-text': $t('vuetify.dataIterator.itemsPerPageText'),
          'items-per-page-all-text': $t('vuetify.dataIterator.itemsPerPageAllText')
        }"
        :headers="tableHeaders"
        item-key="id"
        :items="Object.values(components)"
      >
        <template #item.status="{item}">
          <v-icon
            v-if="item.status === 'UP'"
            color="green"
          >
            mdi-check-circle
          </v-icon>
          <v-icon
            v-else-if="item.status === undefined"
            color="yellow-darken-2"
          >
            mdi-help-circle
          </v-icon>
          <v-icon
            v-else
            color="red"
          >
            mdi-alert-circle
          </v-icon>
        </template>
      </v-data-table>
    </base-material-card>
  </v-container>
</template>

<script>
import ServiceManagementActuatorRestApi from "@/api/service-management/serviceManagementActuatorRestApi";
import ResourceManagementActuatorRestApi from "@/api/resource-management/resourceManagementActuatorRestApi";
import NotificationServiceActuatorRestApi from "@/api/notification-service/notificationServiceActuatorRestApi";
import OverviewHeading from "@/components/base/OverviewHeading.vue";
import {useEnvStore} from "@/stores/environmentStore";

export default {
  name: "VersionsOverview",
  components: { OverviewHeading },
  setup() {
    return { envStore: useEnvStore() }
  },
  data () {
    return {
      components: {
        service_management: {
          prettyName: "Service Management",
          version: undefined,
          buildTime: undefined,
          status: undefined
        },
        resource_management: {
          prettyName: "Resource Management",
          version: undefined,
          buildTime: undefined,
          status: undefined
        },
        notification_service: {
          prettyName: "Notification Service",
          version: undefined,
          buildTime: undefined,
          status: undefined
        },
        ui: {
          prettyName: "UI",
          version: undefined,
          buildTime: undefined,
          status: undefined
        }
      }
    }
  },
  computed: {
    tableHeaders () {
      return [
        { title: 'Name', value: 'prettyName', sortable: true },
        { title: 'Version', value: 'version', sortable: true },
        { title: 'Build Time', value: 'buildTime', sortable: true },
        { title: 'Status', value: 'status', sortable: true },
      ]
    },
  },
  mounted() {
    this.components.ui.version = this.envStore.appVersion

    ServiceManagementActuatorRestApi.getInfo().then((info) => {
      this.components.service_management.version = info.build.version
      this.components.service_management.buildTime = info.build.time
    })
    ServiceManagementActuatorRestApi.getHealth().then((health) => {
      this.components.service_management.status = health.status
    })

    ResourceManagementActuatorRestApi.getInfo().then((info) => {
      this.components.resource_management.version = info.build.version
      this.components.resource_management.buildTime = info.build.time
    })
    ResourceManagementActuatorRestApi.getHealth().then((health) => {
      this.components.resource_management.status = health.status
    })

    NotificationServiceActuatorRestApi.getInfo().then((info) => {
      this.components.notification_service.version = info.build.version
      this.components.notification_service.buildTime = info.build.time
    })
    NotificationServiceActuatorRestApi.getHealth().then((health) => {
      this.components.notification_service.status = health.status
    })


  }
}
</script>

<style scoped>

</style>
