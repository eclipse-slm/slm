<template>
  <v-card>
    <v-btn-toggle
      v-model="groupByServiceInstanceGroups"
      class="px-4 py-4"
      mandatory
      @change="onGroupByServiceInstanceGroupsClicked"
    >
      <v-btn
        small
        :color="groupByServiceInstanceGroups == 0 ? 'secondary' : 'disabled'"
      >
        <v-icon color="white">
          mdi-ungroup
        </v-icon>
      </v-btn>
      <v-btn
        small
        :color="groupByServiceInstanceGroups == 1 ? 'secondary' : 'disabled'"
      >
        <v-icon color="white">
          mdi-group
        </v-icon>
      </v-btn>
    </v-btn-toggle>

    <v-data-table
      :headers="headers"
      :items="groupedServices"
      item-key="rowId"
      :item-class="rowClass"
      :group-by="groupByServiceInstanceGroups ? 'groupName' : null"
      @click:row="onServiceInstanceClicked"
    >
      <template #group.header="{items, isOpen, toggle}">
        <th colspan="7">
          <v-icon @click="toggle">
            {{ isOpen ? 'mdi-minus' : 'mdi-plus' }}
          </v-icon>
          {{ items[0].groupName }}
        </th>
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
        <v-row>
          <v-btn
            :disabled="serviceInstance.markedForDelete"
            class="ml-4"
            color="error"
            @click.stop="onDeleteClicked(serviceInstance)"
          >
            <v-icon>mdi-delete</v-icon>
          </v-btn>

          <v-menu>
            <template #activator="{ on: onMenu, attrs: attrsMenu }">
              <v-tooltip top>
                <template #activator="{ on }">
                  <div v-on="!availableVersionChangesOfServices[serviceInstance.id] ? on : ''">
                    <v-btn
                      :disabled="!availableVersionChangesOfServices[serviceInstance.id]"
                      color="blue"
                      class="ml-4"
                      v-bind="attrsMenu"
                      v-on="onMenu"
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
              <v-subheader>Available versions</v-subheader>
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

    <resources-info-dialog
      :resource="selectedResource"
      @closed="selectedResource = null"
    />

    <confirm-dialog
      v-if="serviceVersionChange.dialog"
      :show="serviceVersionChange.dialog"
      :title="`Change version of service instance '${serviceVersionChange.serviceInstance.id}'?`"
      :text="`Do you really want to change to version '${serviceVersionChange.targetServiceVersion.version}'?`"
      @canceled="serviceVersionChange.dialog = false"
      @confirmed="onServiceInstanceVersionChangedConfirmed"
    />
  </v-card>
</template>

<script>
  import {
    mapGetters,
  } from 'vuex'
  import ServiceInstanceDeleteDialog from '@/components/services/ServiceInstanceDeleteDialog'
  import ServiceInstancesRestApi from '@/api/service-management/serviceInstancesRestApi'
  import ResourcesInfoDialog from '@/components/resources/dialogs/ResourcesInfoDialog'
  import Vue from "vue";
  import ConfirmDialog from "@/components/base/ConfirmDialog";
  import {serviceInstanceMixin} from "@/components/services/serviceInstanceMixin";

  export default {
    name: 'ServiceInstancesTable',
    comments: {
      ServiceInstanceDeleteDialog,
    },
    components: { ServiceInstanceDeleteDialog, ResourcesInfoDialog, ConfirmDialog },
    mixins: [ serviceInstanceMixin ],
    data () {
      return {
        headers: [
          { text: 'Id', value: 'id', sortable: true },
          { text: 'Service Offering', value: 'offering', sortable: true },
          { text: 'Ports', value: 'ports', sortable: true, width: '20%' },
          { text: 'Tags', value: 'tags', sortable: true },
          { text: 'Resource', value: 'resource', sortable: true },
          { text: 'Actions', value: 'actions' },
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
        groupByServiceInstanceGroups: 0
      }
    },
    computed: {
      ...mapGetters([
        'services',
        'serviceOfferingById',
        'resourceById',
        'serviceInstanceGroupById'
      ]),
    },
    created() {
      this.groupedServices = this.services
      this.services.forEach(service => {
        ServiceInstancesRestApi.getAvailableVersionsForServiceInstance(service.id).then(availableUpdates => {
          if (availableUpdates.length > 0) {
            Vue.set(this.availableVersionChangesOfServices, service.id, availableUpdates)
          }
        })
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
        ServiceInstancesRestApi.deleteServiceInstance(this.serviceToDelete.id)
        this.$store.commit('SET_SERVICE_MARKED_FOR_DELETE', this.serviceToDelete)
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
        ServiceInstancesRestApi.changeServiceInstanceVersion(
            this.serviceVersionChange.serviceInstance.id,
            this.serviceVersionChange.targetServiceVersion.serviceOfferingVersionId)
      },

      onServiceInstanceClicked (serviceInstance) {
        this.$emit('service-instance-clicked', serviceInstance)
      },

      onGroupByServiceInstanceGroupsClicked () {
        if (this.groupByServiceInstanceGroups === 1) {
          this.groupedServices = []
          this.services.forEach(service => {
            if (service.groupIds.length == 0) {
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
        return item.markedForDelete ? 'grey--text text--lighten-1 row-pointer' : 'row-pointer'
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
