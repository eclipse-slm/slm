<script setup>
import { ref, computed, onMounted } from 'vue';
import {useRoute, useRouter} from 'vue-router';
import ApiState from '@/api/apiState';
import logRequestError from '@/api/restApiHelper';
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import { Field, Form as ValidationForm } from "vee-validate";
import ServiceManagementClient from "@/api/service-management/service-management-client";
import * as yup from "yup";
import ServiceOptionValue from "@/components/service_offerings/ServiceOptionValue.vue";
import {useToast} from "vue-toast-notification";

const route = useRoute();
const required = yup.string().required();
const $toast = useToast();

const selectedTargetSubmodelId = ref('');
const orderButtonPressed = ref(false);
const serviceOfferingVersion = ref(null);
const deploymentTargets = ref([]);
const showProgressCircular = ref(false);
// Computed properties
const serviceOfferingId = computed(() => route.params.serviceOfferingId);
const serviceOfferingVersionId = computed(() => route.params.serviceOfferingVersionId);
// Api State
const apiState = ref({
  serviceOfferingVersion: ApiState.INIT,
  deploymentTargets: ApiState.INIT,
});
const apiStateLoaded = computed(() => apiState.value.serviceOfferingVersion === ApiState.LOADED && apiState.value.deploymentTargets === ApiState.LOADED);
const apiStateLoading = computed(() => apiState.value.serviceOfferingVersion === ApiState.LOADING || apiState.value.deploymentTargets === ApiState.LOADING);
const apiStateError = computed(() => apiState.value.serviceOfferingVersion === ApiState.ERROR || apiState.value.deploymentTargets === ApiState.ERROR);


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
    deploymentTargetSubmodelId: deploymentTargets.value.find(target => target.submodelId === selectedTargetSubmodelId.value).submodelId,
  };

  showProgressCircular.value = true;
  ServiceManagementClient.serviceOfferingVersionsApi.orderServiceOfferingVersionById(
      serviceOfferingId.value,
      serviceOfferingVersionId.value,
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

    const deploymentType = response.data.deploymentDefinition?.deploymentType;
    ServiceManagementClient.serviceOfferingVersionsApi.getDeploymentTargets(deploymentType)
        .then((targetsResponse) => {
          deploymentTargets.value = targetsResponse.data;
          apiState.value.deploymentTargets = ApiState.LOADED;

          if (targetsResponse.data.length > 0) {
            selectedTargetSubmodelId.value = targetsResponse.data[0].submodelId;
          }
        }).catch(logRequestError);
  }).catch(logRequestError);
});
</script>

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
            Deployment Target
          </template>

          <v-card-text v-if="apiState['deploymentTargets'] === 2">
            <progress-circular />
          </v-card-text>
          <v-card-text v-else-if="deploymentTargets?.length > 0">
            <v-container>
              <v-row>
                <v-col>
                  <span>Found '<strong>{{ deploymentTargets.length }}</strong>' available deployment target(s)</span>
                </v-col>
              </v-row>
              <v-row>
                <v-col>
                  <Field
                    v-slot="{ field, errors }"
                    v-model="selectedTargetSubmodelId"
                    name="deployment target"
                    :rules="required"
                  >
                    <v-select
                      v-bind="field"
                      v-model="selectedTargetSubmodelId"
                      :items="deploymentTargets"
                      item-value="submodelId"
                      hint="Select deployment target for service deployment"
                      persistent-hint
                      required
                    >
                      <template #selection="{ item, props }">
                        <v-list-item-title v-bind="props">
                          <strong>{{ item.raw.displayName }}</strong>{{ ` | ${item.raw.mechanismName} ${item.raw.mechanismVersion}` }}
                        </v-list-item-title>
                      </template>
                      <template #item="{ item, props: { onClick } }">
                        <v-list-item @click="onClick">
                          <v-list-item-title>
                            <strong>{{ item.raw.displayName }}</strong>
                          </v-list-item-title>
                          <v-list-item-subtitle>
                            {{ item.raw.mechanismName }} {{ item.raw.mechanismVersion }}
                          </v-list-item-subtitle>
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
            No suitable deployment targets available for this service offering
          </v-card-text>
        </base-material-card>

        <div
          v-if="selectedTargetSubmodelId"
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

<style scoped>
</style>