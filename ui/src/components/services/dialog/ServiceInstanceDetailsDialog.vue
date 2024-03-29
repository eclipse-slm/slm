<template>
  <v-dialog
    v-model="showDialog"
    @click:outside="onCloseButtonClicked"
  >
    <template>
      <v-card v-if="showDialog">
        <v-toolbar
          color="primary"
          dark
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

          <v-list v-if="apiStateLoaded">
            <v-list-group
              :value="true"
            >
              <template #activator>
                <v-list-item-icon>
                  <v-icon>mdi-information</v-icon>
                </v-list-item-icon>
                <v-list-item-content>
                  <v-list-item-title>
                    Common
                  </v-list-item-title>
                </v-list-item-content>
              </template>
              <v-simple-table v-slot>
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
              </v-simple-table>
            </v-list-group>
            <v-list-group>
              <template #activator>
                <v-list-item-icon>
                  <v-icon>mdi-adjust</v-icon>
                </v-list-item-icon>
                <v-list-item-content>
                  <v-list-item-title>
                    Service Options
                  </v-list-item-title>
                </v-list-item-content>
              </template>
              <v-simple-table
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
                          bottom
                        >
                          <template #activator="{ on, attrs }">
                            <div
                              v-bind="attrs"
                              v-on="on"
                            >
                              {{ serviceOption.name }}
                            </div>
                          </template>
                          <span>{{ serviceOption.description }}</span>
                        </v-tooltip>
                      </td>
                      <td>
                        <div v-if="serviceOption.valueType == 'PASSWORD'">
                          •••••••••••••
                        </div>
                        <div v-else-if="serviceOption.valueType == 'AAS_SM_TEMPLATE'">
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
              </v-simple-table>
              <no-item-available-note
                v-else
                item="Service Options"
              />
            </v-list-group>
            <v-list-group>
              <template #activator>
                <v-list-item-icon>
                  <v-icon>mdi-history</v-icon>
                </v-list-item-icon>
                <v-list-item-content>
                  <v-list-item-title>
                    Order History
                  </v-list-item-title>
                </v-list-item-content>
              </template>
              <v-simple-table fixed-header>
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
              </v-simple-table>
            </v-list-group>
          </v-list>

          <v-card-actions class="justify-end">
            <v-btn
              text
              @click="onCloseButtonClicked"
            >
              Close
            </v-btn>
          </v-card-actions>
        </v-card-text>
      </v-card>
    </template>

    <resources-info-dialog
      :resource="selectedResource"
      @closed="selectedResource = null"
    />
  </v-dialog>
</template>

<script>

  import {
    mapGetters,
  } from 'vuex'
  import {serviceInstanceMixin} from "@/components/services/serviceInstanceMixin";
  import ResourcesInfoDialog from '@/components/resources/dialogs/ResourcesInfoDialog'
  import ServiceInstancesRestApi from "@/api/service-management/serviceInstancesRestApi";
  import ApiState from "@/api/apiState";
  import ProgressCircular from "@/components/base/ProgressCircular";
  import AasRestApi from "@/api/resource-management/aasRestApi";
  import getEnv from "@/utils/env";
  import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";

  export default {
    name: 'ServiceInstanceDetailsDialog',
    components: {NoItemAvailableNote, ProgressCircular, ResourcesInfoDialog },
    mixins: [ serviceInstanceMixin ],
    props: ['serviceInstance'],
    data () {
      return {
        selectedResource: null,
        apiState: ApiState.INIT,
        serviceInstanceDetails: null,
        aasGuiUrls: {}
      }
    },
    watch: {
      serviceInstance: {
        immediate: true,
        handler (val, oldVal) {
          if (val != null) {
            ServiceInstancesRestApi.getServiceInstanceDetails(this.serviceInstance.id).then(serviceInstanceDetails => {
              this.serviceInstanceDetails = serviceInstanceDetails
              this.apiState = ApiState.LOADED

              serviceInstanceDetails.serviceOptions.forEach(serviceOption => {
                if (serviceOption.valueType == "AAS_SM_TEMPLATE") {
                  AasRestApi.getSubmoduleTemplateInstanceOfAas(serviceOption.defaultValue, serviceOption.currentValue).then(response => {
                    let aasGuiBaseUrl = getEnv("VUE_APP_BASYX_AAS_GUI_URL")
                    let smEndpoint = response[0].smEndpoint
                    let aasGuiUrl = `${aasGuiBaseUrl}/?aas=${smEndpoint.replace('aas/submodels', 'aas&path=submodels')}`
                    console.log(response)
                    this.aasGuiUrls[serviceOption.relation + '|' + serviceOption.key] = {
                      name: response[0].name,
                      url: aasGuiUrl,
                    }
                  })
                }
              })
            } )
          }
        }
      }
    },
    computed: {
      ...mapGetters([
        'serviceOfferingById',
        'resourceById',
        'serviceInstanceGroupById'
      ]),
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
    methods: {
      onCloseButtonClicked () {
        this.$emit('closed')
      },
    },
  }
</script>
