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
          <base-material-card
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
                    <ServiceOptionValue
                      :service-option="serviceOption"
                    />
                  </v-col>
                </v-row>
              </v-container>
            </v-card-text>
          </base-material-card>
        </div>

        <!-- Cancel & Checkout Buttons-->
        <v-row class="mt-12 pt-12 ">
          <v-spacer />
          <v-btn
            variant="elevated"
            class="mr-3"
            @click="onCancelButtonClicked"
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

<script setup>
import { ref, computed, onMounted } from 'vue';
import {useRoute, useRouter} from 'vue-router';
import ApiState from '@/api/apiState';
import logRequestError from '@/api/restApiHelper';
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import { Field, Form as ValidationForm } from "vee-validate";
import { useResourceDevicesStore } from "@/stores/resourceDevicesStore";
import { useResourceClustersStore } from "@/stores/resourceClustersStore";
import { storeToRefs } from "pinia";
import ServiceManagementClient from "@/api/service-management/service-management-client";
import * as yup from "yup";
import ServiceOptionValue from "@/components/service_offerings/ServiceOptionValue.vue";
import {useToast} from "vue-toast-notification";

const route = useRoute();
const required = yup.string().required();
const $toast = useToast();
const resourceDevicesStore = useResourceDevicesStore();
const resourceClustersStore = useResourceClustersStore();
const { resourceById } = storeToRefs(resourceDevicesStore);
const { clusterById } = storeToRefs(resourceClustersStore);

const selectedResourceId = ref('');
const orderButtonPressed = ref(false);
const serviceOfferingVersion = ref(null);
const matchingResources = ref([]);
const showProgressCircular = ref(false);
// Computed properties
const resources = computed(() => resourceDevicesStore.resources);
const clusters = computed(() => resourceClustersStore.clusters);
const totalResourcesCount = computed(() => resources.value?.length + clusters.value?.length);
const serviceOfferingId = computed(() => route.params.serviceOfferingId);
const serviceOfferingVersionId = computed(() => route.params.serviceOfferingVersionId);
// Api State
const apiState = ref({
  serviceOfferingVersion: ApiState.INIT,
  matchingResources: ApiState.INIT,
});
const apiStateLoaded = computed(() => apiState.value.serviceOfferingVersion === ApiState.LOADED && apiState.value.matchingResources === ApiState.LOADED);
const apiStateLoading = computed(() => apiState.value.serviceOfferingVersion === ApiState.LOADING || apiState.value.matchingResources === ApiState.LOADING);
const apiStateError = computed(() => apiState.value.serviceOfferingVersion === ApiState.ERROR || apiState.value.matchingResources === ApiState.ERROR);


const router = useRouter();

const order = () => {
  if (orderButtonPressed.value) {
    return;
  } else {
    orderButtonPressed.value = true;
  }

  const serviceOptionValues = [];
  serviceOfferingVersion.value.serviceOptionCategories.forEach(function (serviceOptionCategory) {
    serviceOptionCategory.serviceOptions.forEach(function (serviceOption) {
      serviceOptionValues.push({
        serviceOptionId: (serviceOption.relation === '' ? serviceOption.key : serviceOption.relation + '|' + serviceOption.key),
        value: serviceOption.defaultValue,
      });
    });
  });

  const serviceOfferingVersionOrder = {
    serviceOptionValues: serviceOptionValues,
  };

  showProgressCircular.value = true;
  ServiceManagementClient.serviceOfferingVersionsApi.orderServiceOfferingVersionById(
    serviceOfferingId.value,
    serviceOfferingVersionId.value,
    matchingResources.value.find(obj => obj.resourceId === selectedResourceId.value).capabilityServiceId,
    serviceOfferingVersionOrder
  ).then(response => {
    console.log(response);
    $toast.info('Service deployment started');
    router.push({ path: '/services/instances' });
    orderButtonPressed.value = false;
    showProgressCircular.value = false;
  }).catch(error => {
    $toast.error('Service deployment request failed. See log for more information.');
    orderButtonPressed.value = false;
    logRequestError(error);
    showProgressCircular.value = false;
  });
};

const onCancelButtonClicked = () => {
  router.push({ path: '/services/offerings' });
};

onMounted(() => {
  ServiceManagementClient.serviceOfferingVersionsApi.getServiceOfferingVersionById(serviceOfferingId.value, serviceOfferingVersionId.value).then(response => {
    serviceOfferingVersion.value = response.data;
    apiState.value.serviceOfferingVersion = ApiState.LOADED;
  }).catch(logRequestError);

  ServiceManagementClient.serviceOfferingVersionsApi.getResourcesMatchingServiceRequirements(serviceOfferingId.value, serviceOfferingVersionId.value)
    .then((response) => {
      matchingResources.value = [];
      apiState.value.matchingResources = ApiState.LOADED;

      if (response.data.length > 0) {
        let matchingNodeResources = response.data.filter(matchingResource => !matchingResource.isCluster);
        matchingResources.value.push(...matchingNodeResources);

        let matchingClusterResources = response.data.filter(matchingResource => matchingResource.isCluster);
        matchingResources.value.push(...matchingClusterResources);

        selectedResourceId.value = matchingResources.value.filter(obj => obj.hasOwnProperty('resourceId'))[0].resourceId;
      }
    }).catch(logRequestError);
});
</script>

<style scoped>
</style>