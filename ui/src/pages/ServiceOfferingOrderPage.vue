<template>
  <v-container fluid>
    <div v-if="apiStateLoading">
      <progress-circular />
    </div>

    <div v-if="apiStateError">
      Error
    </div>

    <v-container
      v-if="apiStateLoaded"
      class="mx-auto"
    >
      <ValidationForm
        ref="observer"
        v-slot="{ meta, handleSubmit, validate }"
      >
        <base-material-card color="secondary">
          <template #heading>
            Deployment Resource
          </template>

          <v-card-text v-if="apiState['matchingResources'] === 2">
            <progress-circular />
          </v-card-text>
          <v-card-text v-else-if="matchingResources?.length > 0">
            <v-container>
              <v-row>
                <v-col>
                  <span>Found '<strong>{{ matchingResources.length }}</strong>' suitable target resources, from '<strong>{{ totalResourcesCount }}</strong>' available resources</span>

                  <v-tooltip
                    location="bottom"
                  >
                    <template #activator="{ props }">
                      <v-icon
                        class="mx-3"
                        color="primary"
                        theme="dark"
                        v-bind="props"
                      >
                        mdi-information
                      </v-icon>
                    </template>
                    <span>{{ resources?.length }} host(s), {{ clusters?.length }} cluster(s)</span>
                  </v-tooltip>
                </v-col>
              </v-row>
              <v-row>
                <v-col>
                  <Field
                    v-slot="{ field, errors }"
                    v-model="selectedResourceId"
                    name="resource id"
                    :rules="required"
                  >
                    <v-select
                      v-bind="field"
                      v-model="selectedResourceId"
                      :items="matchingResources"
                      item-value="resourceId"
                      hint="Select resource for service deployment"
                      persistent-hint
                      required
                    >
                      <template #selection="{ item }">
                        <v-list-item-title>
                          <div v-if="item.raw.isCluster">
                            Cluster <strong>{{ clusterById(item.raw.resourceId).metaData.cluster_user }} @ {{ clusterById(item.raw.resourceId).metaData.cluster_name }}</strong>
                            {{ ` | ${clusterById(item.raw.resourceId).clusterType} ${clusterById(item.raw.resourceId).isManaged? 'managed': 'with '+clusterById(item.raw.resourceId).nodes.length+' nodes' } | ${item.raw.resourceId}` }}
                          </div>
                          <div>
                            <strong>{{ resourceById(item.raw.resourceId).hostname }}</strong>{{ ` | ${item.raw.resourceId} | ${resourceById(item.raw.resourceId).ip}` }}
                          </div>
                        </v-list-item-title>
                      </template>
                      <template #item="{ item }">
                        <v-list-item>
                          <v-list-item-title>
                            <div v-if="item.raw.isCluster">
                              Cluster <strong>{{ clusterById(item.raw.resourceId).metaData.cluster_user }} @ {{ clusterById(item.raw.resourceId).metaData.cluster_name }}</strong>
                              {{ ` | ${clusterById(item.raw.resourceId).clusterType} ${clusterById(item.raw.resourceId).isManaged? 'managed': 'with '+clusterById(item.raw.resourceId).nodes.length+' nodes' } | ${item.raw.resourceId}` }}
                            </div>
                            <div v-else>
                              <strong>{{ resourceById(item.raw.resourceId).hostname }}</strong>{{ ` | ${item.raw.resourceId} | ${resourceById(item.raw.resourceId).ip}` }}
                            </div>
                          </v-list-item-title>
                        </v-list-item>
                      </template>
                    </v-select>
                    <span>{{ errors[0] }}</span>
                  </Field>
                </v-col>
              </v-row>
            </v-container>
          </v-card-text>
          <v-card-text v-else>
            No suitable resources available for this service offering
          </v-card-text>
        </base-material-card>

        <div
          v-if="selectedResourceId"
        >
          <!--          <base-material-card
            v-for="serviceOptionCategory in serviceOfferingVersion.serviceOptionCategories"
            :key="serviceOptionCategory.id"
            color="secondary"
          >
            <template #heading>
              <div>
                {{ serviceOptionCategory.name }}
              </div>
            </template>
            <v-card-text>
              <v-container fluid>
                <v-row
                  v-for="serviceOption in serviceOptionCategory.serviceOptions"
                  :key="serviceOption.key"
                  align="center"
                  justify="center"
                >
                  <v-col cols="3">
                    {{ serviceOption.name }}
                    <v-tooltip
                      v-if="serviceOption.description != null"
                      location="bottom"
                    >
                      <template #activator="{ props }">
                        <v-icon
                          class="mx-3"
                          color="primary"
                          theme="dark"
                          v-bind="props"
                        >
                          mdi-information
                        </v-icon>
                      </template>
                      <span>{{ serviceOption.description }}</span>
                    </v-tooltip>
                  </v-col>
                  <v-col cols="9">
                    <service-option-value
                      :service-option="serviceOption"
                    />
                  </v-col>
                </v-row>
              </v-container>
            </v-card-text>
          </base-material-card>-->
        </div>

        <!-- Cancel & Checkout Buttons-->
        <v-row class="mt-12 pt-12 ">
          <v-spacer />
          <v-btn
            variant="elevated"
            class="mr-3"
            @click="cancel()"
          >
            {{ $t('buttons.Cancel') }}
          </v-btn>
          <v-spacer />
          <v-btn
            variant="elevated"
            :color="!meta.valid ? $vuetify.theme.themes.light.colors.disable : $vuetify.theme.themes.light.colors.secondary"
            @click="!meta.valid ? validate() : handleSubmit(order)"
          >
            {{ $t('buttons.Checkout') }}
          </v-btn>
          <v-spacer />
        </v-row>
      </ValidationForm>
    </v-container>

    <progress-circular
      :show-as-overlay="true"
      :overlay="showProgressCircular"
    />
  </v-container>
</template>

<script>

import ApiState from '@/api/apiState'
import ServiceOfferingVersionsRestApi from '@/api/service-management/serviceOfferingVersionsRestApi'
import logRequestError from '@/api/restApiHelper'
import ProgressCircular from "@/components/base/ProgressCircular";
import {Field, Form as ValidationForm} from "vee-validate";
import * as yup from 'yup';
import {useServicesStore} from "@/stores/servicesStore";
import {useResourcesStore} from "@/stores/resourcesStore";
import {useUserStore} from "@/stores/userStore";
import {useJobsStore} from "@/stores/jobsStore";
import {storeToRefs} from "pinia";

export default {
    name: 'ServiceOrderView',
    components: {
      Field,
       ProgressCircular, ValidationForm
    },
    props: {
      serviceOfferingId: {
        type: String,
        default: null
      },
      serviceOfferingVersionId: {
        type: String,
        default: null
      },
    },
    setup(){
      const required = yup.string().required();
      const servicesStore = useServicesStore();
      const resourcesStore = useResourcesStore();
      const userStore = useUserStore();
      const jobsStore = useJobsStore();
      const {resourceById, clusterById} = storeToRefs(resourcesStore);
      const {serviceOfferingById} = storeToRefs(servicesStore);
      return {
        required, servicesStore, resourcesStore, userStore, jobsStore,
        resourceById, clusterById, serviceOfferingById
      }
    },
    data () {
      return {
        selectedResourceId: '',
        orderButtonPressed: false,
        serviceOfferingVersion: null,
        matchingResources: [],
        apiState: {
          serviceOfferingVersion: ApiState.INIT,
          matchingResources: ApiState.INIT,
        },
        showProgressCircular: false
      }
    },

    computed: {
      apiStateServices() {
        return this.servicesStore.apiStateServices
      },
      resources() {
        return this.resourcesStore.resources
      },
      clusters () {
        return this.resourcesStore.clusters
      },

      totalResourcesCount () {
        return this.resources?.length + this.clusters?.length
      },

      apiStateLoaded () {
        return this.apiState.serviceOfferingVersion === ApiState.LOADED
            && this.apiState.matchingResources === ApiState.LOADED
      },
      apiStateLoading () {
        return this.apiState.serviceOfferingVersion === ApiState.LOADING
            && this.apiState.matchingResources === ApiState.LOADING
      },
      apiStateError () {
        return this.apiState.serviceOfferingVersion === ApiState.ERROR
            && this.apiState.matchingResources === ApiState.ERROR
      },
    },
    created () {
      ServiceOfferingVersionsRestApi.getServiceOfferingVersionById(this.serviceOfferingId, this.serviceOfferingVersionId).then(response => {
            this.serviceOfferingVersion = response;
            this.apiState.serviceOfferingVersion = ApiState.LOADED;
      })

      ServiceOfferingVersionsRestApi.getServiceOfferingVersionMatchingResources(this.serviceOfferingId, this.serviceOfferingVersionId).then((response) => {
        this.matchingResources = []
        this.apiState.matchingResources = ApiState.LOADED;

        if (response.length > 0) {
          // this.matchingResources.push({ header: "Nodes" });
          let matchingNodeResources = response.filter(matchingResource => !matchingResource.isCluster)
          this.matchingResources.push(...matchingNodeResources)

          // this.matchingResources.push({ header: "Clusters" });
          let matchingClusterResources = response.filter(matchingResource => matchingResource.isCluster)
          this.matchingResources.push(...matchingClusterResources)

          console.log('asdf', this.matchingResources)

          // preselect resource
          this.selectedResourceId = this.matchingResources.filter(obj => obj.hasOwnProperty('resourceId'))[0].resourceId;
        }
      })
    },
    methods: {
      order () {
        if (this.orderButtonPressed) {
          return
        } else {
          this.orderButtonPressed = true
        }

        const serviceOptionValues = []
        this.serviceOfferingVersion.serviceOptionCategories.forEach(function (serviceOptionCategory) {
          serviceOptionCategory.serviceOptions.forEach(function (serviceOption) {
            serviceOptionValues.push({
              serviceOptionId: (serviceOption.relation === '' ? serviceOption.key : serviceOption.relation + '|' + serviceOption.key),
              value: serviceOption.defaultValue,
            })
          })
        })

        const serviceOfferingVersionOrder = {
          serviceOptionValues: serviceOptionValues,
        }

        this.showProgressCircular = true
        ServiceOfferingVersionsRestApi.orderServiceOfferingVersion(
            this.serviceOfferingId,
            this.serviceOfferingVersionId,
            serviceOfferingVersionOrder,
            this.matchingResources.find(obj => obj.resourceId === this.selectedResourceId).capabilityServiceId
        ).then(response => {
          console.log(response)
          this.$toast.info('Service deployment started')
          this.$router.push({ path: '/services/instances' })
          this.orderButtonPressed = false
          this.showProgressCircular = false
        }).catch(error => {
          this.$toast.error('Service deployment request failed. See log for more information.')
          this.orderButtonPressed = false
          logRequestError(error)
          this.showProgressCircular = false
        })
      },
      cancel () {
        this.$router.push({ path: '/services/offerings' })
      },
    },
  }
</script>

<style scoped>

</style>
