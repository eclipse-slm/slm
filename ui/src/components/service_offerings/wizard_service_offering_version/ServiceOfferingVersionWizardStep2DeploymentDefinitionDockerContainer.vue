<template>
  <v-container
    fluid
  >
    <ValidationForm
      ref="observer"
      v-slot="{ meta, handleSubmit, validate }"
    >
      <v-list :opened="['Container', 'Environment']">
        <v-list-group value="Container">
          <template #activator="{props}">
            <v-list-item
              v-bind="props"
              title="Container"
            />
          </template>

          <div v-if="config_Container">
            <docker-container-image-name
              :new-service-offering="serviceOfferingVersion"
            />

            <service-repository-select
              v-model="serviceOfferingVersion.serviceRepositories"
              label="Docker Registry Credentials"
              :service-vendor-id="serviceVendorId"
              :multiple="false"
            />

            <docker-container-restart-policy
              :new-service-offering="serviceOfferingVersion"
            />

            <docker-container-port-mapping
              :port-mappings="serviceOfferingVersion.deploymentDefinition.portMappings"
              :editable="true"
            />

            <docker-container-volume-mapping
              :volumes="serviceOfferingVersion.deploymentDefinition.volumes"
              :editable="true"
            />

            <docker-container-labels
              :labels="serviceOfferingVersion.deploymentDefinition.labels"
              :editable="true"
            />
          </div>
        </v-list-group>

        <v-list-group value="Environment">
          <template #activator="{props}">
            <v-list-item
              v-bind="props"
              title="Environment"
            />
          </template>

          <docker-container-environment-variables
            v-if="config_Environment"
            :environment-variables="serviceOfferingVersion.deploymentDefinition.environmentVariables"
            :editable="true"
            :addable="true"
          />
        </v-list-group>
        <v-row>
          <v-col />
        </v-row>
      </v-list>
      <!-- Navigation Buttons-->
      <v-card-actions>
        <v-btn
          variant="tonal"
          :color="$vuetify.theme.themes.light.colors.secondary"
          type="submit"
          @click="onCancelButtonClicked()"
        >
          {{ $t('buttons.Back') }}
        </v-btn>
        <v-spacer />
        <v-btn
          variant="tonal"
          :color="!meta.valid ? $vuetify.theme.themes.light.colors.disable : $vuetify.theme.themes.light.colors.secondary"
          @click="!meta.valid ? validate() : handleSubmit(onNextButtonClicked)"
        >
          {{ $t('buttons.Next') }}
        </v-btn>
      </v-card-actions>
    </ValidationForm>
  </v-container>
</template>

<script>
  import DockerContainerEnvironmentVariables from './Docker/DockerEnvironmentVariables'
  import { isServiceOptionExisting, deleteServiceOption, isEnvVarExisting } from '@/utils/serviceOptionUtil'
  import DockerContainerRestartPolicy
    from '@/components/service_offerings/wizard_service_offering_version/Docker/DockerContainerRestartPolicy'
  import DockerContainerLabels from '@/components/service_offerings/wizard_service_offering_version/Docker/DockerContainerLabels'
  import DockerContainerVolumeMapping from '@/components/service_offerings/wizard_service_offering_version/Docker/DockerContainerVolumes'
  import DockerContainerPortMapping
    from '@/components/service_offerings/wizard_service_offering_version/Docker/DockerContainerPortMappings'
  import DockerContainerImageName from '@/components/service_offerings/wizard_service_offering_version/Docker/DockerContainerImageName'
  import ServiceRepositorySelect from '@/components/service_offerings/wizard_service_offering_version/ServiceRepositorySelect'
  import {Field, Form as ValidationForm } from "vee-validate";
  import * as yup from 'yup';

  export default {
    name: 'ServiceOfferingVersionWizardStep2DeploymentDefinitionDockerContainer',
    components: {
      DockerContainerRestartPolicy,
      DockerContainerLabels,
      DockerContainerVolumeMapping,
      DockerContainerPortMapping,
      DockerContainerImageName,
      DockerContainerEnvironmentVariables,
      ServiceRepositorySelect,
      ValidationForm
    },
    props: ['editMode', 'serviceOfferingVersion', 'serviceVendorId'],

    data () {
      return {
        stepNumber: 2,
        config_Container: true,
        config_Environment: true,
      }
    },

    created () {
    },

    methods: {
      onCancelButtonClicked () {
        this.$emit('step-canceled', this.stepNumber)
      },
      async onNextButtonClicked () {
        if (this.editMode) {
          // Check for Service Options without related Environment Variable
          for (const serviceOptionCategory of this.serviceOfferingVersion.serviceOptionCategories) {
            for (const serviceOption of serviceOptionCategory.serviceOptions) {
              // Remove Service Option without Environment Variable
              if (!isEnvVarExisting(serviceOption.key, this.serviceOfferingVersion.deploymentDefinition.environmentVariables)) {
                this.serviceOfferingVersion.serviceOptionCategories = deleteServiceOption(serviceOption.key, this.serviceOfferingVersion.serviceOptionCategories)
              }
            }
          }
          // Check for Environment Variables for Service Options
          for (const envVar of this.serviceOfferingVersion.deploymentDefinition.environmentVariables) {
            if (isServiceOptionExisting(envVar.key, this.serviceOfferingVersion.serviceOptionCategories)) {
              // Remove Service Option if it is disabled for this Environment Variable
              if (!envVar.serviceOption) {
                this.serviceOfferingVersion.serviceOptionCategories = deleteServiceOption(envVar.key, this.serviceOfferingVersion.serviceOptionCategories)
              }
            } else {
              // Add Service Option if it is enabled for this Environment Variable
              if (envVar.serviceOption) {
                this.serviceOfferingVersion.serviceOptionCategories[0].serviceOptions.push({
                  key: envVar.key,
                  relation: '',
                  name: envVar.key,
                  description: '',
                  optionType: 'ENVIRONMENT_VARIABLE',
                  defaultValue: envVar.value,
                  valueType: 'STRING',
                  valueOptions: [],
                  required: false,
                  editable: false,
                })
              }
            }
          }
        } else {
          this.serviceOfferingVersion.serviceOptionCategories = []

          if (this.serviceOfferingVersion.deploymentDefinition.environmentVariables.length > 0) {
            const envVarServiceOptions = []
            for (const envVar of this.serviceOfferingVersion.deploymentDefinition.environmentVariables) {
              envVarServiceOptions.push({
                key: envVar.key,
                relation: '',
                name: envVar.key,
                description: '',
                defaultValue: envVar.value,
                optionType: 'ENVIRONMENT_VARIABLE',
                valueType: 'STRING',
                valueOptions: [],
                required: false,
                editable: false,
              })
            }

            this.serviceOfferingVersion.serviceOptionCategories.push({
              id: this.serviceOfferingVersion.serviceOptionCategories.length + 1,
              name: 'Environment',
              serviceOptions: envVarServiceOptions,
            })
          }

          if (this.serviceOfferingVersion.deploymentDefinition.labels.length > 0) {
            const labelServiceOptions = []
            for (const label of this.serviceOfferingVersion.deploymentDefinition.labels) {
              labelServiceOptions.push({
                key: label.name,
                relation: '',
                name: label.name,
                description: '',
                defaultValue: label.value,
                optionType: 'LABEL',
                valueType: 'STRING',
                valueOptions: [],
                required: false,
                editable: false,
              })
            }

            this.serviceOfferingVersion.serviceOptionCategories.push({
              id: this.serviceOfferingVersion.serviceOptionCategories.length + 1,
              name: 'Labels',
              serviceOptions: labelServiceOptions,
            })
          }

          if (this.serviceOfferingVersion.deploymentDefinition.volumes.length > 0) {
            const volumeServiceOptions = []
            for (const volume of this.serviceOfferingVersion.deploymentDefinition.volumes) {
              volumeServiceOptions.push({
                key: volume.containerPath,
                relation: '',
                name: volume.name,
                description: '',
                defaultValue: volume.name,
                optionType: 'VOLUME',
                valueType: 'VOLUME',
                valueOptions: [],
                required: false,
                editable: false,
              })
            }

            this.serviceOfferingVersion.serviceOptionCategories.push({
              id: this.serviceOfferingVersion.serviceOptionCategories.length + 1,
              name: 'Volumes',
              serviceOptions: volumeServiceOptions,
            })
          }

          if (this.serviceOfferingVersion.deploymentDefinition.portMappings.length > 0) {
            const portMappingServiceOptions = []
            for (const portMapping of this.serviceOfferingVersion.deploymentDefinition.portMappings) {
              portMappingServiceOptions.push({
                key: portMapping.containerPort,
                relation: '',
                name: portMapping.containerPort,
                description: '',
                defaultValue: portMapping.hostPort,
                optionType: 'PORT_MAPPING',
                valueType: 'PORT',
                valueOptions: [],
                required: false,
                editable: false,
              })
            }

            this.serviceOfferingVersion.serviceOptionCategories.push({
              id: this.serviceOfferingVersion.serviceOptionCategories.length + 1,
              name: 'Port Mappings',
              serviceOptions: portMappingServiceOptions,
            })
          }
        }

        this.$emit('step-completed', this.stepNumber)
      },
    },

  }
</script>
