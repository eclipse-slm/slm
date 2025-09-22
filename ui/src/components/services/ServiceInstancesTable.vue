<template>
  <v-container fluid>
    <v-row>
      <v-text-field
        v-model="searchServices"
        label="Search services"
        append-inner-icon="mdi-magnify"
        clearable
        variant="underlined"
      />
      <v-spacer />
      <v-spacer />
      <v-btn-toggle
        v-model="groupByServiceInstanceGroups"
        class="px-2 py-2"
        mandatory
        base-color="disable"
        @update:modelValue="onGroupByServiceInstanceGroupsClicked"
      >
        <v-btn
          :color="groupByServiceInstanceGroups === 0 ? 'secondary' : 'disable'"
        >
          <v-icon color="white">
            mdi-ungroup
          </v-icon>
        </v-btn>
        <v-btn
          :color="groupByServiceInstanceGroups === 1 ? 'secondary' : 'disable'"
        >
          <v-icon color="white">
            mdi-group
          </v-icon>
        </v-btn>
      </v-btn-toggle>
    </v-row>

    <v-data-table
      id="table-service-instances"
      :headers="headers"
      :items="groupedServices"
      item-key="rowId"
      :search="searchServices"
      :row-props="rowClass"
      :group-by="groupByServiceInstanceGroups ? [{key: 'groupName'}] : []"
      @click:row="onServiceInstanceClicked"
    >
      <template #group-header="{ item, columns, toggleGroup, isGroupOpen }">
        <tr>
          <td :colspan="columns.length">
            <v-btn
              :icon="isGroupOpen(item) ? '$expand' : '$next'"
              class="ma-2"
              variant="text"
              @click="toggleGroup(item)"
            />
            {{ item.value }}
          </td>
        </tr>
      </template>

      <template #item.offering="{ item : serviceInstance }">
        <div
          cols="10"
          @click.stop="onServiceOfferingTextClicked(serviceInstance.serviceOfferingId)"
        >
          {{ getServiceOfferingText(serviceInstance) }}
        </div>
      </template>

      <template #item.ports="{ item }">
        <v-row>
          <v-col>
            <span>
              <a
                v-for="portLink in getPortLinks(item)"
                :key="portLink.text"
                :href="portLink.link"
                target="_blank"
                rel="noreferrer noopener"
                @click.stop=""
              >
                {{ portLink.text }}
              </a>
            </span>
          </v-col>
        </v-row>
      </template>

      <!-- Column: Tags -->
      <template #item.tags="{ item }">
        <v-chip
          v-for="tag in item.tags"
          :key="tag"
        >
          {{ tag }}
        </v-chip>
      </template>

      <template #item.resource="{ item : serviceInstance }">
        <span
          v-if="resourceById(serviceInstance.resourceId) !== undefined"
          @click.stop="onResourceTextClicked(serviceInstance.resourceId)"
        >
          {{ resourceById(serviceInstance.resourceId).hostname }}
        </span>
        <div v-else>
          -
        </div>
      </template>

      <!-- Column: Actions -->
      <template
        #item.actions="{ item : serviceInstance }"
      >
        <v-row align="center">
          <v-btn
            :disabled="serviceInstance.markedForDelete"
            class="ml-4"
            color="error"
            size="small"
            @click.stop="onDeleteClicked(serviceInstance)"
          >
            <v-icon>mdi-delete</v-icon>
          </v-btn>

          <v-menu>
            <template #activator="{ propsM }">
              <v-tooltip location="top">
                <template #activator="{ props }">
                  <div v-bind="!availableVersionChangesOfServices[serviceInstance.id] ? props : ''">
                    <v-btn
                      :disabled="!availableVersionChangesOfServices[serviceInstance.id]"
                      color="blue"
                      class="ml-4"
                      size="small"
                      v-bind="propsM"
                    >
                      <v-icon>
                        mdi-upload
                      </v-icon>
                    </v-btn>
                  </div>
                </template>
                <span>No version changes available</span>
              </v-tooltip>
            </template>

            <v-list>
              <v-list-subheader>Available versions</v-list-subheader>
              <v-list-item
                v-for="(availableVersionChange, i) in availableVersionChangesOfServices[serviceInstance.id]"
                :key="i"
              >
                <v-list-item-title>
                  <v-row>
                    <v-col cols="7">
                      {{ availableVersionChange.version }}
                    </v-col>
                    <v-col cols="1">
                      <v-icon
                        :color="availableVersionChange.changeType == 'UP' ? 'green' : 'orange'"
                        @click.stop="onServiceInstanceVersionChangedClicked(serviceInstance, availableVersionChange)"
                      >
                        {{ availableVersionChange.changeType == 'UP' ? 'mdi-upload' : 'mdi-download' }}
                      </v-icon>
                    </v-col>
                  </v-row>
                </v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>
        </v-row>
      </template>
    </v-data-table>

    <service-instance-delete-dialog
      :service="serviceToDelete"
      :show="serviceToDelete != null"
      @canceled="onServiceDeleteCanceled"
      @confirmed="onServiceDeleteConfirmed"
    />

    <DeviceInfoView
      :resource="selectedResource"
      @closed="selectedResource = null"
    />

    <confirm-dialog
      v-if="serviceVersionChange.dialog"
      :show="serviceVersionChange.dialog"
      :title="`Change version of service instance '${serviceVersionChange.serviceInstance.id}'?`"
      :text="`Do you want to change to version '${serviceVersionChange.targetServiceVersion.version}'?`"
      @canceled="serviceVersionChange.dialog = false"
      @confirmed="onServiceInstanceVersionChangedConfirmed"
    />
  </v-container>
</template>

<script>
import ServiceInstanceDeleteDialog from '@/components/services/ServiceInstanceDeleteDialog.vue'
import DeviceInfoView from '@/components/resources/deviceinfo/DeviceInfoView.vue'
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";
import {serviceInstanceMixin} from "@/components/services/serviceInstanceMixin";
import {useServiceInstancesStore} from "@/stores/serviceInstancesStore";
import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import {storeToRefs} from "pinia";
import ServiceManagementClient from "@/api/service-management/service-management-client";
import logRequestError from "@/api/restApiHelper";

export default {
    name: 'ServiceInstancesTable',
    comments: {
      ServiceInstanceDeleteDialog,
    },
    components: { ServiceInstanceDeleteDialog, DeviceInfoView, ConfirmDialog },
    mixins: [ serviceInstanceMixin ],
    setup(){
      const serviceInstancesStore = useServiceInstancesStore();
      const serviceOfferingsStore = useServiceOfferingsStore();
      const resourceDevicesStore = useResourceDevicesStore();
      const {serviceInstanceGroupById} = storeToRefs(serviceInstancesStore)
      const {serviceOfferingById} = storeToRefs(serviceOfferingsStore)
      const {resourceById} = storeToRefs(resourceDevicesStore)
      return {serviceInstancesStore, resourceDevicesStore, serviceOfferingById, serviceInstanceGroupById, resourceById};
    },
    data () {
      return {
        headers: [
          { title: 'Id', key: 'id', sortable: true },
          { title: 'Service Offering', key: 'offering', sortable: true },
          { title: 'Ports', key: 'ports', sortable: true, width: '20%' },
          { title: 'Tags', key: 'tags', sortable: true },
          { title: 'Resource', key: 'resource', sortable: true },
          { title: 'Actions', key: 'actions' },
        ],
        serviceToDelete: null,
        selectedResource: null,
        resourceTextHover: false,
        availableVersionChangesOfServices: {},
        serviceVersionChangeDialog: false,
        serviceVersionChange: {
          dialog: false,
          serviceInstance: null,
          targetServiceVersion: null
        },
        groupedServices: [],
        groupByServiceInstanceGroups: 0,
        searchServices: undefined
      }
    },
    computed: {
      services () {
        return this.serviceInstancesStore.services
      },

    },
    created() {

      this.groupedServices = this.services
      this.services.forEach(service => {
        ServiceManagementClient.serviceInstancesApi.getAvailableVersionChangesForServiceInstance(service.id).then(response => {
          if (response.data && response.data.length > 0) {
            this.availableVersionChangesOfServices[service.id] = response.data;
          }
        }).catch(logRequestError)
      })
    },
    methods: {
      onDeleteClicked (clickedService) {
        this.serviceToDelete = clickedService
      },
      onServiceDeleteCanceled () {
        this.serviceToDelete = null
      },
      onServiceDeleteConfirmed () {
        ServiceManagementClient.serviceInstancesApi.deleteServiceInstance(this.serviceToDelete.id).then().catch(logRequestError)
        this.serviceInstancesStore.setServiceMarkedForDelete(this.serviceToDelete);
        this.serviceToDelete = null
        this.$toast.info('Service deletion started')
      },

      onServiceOfferingTextClicked (serviceOfferingId) {
        this.$router.push({ path: `/services/offerings/${serviceOfferingId}` })
      },

      onResourceTextClicked (resourceId) {
        this.selectedResource = this.resourceById(resourceId)
      },

      onServiceInstanceVersionChangedClicked(serviceInstance, availableUpdate) {
        this.serviceVersionChange.dialog = true
        this.serviceVersionChange.serviceInstance = serviceInstance
        this.serviceVersionChange.targetServiceVersion = availableUpdate
      },

      onServiceInstanceVersionChangedConfirmed() {
        ServiceManagementClient.serviceInstancesApi.updateServiceInstanceToVersion(
            this.serviceVersionChange.serviceInstance.id,
            this.serviceVersionChange.targetServiceVersion.serviceOfferingVersionId
        ).then().catch(logRequestError)
      },

      onServiceInstanceClicked (event, serviceInstance) {
        this.$emit('service-instance-clicked', serviceInstance.item)
      },

      onGroupByServiceInstanceGroupsClicked () {
        if (this.groupByServiceInstanceGroups === 1) {
          this.groupedServices = []
          this.services.forEach(service => {
            if (service.groupIds.length === 0) {
              let serviceWithGroup = JSON.parse(JSON.stringify(service))
              serviceWithGroup.groupName = 'No group'
              serviceWithGroup.rowId = service.id + "_ungrouped"
              this.groupedServices.push(serviceWithGroup)
            } else {
              service.groupIds.forEach(groupId => {
                let serviceWithGroup = JSON.parse(JSON.stringify(service))
                serviceWithGroup.groupName = this.serviceInstanceGroupById(groupId).name
                serviceWithGroup.rowId = service.id + "_" + serviceWithGroup.groupName
                this.groupedServices.push(serviceWithGroup)
              })
            }
          })
        }
        else {
          this.groupedServices = this.services
        }
      },

      rowClass (item) {
        return {
          class: {
            'text-grey text--lighten-1 row-pointer': item.markedForDelete,
            'row-pointer': item.markedForDelete
          }
        };
      },

    },
  }

</script>

<style scoped>
.row-pointer  {
  cursor: pointer;
}

span:hover {
  color: #00a0e3;
}

a { text-decoration: none; }
a:hover {
  color: #00a0e3;
}
</style>
