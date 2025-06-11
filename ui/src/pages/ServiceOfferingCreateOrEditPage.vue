<template>
  <div>
    <v-row
      v-if="apiState === ApiState.INIT || apiState === ApiState.LOADING || apiState === ApiState.UPDATING"
      align="center"
      justify="center"
      class="text-center"
    >
      <progress-circular />
    </v-row>

    <div v-if="apiState === ApiState.ERROR">
      Error
    </div>

    <div v-if="apiState === ApiState.LOADED">
      <v-row
        v-if="editMode == true && !newServiceOffering"
        align="center"
        justify="center"
      >
        <progress-circular />
      </v-row>

      <v-stepper 
        v-else
        v-model="createWizardState.currentStep"
        hide-actions
        :items="[$t('ServiceStepper.Common')]"
      >
        <template
          v-if="creationType === 'manual'"
          #item.1
        >
          <service-offering-wizard-manual-step1-common
            v-if="creationType === 'manual'"
            :edit-mode="editMode"
            :new-service-offering="newServiceOffering"
            @step-canceled="onStepCanceled"
            @step-completed="onStepCompleted"
          />
        </template>

        <template
          v-if="creationType === 'git'"
          #item.1
        >
          <service-offering-wizard-git-step1-common
            :new-service-offering="newServiceOffering"
            :service-vendor-id="serviceVendorId"
            @step-canceled="onStepCanceled"
            @step-completed="onStepCompleted"
          />
        </template>
      </v-stepper>
    </div>
  </div>
</template>

<script>
import ServiceOfferingWizardManualStep1Common
  from '@/components/service_offerings/wizard_service_offering/ServiceOfferingWizardManualStep1Common'
import ServiceOfferingWizardGitStep1Common
  from "@/components/service_offerings/wizard_service_offering/ServiceOfferingWizardGitStep1Common";

import ApiState from '@/api/apiState'
import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";
import {storeToRefs} from "pinia";
import ServiceManagementClient from "@/api/service-management/service-management-client";
import logRequestError from "@/api/restApiHelper";
import ProgressCircular from "@/components/base/ProgressCircular.vue";

export default {
    name: 'ServiceOfferingCreatePage',

    components: {
      ProgressCircular,
      ServiceOfferingWizardManualStep1Common,
      ServiceOfferingWizardGitStep1Common
    },
    props: {
      editMode: {
        type: Boolean,
        default: false
      },
      creationType: {
        type: String,
        default: ""
      },
      serviceOfferingId: {
        type: String,
        default: null
      },
      serviceVendorId: {
        type: String,
        default: null
      },
    },
    setup(){
      const serviceOfferingsStore = useServiceOfferingsStore();
      const {apiState, serviceOfferingById} = storeToRefs(serviceOfferingsStore);
      return {apiState, serviceOfferingsStore, serviceOfferingById}
    },
    data () {
      return {
        createWizardState: {
          currentStep: 1,
          step1: {
            completed: false,
          }
        },
        newServiceOffering: {
          name: 'New Service',
          serviceVendorId: this.serviceVendorId,
          description: 'My new service description',
          shortDescription: 'My new service short description',
          coverImage: 'none',
          serviceCategoryId: null,
        },
      }
    },

    computed: {
      ApiState() {
        return ApiState
      }
    },

    created () {
      if (this.editMode) {
        this.newServiceOffering = null
        ServiceManagementClient.serviceOfferingsApi.getServiceOfferingById(this.serviceOfferingId).then(response => {
          this.newServiceOffering = response.data
        }).then().catch(logRequestError);
      }
    },

    methods: {
      onStepCanceled (stepNumber) {
        switch (stepNumber) {
          case 1:
            this.$router.push({ path: `/services/vendors/${this.serviceVendorId}` })
            break
          default:
            this.createWizardState.currentStep--
            break
        }
      },
      onStepCompleted (stepNumber) {
        switch (stepNumber) {
          case 1:
            if (this.newServiceOffering.coverImage.includes('image/')) {
              this.newServiceOffering.coverImage =
                  this.newServiceOffering.coverImage.slice(this.newServiceOffering.coverImage.indexOf(',') + 1, this.newServiceOffering.coverImage.length)
            }

            if (this.editMode) {
              ServiceManagementClient.serviceOfferingsApi.createOrUpdateServiceOfferingWithId(this.serviceVendorId, this.newServiceOffering).then(
                response => {
                  if (response.status === 200) {
                    this.$toast.info('Successfully updated service offering')
                    this.serviceOfferingsStore.getServiceOfferings();
                    this.$router.push({ path: `/services/vendors/${this.serviceVendorId}` })
                  } else {
                    console.log(response)
                  }
                })
                .catch(exception => {
                  console.log('Service offering creation failed: ' + exception.response.data.message)
                  console.log(exception)
                })
            } else {
              ServiceManagementClient.serviceOfferingsApi.createServiceOfferingWithAutoGeneratedId(this.newServiceOffering).then(
                response => {
                  if (response.status === 200) {
                    this.$toast.info('Successfully created service offering')
                    this.serviceOfferingsStore.getServiceOfferings();
                    this.$router.push({ path: `/services/vendors/${this.serviceVendorId}` })
                  } else {
                    console.log(response)
                  }
                })
                .catch(exception => {
                  this.$toast.error('Failed to create service offering')
                  console.log('Service offering creation failed: ' + exception.response.data.message)
                  console.log(exception)
                })
            }
            break
          default:
            this.createWizardState.currentStep++
            break
        }
      },
    },
  }
</script>
