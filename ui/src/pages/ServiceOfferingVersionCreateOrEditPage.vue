<template>
  <div>
    <div
      v-if="apiStateLoading"
      class="text-center"
    >
      <v-progress-circular
        :size="70"
        :width="7"
        color="primary"
        indeterminate
      />
    </div>

    <div v-if="apiStateError">
      Error
    </div>

    <div v-if="apiStateLoaded">
      <v-progress-circular
        v-if="editMode == true && !newServiceOfferingVersion"
        :size="70"
        :width="7"
        color="primary"
        indeterminate
      />

      <v-stepper
        v-else
        v-model="createWizardState.currentStep"
        horizontal
        hide-actions
        :items="[$t('ServiceStepper.Common'), 'Deployment Definition', 'Service Options', $t('ServiceStepper.Requirements') ]"
      >
        <!--        <v-stepper-header>
          <v-stepper-item
            step="1"
            :complete="createWizardState.step1.completed"
          >
          </v-stepper-item>

          <v-stepper-item
            step="2"
            :complete="createWizardState.step2.completed"
          >
          </v-stepper-item>

          <v-stepper-item
            step="3"
            :complete="createWizardState.step3.completed"
          >
          </v-stepper-item>

          <v-stepper-item
            step="4"
            :complete="createWizardState.step4.completed"
          >
          </v-stepper-item>
        </v-stepper-header>-->

        <!-- Step 1 - Common -->
        <template #item.1>
          <service-offering-version-wizard-step1-common
            :edit-mode="editMode"
            :service-offering-version="newServiceOfferingVersion"
            :service-vendor-id="serviceVendorId"
            @step-canceled="onStepCanceled"
            @step-completed="onStepCompleted"
          />
        </template>
        <!-- Step 2 - Deployment Definition -->
        <template #item.2>
          <!-- Docker Container -->
          <service-offering-version-wizard-step2-deployment-definition-docker-container
            v-if="newServiceOfferingVersion.deploymentDefinition.deploymentType === 'DOCKER_CONTAINER'"
            :edit-mode="editMode"
            :service-offering-version="newServiceOfferingVersion"
            :service-vendor-id="serviceVendorId"
            @step-canceled="onStepCanceled"
            @step-completed="onStepCompleted"
          />
          <!-- Docker Compose -->
          <service-offering-version-wizard-step2-deployment-definition-docker-compose
            v-if="newServiceOfferingVersion.deploymentDefinition.deploymentType === 'DOCKER_COMPOSE'"
            :edit-mode="editMode"
            :service-offering-version="newServiceOfferingVersion"
            :service-vendor-id="serviceVendorId"
            @step-canceled="onStepCanceled"
            @step-completed="onStepCompleted"
          />
          <!-- Kubernetes -->
          <service-offering-version-wizard-step2-deployment-definition-kubernetes
            v-if="newServiceOfferingVersion.deploymentDefinition.deploymentType === 'KUBERNETES'"
            :edit-mode="editMode"
            :service-offering-version="newServiceOfferingVersion"
            :service-vendor-id="serviceVendorId"
            @step-canceled="onStepCanceled"
            @step-completed="onStepCompleted"
          />
          <!-- Codesys -->
          <service-offering-version-wizard-step2-deployment-definition-codesys
            v-if="newServiceOfferingVersion.deploymentDefinition.deploymentType === 'CODESYS'"
            :edit-mode="editMode"
            :service-offering-version="newServiceOfferingVersion"
            :service-vendor-id="serviceVendorId"
            @step-canceled="onStepCanceled"
            @step-completed="onStepCompleted"
          />
        </template>
        <!-- Step 3 - Service Options -->
        <template #item.3>
          <service-offering-version-wizard-step3-service-options
            :edit-mode="editMode"
            :service-offering-version="newServiceOfferingVersion"
            @step-canceled="onStepCanceled"
            @step-completed="onStepCompleted"
          />
        </template>

        <!--- Step 4 - Service Requirements -->
        <template #item.4>
          <service-offering-version-wizard-step4-requirements
            :edit-mode="editMode"
            :service-offering-version="newServiceOfferingVersion"
            @step-canceled="onStepCanceled"
            @step-completed="onStepCompleted"
          />
        </template>
      </v-stepper>
    </div>
  </div>
</template>

<script>
import ServiceOfferingVersionWizardStep1Common
  from '@/components/service_offerings/wizard_service_offering_version/ServiceOfferingVersionWizardStep1Common'
import ServiceOfferingVersionWizardStep2DeploymentDefinitionCodesys
  from "@/components/service_offerings/wizard_service_offering_version/ServiceOfferingVersionWizardStep2DeploymentDefinitionCodesys.vue";
import ServiceOfferingVersionWizardStep2DeploymentDefinitionDockerContainer
  from '@/components/service_offerings/wizard_service_offering_version/ServiceOfferingVersionWizardStep2DeploymentDefinitionDockerContainer'
import ServiceOfferingVersionWizardStep2DeploymentDefinitionDockerCompose
  from '@/components/service_offerings/wizard_service_offering_version/ServiceOfferingVersionWizardStep2DeploymentDefinitionDockerCompose'
import ServiceOfferingVersionWizardStep2DeploymentDefinitionKubernetes
  from '@/components/service_offerings/wizard_service_offering_version/ServiceOfferingVersionWizardStep2DeploymentDefinitionKubernetes'
import ServiceOfferingVersionWizardStep3ServiceOptions
  from '@/components/service_offerings/wizard_service_offering_version/ServiceOfferingVersionWizardStep3ServiceOptions'
import ServiceOfferingVersionWizardStep4Requirements
  from '@/components/service_offerings/wizard_service_offering_version/ServiceOfferingVersionWizardStep4Requirements'

import ApiState from '@/api/apiState'
import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";
import {storeToRefs} from "pinia";
import ServiceManagementClient from "@/api/service-management/service-management-client";
import logRequestError from "@/api/restApiHelper";

export default {
    name: 'ServiceOfferingCreatePage',

    components: {
      ServiceOfferingVersionWizardStep1Common,
      ServiceOfferingVersionWizardStep2DeploymentDefinitionDockerContainer,
      ServiceOfferingVersionWizardStep2DeploymentDefinitionDockerCompose,
      ServiceOfferingVersionWizardStep2DeploymentDefinitionKubernetes,
      ServiceOfferingVersionWizardStep2DeploymentDefinitionCodesys,
      ServiceOfferingVersionWizardStep3ServiceOptions,
      ServiceOfferingVersionWizardStep4Requirements,
    },
    props: {
      editMode: {
        type: Boolean,
        default: false
      },
      serviceOfferingId: {
        type: String,
        default: null
      },
      serviceOfferingVersionId: {
        type: String,
        default: null
      },
      serviceVendorId: {
        type: String,
        default: null
      }
    },
    setup(){
      const serviceOfferingsStore = useServiceOfferingsStore();
      const {serviceOfferingDeploymentTypePrettyName} = storeToRefs(serviceOfferingsStore)
      return {serviceOfferingsStore, serviceOfferingDeploymentTypePrettyName}
    },
    data () {
      return {
        createWizardState: {
          currentStep: 1,
          step1: {
            completed: false,
          },
          step2: {
            completed: false,
          },
          step3: {
            completed: false,
          },
          step4: {
            completed: false,
          },
        },
        newServiceOfferingVersion: {
          id: null,
          serviceOfferingId: null,
          version: '1.0.0',
          serviceRepositories: [],
          serviceOptionCategories: [],
          deploymentDefinition: {
            deploymentType: null,

            environmentVariables: [],

            imageRepository: '',
            imageTag: '',
            restartPolicy: '',
            portMappings: [],
            volumes: [],
            labels: [],

            composeFile: '',
            dotEnvFile: {
              content: '',
              environmentVariables: [],
            },
            envFiles: {},

            manifestFile: '',

            codesysFile: '',
            applicationPath: ''
          },
          serviceRequirements: []
        },
      }
    },

    computed: {
      apiStateServices () {
        return this.serviceOfferingsStore.apiState
      },
      apiStateLoaded () {
        return this.apiStateServices === ApiState.LOADED
      },
      apiStateLoading () {
        if (this.apiStateServices === ApiState.INIT) {
          this.serviceOfferingsStore.getServiceOfferingDeploymentTypes();
        }
        return this.apiStateServices === ApiState.LOADING || this.apiStateServices === ApiState.INIT
      },
      apiStateError () {
        return this.apiStateServices === ApiState.ERROR
      },
    },

    created () {
      if (this.editMode) {
        this.newServiceOfferingVersion = null
        ServiceManagementClient.serviceOfferingVersionsApi.getServiceOfferingVersionById(this.serviceOfferingId, this.serviceOfferingVersionId)
            .then(result => {
              const serviceOfferingVersion = result.data;
              var requirements = serviceOfferingVersion.serviceRequirements
              for(let requirementId = 0; requirementId < requirements.length; requirementId++) {
                var requirement = requirements[requirementId]
                requirement.id = requirementId
                requirement.activeLogic = null
                for(let logicId = 0; logicId < requirement.logics.length; logicId++) {
                  var logic = requirement.logics[logicId]
                  logic.id = logicId
                  for(let propertyId = 0; propertyId < logic.properties.length; propertyId++) {
                    logic.properties[propertyId].id = propertyId
                  }
                  requirement.logics[logicId] = logic
                }
                requirements[requirementId] = requirement
              }
              serviceOfferingVersion.serviceRequirements = requirements
              this.newServiceOfferingVersion = serviceOfferingVersion
            }).catch(logRequestError)
      }
      else {
        this.newServiceOfferingVersion.serviceOfferingId = this.serviceOfferingId
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
          case 4:
            let serviceOfferingVersionDTO = JSON.parse(JSON.stringify(this.newServiceOfferingVersion))

            // Delete all port mappings, volumes, labels and environment variables which are service options
            if (serviceOfferingVersionDTO.deploymentDefinition.deploymentType === 'DOCKER_CONTAINER') {
              for (let i = serviceOfferingVersionDTO.deploymentDefinition.portMappings.length - 1; i >= 0; i--) {
                if (serviceOfferingVersionDTO.deploymentDefinition.portMappings[i].isServiceOption) {
                  serviceOfferingVersionDTO.deploymentDefinition.portMappings.splice(i, 1)
                }
              }

              for (let i = serviceOfferingVersionDTO.deploymentDefinition.volumes.length - 1; i >= 0; i--) {
                if (serviceOfferingVersionDTO.deploymentDefinition.volumes[i].isServiceOption) {
                  serviceOfferingVersionDTO.deploymentDefinition.volumes.splice(i, 1)
                }
              }

              for (let i = serviceOfferingVersionDTO.deploymentDefinition.labels.length - 1; i >= 0; i--) {
                if (serviceOfferingVersionDTO.deploymentDefinition.labels[i].isServiceOption) {
                  serviceOfferingVersionDTO.deploymentDefinition.labels.splice(i, 1)
                }
              }

              for (let i = serviceOfferingVersionDTO.deploymentDefinition.environmentVariables.length - 1; i >= 0; i--) {
                if (serviceOfferingVersionDTO.deploymentDefinition.environmentVariables[i].isServiceOption) {
                  serviceOfferingVersionDTO.deploymentDefinition.environmentVariables.splice(i, 1)
                }
              }
            } else if (serviceOfferingVersionDTO.deploymentDefinition.deploymentType === 'DOCKER_COMPOSE') {
              serviceOfferingVersionDTO.deploymentDefinition.composeFile = this.newServiceOfferingVersion.deploymentDefinition.composeFile
              serviceOfferingVersionDTO.deploymentDefinition.dotEnvFile = this.newServiceOfferingVersion.deploymentDefinition.dotEnvFile.content
              serviceOfferingVersionDTO.deploymentDefinition.envFiles = {}
              for (const [key, value] of Object.entries(this.newServiceOfferingVersion.deploymentDefinition.envFiles)) {
                serviceOfferingVersionDTO.deploymentDefinition.envFiles[key] = value.content
              }
            }

            if (this.editMode) {
              switch (serviceOfferingVersionDTO.deploymentDefinition.deploymentType) {
                case 'Docker Container':
                  serviceOfferingVersionDTO.deploymentDefinition.deploymentType = 'DOCKER_CONTAINER'
                  break;
                case 'Docker Compose':
                  serviceOfferingVersionDTO.deploymentDefinition.deploymentType = 'DOCKER_COMPOSE'
                  break;
                default:
                  break;
              }
              ServiceManagementClient.serviceOfferingVersionsApi.createOrUpdateServiceOfferingVersionWithId(
                  serviceOfferingVersionDTO.serviceOfferingId, serviceOfferingVersionDTO.id,serviceOfferingVersionDTO
              ).then(
                response => {
                  if (response.status === 200) {

                    if(serviceOfferingVersionDTO.deploymentDefinition.deploymentType === 'CODESYS'){
                      ServiceManagementClient.serviceOfferingVersionsApi.createOrUpdateServiceOfferingFileWithId(
                          response.data.serviceOfferingId,
                          response.data.serviceOfferingVersionId,
                          this.newServiceOfferingVersion.deploymentDefinition.codesysFile
                      ).then(uploadFileResponse => {
                        this.$toast.info('Successfully uploaded file for offering')
                      }).catch(error => console.log(error));
                    }

                    this.$toast.info('Successfully updated service offering version')
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
              switch (serviceOfferingVersionDTO.deploymentDefinition.deploymentType) {
                case 'Docker Container':
                  serviceOfferingVersionDTO.deploymentDefinition = 'DOCKER_CONTAINER'
                  break
                case 'Docker Compose':
                  serviceOfferingVersionDTO.deploymentDefinition = 'DOCKER_COMPOSE'
                  break
                case 'Kubernetes':
                  serviceOfferingVersionDTO.deploymentDefinition = 'KUBERNETES'
                  break
                case 'Codesys':
                  serviceOfferingVersionDTO.deploymentDefinition = 'CODESYS'
                  break
                default:
                  break
              }
              ServiceManagementClient.serviceOfferingVersionsApi.createServiceOfferingVersionWithAutoGeneratedId(serviceOfferingVersionDTO.serviceOfferingId, serviceOfferingVersionDTO).then(
                response => {
                  if (response.status === 200) {

                    if(serviceOfferingVersionDTO.deploymentDefinition.deploymentType === 'CODESYS'){
                      ServiceManagementClient.serviceOfferingVersionsApi.createOrUpdateServiceOfferingFileWithId(
                          response.data.serviceOfferingId,
                          response.data.serviceOfferingVersionId,
                          this.newServiceOfferingVersion.deploymentDefinition.codesysFile
                      ).then(uploadFileResponse => {
                        this.$toast.info('Successfully uploaded file for offering')
                      }).catch(error => console.log(error));
                    }

                    this.$toast.info('Successfully created service offering version')
                    this.serviceOfferingsStore.getServiceOfferings();
                    this.$router.push({ path: `/services/vendors/${this.serviceVendorId}` })
                  } else {
                    console.log(response)
                  }
                })
                .catch(exception => {
                  this.$toast.error('Failed to create service offering version')
                  console.log('Service offering version creation failed: ' + exception?.response?.data?.message)
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
