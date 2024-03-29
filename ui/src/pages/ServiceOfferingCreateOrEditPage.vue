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
        horizontal
      >
        <v-stepper-header>
          <v-stepper-step
            step="1"
            :complete="createWizardState.step1.completed"
          >
            {{ $t('ServiceStepper.Common') }}
          </v-stepper-step>
        </v-stepper-header>

        <v-stepper-items>
          <!-- Step 1 - Manual Service Offering Creation -->
          <v-stepper-content
            v-if="creationType == 'manual'"
            step="1"
          >
            <service-offering-wizard-manual-step1-common
              :edit-mode="editMode"
              :new-service-offering="newServiceOffering"
              @step-canceled="onStepCanceled"
              @step-completed="onStepCompleted"
            />
          </v-stepper-content>
          <!-- Step 1 - Git-based Service Offering -->
          <v-stepper-content
            v-if="creationType == 'git'"
            step="1"
          >
            <service-offering-wizard-git-step1-common
              :new-service-offering="newServiceOffering"
              :service-vendor-id="serviceVendorId"
              @step-canceled="onStepCanceled"
              @step-completed="onStepCompleted"
            />
          </v-stepper-content>
        </v-stepper-items>
      </v-stepper>
    </div>
  </div>
</template>

<script>
  import ServiceOfferingWizardManualStep1Common from '@/components/service_offerings/wizard_service_offering/ServiceOfferingWizardManualStep1Common'
  import ServiceOfferingWizardGitStep1Common
    from "@/components/service_offerings/wizard_service_offering/ServiceOfferingWizardGitStep1Common";
  import ServiceOfferingsRestApi from '@/api/service-management/serviceOfferingsRestApi'
  import { mapGetters } from 'vuex'
  import ApiState from '@/api/apiState'
  import Vue from 'vue'

  export default {
    name: 'ServiceOfferingCreatePage',

    components: {
      ServiceOfferingWizardManualStep1Common,
      ServiceOfferingWizardGitStep1Common
    },
    props: ['editMode', 'creationType', 'serviceOfferingId', 'serviceVendorId'],

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

    created () {
      if (this.editMode) {
        this.newServiceOffering = null
        ServiceOfferingsRestApi.getServiceOfferingById(this.serviceOfferingId).then(serviceOffering => {
          this.newServiceOffering = serviceOffering
        })
      }
    },

    computed: {
      ...mapGetters([
        'apiStateServices',
        'serviceOfferingCategories',
        'serviceOfferingById',
      ]),
      apiStateLoaded () {
        return this.apiStateServices.serviceOfferingCategories === ApiState.LOADED &&
          this.apiStateServices.serviceVendors === ApiState.LOADED
      },
      apiStateLoading () {
        if (this.apiStateServices.serviceOfferingCategories === ApiState.INIT) {
          this.$store.dispatch('getServiceOfferingCategories')
        }
        if (this.apiStateServices.serviceVendors === ApiState.INIT) {
          this.$store.dispatch('getServiceVendors')
        }
        return this.apiStateServices.serviceOfferingCategories === ApiState.LOADING || this.apiStateServices.serviceOfferingCategories === ApiState.INIT ||
          this.apiStateServices.serviceVendors === ApiState.LOADING || this.apiStateServices.serviceVendors === ApiState.INIT
      },
      apiStateError () {
        return this.apiStateServices.serviceOfferingCategories === ApiState.ERROR &&
          this.apiStateServices.serviceVendors === ApiState.ERROR
      },
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
                    Vue.$toast.info('Successfully updated service offering')
                    this.$store.dispatch('getServiceOfferings')
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
                    Vue.$toast.info('Successfully created service offering')
                    this.$store.dispatch('getServiceOfferings')
                    this.$router.push({ path: `/services/vendors/${this.serviceVendorId}` })
                  } else {
                    console.log(response)
                  }
                })
                .catch(exception => {
                  Vue.$toast.error('Failed to create service offering')
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
