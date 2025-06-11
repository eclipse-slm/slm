<template>
  <div>
    <v-data-table
      :expanded="expanded"
      :headers="ServiceOfferingsTableHeaders"
      :items="serviceOfferings"
      show-expand
    >
      <template #item="{ item, internalItem, isExpanded, toggleExpand }">
        <tr @click="expandRow(item);toggleExpand(internalItem)">
          <td>{{ item.name }}</td>
          <td>{{ item.id }}</td>
          <td>{{ serviceOfferingCategoryNameById(item.serviceCategoryId) }}</td>
          <td>
            <v-row>
              <v-tooltip
                location="bottom"
                :disabled="!item.scmBased"
              >
                <template #activator="{ props }">
                  <div v-bind="props">
                    <v-btn
                      :disabled="item.scmBased"
                      class="ma-1"
                      color="accent"
                      size="small"
                      @click="onAddServiceOfferingVersionClicked(item)"
                    >
                      <v-icon>
                        mdi-plus
                      </v-icon>
                    </v-btn>
                  </div>
                </template>
                <span>This action is only available for service offerings that are not based on a Git repository</span>
              </v-tooltip>
              <v-tooltip
                location="bottom"
                :disabled="!item.scmBased"
              >
                <template #activator="{ props }">
                  <div v-bind="props">
                    <v-btn
                      id="editServiceOfferingButton"
                      class="ma-1"
                      size="small"
                      color="info"
                      :disabled="item.scmBased"
                      @click.stop="onEditServiceOfferingClicked(item)"
                    >
                      <v-icon>
                        mdi-pencil
                      </v-icon>
                    </v-btn>
                  </div>
                </template>
                <span>This action is only available for service offerings that are not based on a Git repository</span>
              </v-tooltip>
              <v-btn
                class="ma-1"
                color="error"
                size="small"
                @click.stop="onDeleteServiceOfferingClicked(item)"
              >
                <v-icon>
                  mdi-delete
                </v-icon>
              </v-btn>
            </v-row>
          </td>
          <td>
            <v-icon v-if="item.scmBased">
              mdi-git
            </v-icon>
            <v-icon v-else>
              mdi-account
            </v-icon>
          </td>
          <td>
            <v-icon
              @click.stop="expandRow(item);toggleExpand(internalItem)"
            >
              {{ isExpanded(internalItem) ? 'mdi-close' : 'mdi-chevron-down' }}
            </v-icon>
          </td>
        </tr>
      </template>

      <template #expanded-row="{ columns, item: serviceOffering }">
        <tr>
          <td :colspan="columns.length">
            <v-col
              cols="12"
            >
              <v-data-table
                v-if="serviceOffering.versions.length > 0"
                :items="expandedServiceOfferingVersions"
                :headers="ServiceOfferingVersionsTableHeaders"
                hide-default-footer
                :sort-by="[{key: 'version', order: 'desc'}]"
              >
                <template #item="{ item: version }">
                  <tr>
                    <td>{{ version.version }}</td>
                    <td>{{ version.id }}</td>
                    <td>{{ new Date(version.created).toISOString().slice(0,10) }}</td>
                    <td>
                      <v-row>
                        <v-tooltip
                          location="bottom"
                          :disabled="!serviceOffering.scmBased"
                        >
                          <template #activator="{ props }">
                            <div v-bind="props">
                              <v-btn
                                :disabled="serviceOffering.scmBased"
                                class="ma-1"
                                color="info"
                                size="small"
                                @click="onEditServiceOfferingVersionClicked(version)"
                              >
                                <v-icon>
                                  mdi-pencil
                                </v-icon>
                              </v-btn>
                            </div>
                          </template>
                          <span>This action is only available for service offerings that are not based on a Git repository</span>
                        </v-tooltip>

                        <v-tooltip
                          location="bottom"
                          :disabled="!serviceOffering.scmBased"
                        >
                          <template #activator="{ props }">
                            <div v-bind="props">
                              <v-btn
                                :disabled="serviceOffering.scmBased"
                                class="ma-1"
                                color="error"
                                size="small"
                                @click="onDeleteServiceOfferingVersionClicked(version)"
                              >
                                <v-icon>
                                  mdi-delete
                                </v-icon>
                              </v-btn>
                            </div>
                          </template>
                          <span>This action is only available for service offerings that are not based on a Git repository</span>
                        </v-tooltip>
                      </v-row>
                    </td>
                  </tr>
                </template>
              </v-data-table>

              <v-row v-else>
                <v-col>
                  No version exists for this service offering
                </v-col>
                <v-col cols="9">
                  <v-btn
                    color="secondary"
                    @click="onAddServiceOfferingVersionClicked(expandedServiceOffering)"
                  >
                    <v-icon
                      density="compact"
                      size="small"
                      class="mr-2"
                    >
                      mdi-plus-circle
                    </v-icon>
                    Create first version
                  </v-btn>
                </v-col>
              </v-row>
            </v-col>
          </td>
        </tr>
      </template>
    </v-data-table>
  </div>
</template>

<script>

import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";
import {storeToRefs} from "pinia";
import ServiceManagementClient from "@/api/service-management/service-management-client";
import logRequestError from "@/api/restApiHelper";

export default {
  name: "ServiceOfferingTable",
  // components: {ProgressCircular},
  props: {
    serviceOfferings: {
      type: Array,
      default: () => []
    }
  },
  setup(){
    const serviceOfferingsStore = useServiceOfferingsStore();
    const {serviceVendorById, serviceOfferingCategoryNameById} = storeToRefs(serviceOfferingsStore);
    return {serviceOfferingsStore, serviceVendorById, serviceOfferingCategoryNameById};
  },
  data () {
    return {
      selectedServiceOffering: {},
      serviceOfferingVersionOfServiceOfferingId: {},
      expandedServiceOffering: undefined,
      expandedServiceOfferingVersions: undefined,
      expanded: []
    }
  },
  computed: {
    ServiceOfferingsTableHeaders () {
      return [
        { title: 'Name', value: 'name', sortable: true, width: '15%' },
        { title: 'Id', value: 'id', sortable: true, width: '35%' },
        { title: 'Category', value: 'serviceCategory', sortable: true, width: '15%' },
        { title: 'Actions', value: 'serviceOfferingActions', sortable: false, width: '25%' },
        { title: 'Source', value: 'scmBased', sortable: true, width: '5%' },
        { title: '', value: 'data-table-expand', width: '5%' },
      ]
    },
    ServiceOfferingVersionsTableHeaders () {
      return [
        { title: 'Version', value: 'version', sortable: true, width: '15%' },
        { title: 'Id', value: 'id', sortable: true, width: '35%' },
        { title: 'Created', value: 'created', sortable: true, width: '20%' },
        { title: 'Actions', value: 'serviceOfferingActions', sortable: false, width: '30%' },
      ]
    },
  },
  mounted() {
    this.emitInterface();
  },
  methods: {
    emitInterface() {
      this.$emit("interface", {
        updateExpanded: () => {
          if (this.expandedServiceOffering != undefined) {
            console.log("Updated expanded")
            ServiceManagementClient.serviceOfferingVersionsApi.getServiceOfferingVersionsOfServiceOffering(this.expandedServiceOffering.id).then(response => {
              this.serviceOfferingVersionOfServiceOfferingId[this.expandedServiceOffering.id] = response.data;
              this.expandedServiceOfferingVersions = response.data
            }).catch(logRequestError)
          }
        }
      });
    },
    onEditServiceOfferingClicked (serviceOffering) {
      this.$router.push({ path: `/services/vendors/${serviceOffering.serviceVendorId}/offerings/${serviceOffering.id}` })
    },
    onDeleteServiceOfferingClicked (serviceOffering) {
      this.$emit('service-offering-delete', serviceOffering)
    },
    onAddServiceOfferingVersionClicked(serviceOffering) {
      this.$router.push({ path: `/services/vendors/${serviceOffering.serviceVendorId}` +
            `/offerings/${serviceOffering.id}/versions/` })
    },
    onEditServiceOfferingVersionClicked (serviceOfferingVersion) {
      console.log(serviceOfferingVersion)
      this.$router.push({ path: `/services/vendors/${this.expandedServiceOffering.serviceVendorId}` +
                                `/offerings/${serviceOfferingVersion.serviceOfferingId}` +
                                `/versions/${serviceOfferingVersion.id}` })
    },
    onDeleteServiceOfferingVersionClicked (serviceOfferingVersion) {
      this.$emit('service-offering-version-delete', serviceOfferingVersion)
    },
    expandRow (serviceOffering) {
      this.expandedServiceOffering = serviceOffering
      if (this.serviceOfferingVersionOfServiceOfferingId[serviceOffering.id] == undefined) {
        this.expandedServiceOfferingVersions = undefined
        ServiceManagementClient.serviceOfferingVersionsApi.getServiceOfferingVersionsOfServiceOffering(serviceOffering.id).then(response => {
          this.serviceOfferingVersionOfServiceOfferingId[serviceOffering.id] = response.data;
          this.expandedServiceOfferingVersions = response.data
        }).catch(logRequestError);
      }
      else {
        this.expandedServiceOfferingVersions = this.serviceOfferingVersionOfServiceOfferingId[serviceOffering.id]
      }
    },
  }
}
</script>

<style scoped>

</style>
