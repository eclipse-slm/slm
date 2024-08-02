<template>
  <div>
    <v-row
      v-if="apiStateLoading"
      align="center"
      justify="center"
      class="text-center"
    >
      <v-progress-circular
        :size="70"
        :width="7"
        color="secondary"
        indeterminate
      />
    </v-row>

    <div v-if="apiStateError">
      Error
    </div>

    <div v-if="apiStateLoaded">
      <v-row
        v-if="editMode == true && !newServiceOffering"
        align="center"
        justify="center"
      >
        <v-progress-circular
          :size="70"
          :width="7"
          color="secondary"
          indeterminate
        />
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
import ServiceOfferingsRestApi from '@/api/service-management/serviceOfferingsRestApi'

import ApiState from '@/api/apiState'
import {app} from "@/main";
import {useServicesStore} from "@/stores/servicesStore";
import {storeToRefs} from "pinia";

export default {
    name: 'ServiceOfferingCreatePage',

    components: {
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
        // ['editMode', 'creationType', 'serviceOfferingId', 'serviceVendorId'],
    setup(){
      const servicesStore = useServicesStore();
      const {serviceOfferingById} = storeToRefs(servicesStore);
      return {servicesStore, serviceOfferingById}
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
      apiStateServices () {
        return this.servicesStore.apiStateServices
      },
      serviceOfferingCategories () {
        return this.servicesStore.serviceOfferingCategories
      },

      apiStateLoaded () {
        return this.apiStateServices.serviceOfferingCategories === ApiState.LOADED &&
          this.apiStateServices.serviceVendors === ApiState.LOADED
      },
      apiStateLoading () {
        if (this.apiStateServices.serviceOfferingCategories === ApiState.INIT) {
          this.servicesStore.getServiceOfferingCategories();
        }
        if (this.apiStateServices.serviceVendors === ApiState.INIT) {
          this.servicesStore.getServiceVendors();
        }
        return this.apiStateServices.serviceOfferingCategories === ApiState.LOADING || this.apiStateServices.serviceOfferingCategories === ApiState.INIT ||
          this.apiStateServices.serviceVendors === ApiState.LOADING || this.apiStateServices.serviceVendors === ApiState.INIT
      },
      apiStateError () {
        return this.apiStateServices.serviceOfferingCategories === ApiState.ERROR &&
          this.apiStateServices.serviceVendors === ApiState.ERROR
      },
    },

    created () {
      if (this.editMode) {
        this.newServiceOffering = null
        ServiceOfferingsRestApi.getServiceOfferingById(this.serviceOfferingId).then(serviceOffering => {
          this.newServiceOffering = serviceOffering
        })
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
              ServiceOfferingsRestApi.updateServiceOffering(this.newServiceOffering).then(
                response => {
                  if (response.status === 200) {
                    app.config.globalProperties.$toast.info('Successfully updated service offering')
                    this.servicesStore.getServiceOfferings();
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
              ServiceOfferingsRestApi.addServiceOffering(this.newServiceOffering).then(
                response => {
                  if (response.status === 200) {
                    app.config.globalProperties.$toast.info('Successfully created service offering')
                    this.servicesStore.getServiceOfferings();
                    this.$router.push({ path: `/services/vendors/${this.serviceVendorId}` })
                  } else {
                    console.log(response)
                  }
                })
                .catch(exception => {
                  app.config.globalProperties.$toast.error('Failed to create service offering')
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
