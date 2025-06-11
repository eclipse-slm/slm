<template>
  <v-dialog
    v-model="showDialog"
    @click:outside="onCloseButtonClicked"
  >
    <template #default="{}">
      <v-card v-if="showDialog">
        <v-toolbar
          color="primary"
          theme="dark"
        >
          {{ getServiceOfferingText(serviceInstance) }} | Service Instance {{ serviceInstance.id }}
        </v-toolbar>
        <v-card-text>
          <div
            v-if="apiStateLoading"
            class="fill-height ma-0"
            align="center"
            justify="center"
          >
            <progress-circular />
          </div>

          <div v-else-if="apiStateError">
            Error loading service details
          </div>

          <v-list
            v-if="apiStateLoaded"
            :opened="['Common']"
          >
            <v-list-group
              :model-value="true"
              value="Common"
            >
              <template #activator="{props}">
                <v-list-item
                  v-bind="props"
                  prepend-icon="mdi-information"
                  title="Common"
                />
              </template>
              <v-table>
                <tbody>
                  <tr>
                    <th>{{ 'Id' }}</th>
                    <td colspan="3">
                      {{ serviceInstance.id }}
                    </td>
                  </tr>
                  <tr>
                    <th>{{ 'Service Offering' }}</th>
                    <td colspan="3">
                      <a @click="$router.push({ path: `/services/offerings/${serviceInstance.serviceOfferingId}` })">
                        {{ serviceOfferingById(serviceInstance.serviceOfferingId).name }}
                      </a>
                    </td>
                  </tr>
                  <tr>
                    <th>{{ 'Version' }}</th>
                    <td colspan="3">
                      {{ serviceOfferingById(serviceInstance.serviceOfferingId).versions.find(version => version.id === serviceInstance.serviceOfferingVersionId)?.version }}
                    </td>
                  </tr>
                  <tr>
                    <th>{{ 'Tags' }}</th>
                    <td colspan="3">
                      <v-chip
                        v-for="tag in serviceInstance.tags"
                        :key="tag"
                      >
                        {{ tag }}
                      </v-chip>
                    </td>
                  </tr>
                  <tr>
                    <th>{{ 'Groups' }}</th>
                    <td colspan="3">
                      <v-chip
                        v-for="groupId in serviceInstance.groupIds"
                        :key="groupId"
                      >
                        {{ serviceInstanceGroupById(groupId).name }}
                      </v-chip>
                    </td>
                  </tr>
                  <tr>
                    <th>{{ 'Created' }}</th>
                    <td colspan="3">
                      {{ moment(String(serviceInstanceDetails.initialCreation)).format('DD.MM.YYYY - hh:mm') }}
                    </td>
                  </tr>
                  <tr>
                    <th>{{ 'Last Update' }}</th>
                    <td colspan="3">
                      {{ moment(String(serviceInstanceDetails.lastUpdate)).format('DD.MM.YYYY - hh:mm') }}
                    </td>
                  </tr>
                  <tr>
                    <th>{{ 'Resource' }}</th>
                    <td colspan="3">
                      <a
                        v-if="resourceById(serviceInstance.resourceId) !== undefined"
                        @click="selectedResource = resourceById(serviceInstance.resourceId)"
                      >
                        {{ resourceById(serviceInstance.resourceId).hostname }}
                      </a>
                      <div v-else>
                        -
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <th>{{ 'Ports' }}</th>
                    <td colspan="3">
                      <span>
                        <a
                          v-for="portLink in getPortLinks(serviceInstance)"
                          :key="portLink.text"
                          :href="portLink.link"
                          target="_blank"
                          rel="noreferrer noopener"
                        >
                          {{ portLink.text }}
                        </a>
                      </span>
                    </td>
                  </tr>
                </tbody>
              </v-table>
            </v-list-group>
            <v-list-group value="Service Options">
              <template #activator="{props}">
                <v-list-item
                  v-bind="props"
                  prepend-icon="mdi-adjust"
                  title="Service Options"
                />
              </template>
              <v-table
                v-if="serviceInstanceDetails.serviceOptions.length>0"
                fixed-header
              >
                <template #default>
                  <thead>
                    <tr>
                      <th class="text-left">
                        Name
                      </th>
                      <th class="text-left">
                        Value
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr
                      v-for="serviceOption in serviceInstanceDetails.serviceOptions"
                      :key="serviceOption.relation + '|' + serviceOption.key"
                    >
                      <td>
                        <v-tooltip
                          v-if="serviceOption.description != null"
                          location="bottom"
                        >
                          <template #activator="{ props }">
                            <div
                              v-bind="props"
                            >
                              {{ serviceOption.name }}
                            </div>
                          </template>
                          <span>{{ serviceOption.description }}</span>
                        </v-tooltip>
                      </td>
                      <td>
                        <div v-if="serviceOption.valueType === 'PASSWORD'">
                          •••••••••••••
                        </div>
                        <div v-else-if="serviceOption.valueType === 'AAS_SM_TEMPLATE'">
                          <a
                            :href="aasGuiUrls[serviceOption.relation + '|' + serviceOption.key].url"
                            target="_blank"
                          >
                            {{ aasGuiUrls[serviceOption.relation + '|' + serviceOption.key].name }}
                            <v-icon>mdi-open-in-new</v-icon>
                          </a>
                        </div>
                        <a
                          v-else-if="serviceOption.currentValue instanceof String ? serviceOption.currentValue.startsWith('http') : false"
                          :href="serviceOption.currentValue"
                          target="_blank"
                        >
                          {{ serviceOption.currentValue }}
                        </a>
                        <div v-else>
                          {{ serviceOption.currentValue }}
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </template>
              </v-table>
              <no-item-available-note
                v-else
                item="Service Options"
              />
            </v-list-group>
            <v-list-group>
              <template #activator="{ props }">
                <v-list-item
                  v-bind="props"
                  prepend-icon="mdi-history"
                  title="Order History"
                />
              </template>
              <v-table fixed-header>
                <template #default>
                  <thead>
                    <tr>
                      <th class="text-left">
                        Created
                      </th>
                      <th class="text-left">
                        Order Id
                      </th>
                      <th class="text-left">
                        Result
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr
                      v-for="order in serviceInstanceDetails.orderHistory"
                      :key="order.id"
                    >
                      <td>{{ moment(String(order.created)).format('DD.MM.YYYY - hh:mm') }}</td>
                      <td>{{ order.id }}</td>
                      <td>{{ order.serviceOrderResult }}</td>
                    </tr>
                  </tbody>
                </template>
              </v-table>
            </v-list-group>
          </v-list>

          <v-card-actions class="justify-end">
            <v-btn
              variant="text"
              @click="onCloseButtonClicked"
            >
              Close
            </v-btn>
          </v-card-actions>
        </v-card-text>
      </v-card>

      <DeviceInfoView
        :resource="selectedResource"
        @closed="selectedResource = null"
      />
    </template>
  </v-dialog>
</template>

<script>

import {serviceInstanceMixin} from "@/components/services/serviceInstanceMixin";
import DeviceInfoView from '@/components/resources/deviceinfo/DeviceInfoView.vue'
import ApiState from "@/api/apiState";
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import {useServiceInstancesStore} from "@/stores/serviceInstancesStore";
import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import {storeToRefs} from "pinia";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";
import ServiceManagementClient from "@/api/service-management/service-management-client";
import {useEnvStore} from "@/stores/environmentStore";

export default {
    name: 'ServiceInstanceDetailsDialog',
    components: {NoItemAvailableNote, ProgressCircular, DeviceInfoView },
    mixins: [ serviceInstanceMixin ],
    props: {
      serviceInstance: {
        type: Object,
        default: null
      }
    },
    setup(){
      const envStore = useEnvStore();
      const serviceInstancesStore = useServiceInstancesStore();
      const serviceOfferingsStore = useServiceOfferingsStore();
      const resourceDevicesStore = useResourceDevicesStore();
      const {serviceInstanceGroupById} = storeToRefs(serviceInstancesStore)
      const {serviceOfferingById} = storeToRefs(serviceOfferingsStore)
      const {resourceById} = storeToRefs(resourceDevicesStore)
      return {envStore, serviceInstancesStore, resourceDevicesStore, serviceOfferingById, serviceInstanceGroupById, resourceById};
    },
    data () {
      return {
        selectedResource: null,
        apiState: ApiState.INIT,
        serviceInstanceDetails: null,
        aasGuiUrls: {}
      }
    },
    computed: {
      app() {
        return app
      },
      showDialog () {
        return this.serviceInstance !== null
      },
      apiStateLoaded () {
        return (this.apiState === ApiState.LOADED || this.apiState === ApiState.UPDATING)
      },
      apiStateLoading () {
        return this.apiState === ApiState.LOADING || this.apiState === ApiState.INIT
      },
      apiStateError () {
        return this.apiState === ApiState.ERROR
      },
    },
    watch: {
      serviceInstance: {
        immediate: true,
        handler (val, oldVal) {
          if (val != null) {
            ServiceManagementClient.serviceInstancesApi.getServiceInstanceDetails(this.serviceInstance.id).then(response => {
              this.serviceInstanceDetails = response.data
              this.apiState = ApiState.LOADED

              response.data.serviceOptions.forEach(serviceOption => {
                if (serviceOption.valueType === "AAS_SM_TEMPLATE") {
                  ResourceManagementClient.submodelTemplatesRestControllerApi.getSubmodelTemplateInstancesBySemanticId(serviceOption.defaultValue, serviceOption.currentValue)
                      .then(res => {
                        let response = res.data;
                    let aasGuiBaseUrl = this.envStore.basyxAasGuiUrl
                    let smEndpoint = response[0].smEndpoint
                    let aasGuiUrl = `${aasGuiBaseUrl}/?aas=${smEndpoint.replace('aas/submodels', 'aas&path=submodels')}`
                    console.log(response)
                    this.aasGuiUrls[serviceOption.relation + '|' + serviceOption.key] = {
                      name: response[0].name,
                      url: aasGuiUrl,
                    }
                  }).catch(logRequestError)
                }
              })
            } ).catch(logRequestError)
          }
        }
      }
    },
    methods: {
      onCloseButtonClicked () {
        this.$emit('closed')
      },
    },
  }
</script>
