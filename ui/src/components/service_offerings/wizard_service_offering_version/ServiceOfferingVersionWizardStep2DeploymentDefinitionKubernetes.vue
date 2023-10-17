<template>
  <div>
    <v-form
      v-model="validForm"
    >
      <v-list expand>
        <!-- Kubernetes Manifest File !-->
        <v-list-group :value="true">
          <template #activator>
            <v-list-item-content>
              <v-list-item-title>
                Kubernetes Manifest File
              </v-list-item-title>
            </v-list-item-content>
          </template>
          <v-row>
            <v-col>
              <v-file-input
                v-model="uploadedManifestFile"
                accept=".yml,.yaml"
                label="Click here to select Kubernetes Manifest file"
                auto-grow
                dense
                outlined
                @change="onLoadManifestFileClicked"
              />
            </v-col>
            <v-spacer />
          </v-row>
          <v-textarea
            v-model="serviceOfferingVersion.deploymentDefinition.manifestFile"
            class="full-width"
            outlined
            dense
          />
        </v-list-group>

        <!-- Environment Variables (parsed Kubernetes objects)!-->
        <v-list-group
            v-if="envVarsDefined"
            :value="true"
        >
          <template #activator>
            <v-list-item-content>
              <v-list-item-title>
                Environment (parsed from Kubernetes Objects)
              </v-list-item-title>
            </v-list-item-content>
          </template>
          <docker-container-environment-variables
              subheader="Environment Variables"
              :environment-variables="serviceOfferingVersion.deploymentDefinition.environmentVariables"
              :addable="false"
          />
        </v-list-group>

        <!-- Environment Variables (string replace) !-->
        <v-list-group
            v-if="stringReplacementsDefined"
            :value="true"
        >
          <template #activator>
            <v-list-item-content>
              <v-list-item-title>
                String Replacement (parsed from regular expression)
              </v-list-item-title>
            </v-list-item-content>
          </template>
          <docker-container-environment-variables
              subheader="String Replacement Variables"
              :environment-variables="serviceOfferingVersion.deploymentDefinition.stringReplacements"
              :addable="false"
          />
        </v-list-group>

      </v-list>

      <service-repository-select
          v-model="serviceOfferingVersion.serviceRepositories"
          label="Private Container Registries Credentials"
          :service-vendor-id="serviceVendorId"
          :multiple="true"
      />

    </v-form>

    <!-- Navigation Buttons-->
    <v-card-actions>
      <v-btn
        color="secondary"
        @click="onCancelButtonClicked()"
      >
        {{ $t('buttons.Back') }}
      </v-btn>
      <v-spacer />
      <v-btn
        color="secondary"
        @click="onNextButtonClicked()"
      >
        {{ $t('buttons.Next') }}
      </v-btn>
    </v-card-actions>
  </div>
</template>

<script>
  import 'vue-json-pretty/lib/styles.css'
  import YAML from 'yaml'
  const { parse } = require('dot-properties')
  import DockerContainerEnvironmentVariables
    from '@/components/service_offerings/wizard_service_offering_version/Docker/DockerEnvironmentVariables'
  import ServiceRepositorySelect
    from "@/components/service_offerings/wizard_service_offering_version/ServiceRepositorySelect.vue";

  export default {
    name: 'ServiceOfferingVersionWizardStep2DeploymentDefinitionKubernetes',
    components: {ServiceRepositorySelect, DockerContainerEnvironmentVariables},
    props: ['editMode', 'serviceOfferingVersion', 'serviceVendorId'],

    data () {
      return {
        stepNumber: 2,
        validForm: false,
        uploadedManifestFile: null,
        uploadedDotEnvFile: null,
        uploadedEnvFiles: [],
        envVarsDefined: false,
        stringReplacementsDefined: false,
        envFilesDefined: false,
      }
    },

    mounted() {
      if (this.editMode) {
        this.parseManifestFile(this.serviceOfferingVersion.deploymentDefinition.manifestFile)
        this.serviceOfferingVersion.serviceOptionCategories.forEach(serviceOptionCategory => {
          serviceOptionCategory.serviceOptions.forEach(serviceOption => {
            if (serviceOption.optionType === "ENVIRONMENT_VARIABLE") {
              let envVar = this.serviceOfferingVersion.deploymentDefinition.environmentVariables.find(envVar => envVar.key === serviceOption.key && envVar.serviceName === serviceOption.relation);
              if (typeof envVar !== "undefined"){
                envVar.isServiceOption = true
              }
            }
            if (serviceOption.optionType === "STRING_REPLACE") {
              let envVarKey = serviceOption.key
              let envVar = this.serviceOfferingVersion.deploymentDefinition.stringReplacements.find(envVar => envVar.key === envVarKey);
              if (typeof envVar !== "undefined"){
                envVar.isServiceOption = true
              }
            }
          })
        })
      }
    },

    methods: {
      onCancelButtonClicked () {
        this.$emit('step-canceled', this.stepNumber)
      },
      onNextButtonClicked () {

        // define defaults
        let serviceOptions = []

        // cache previously saved options, if available
        let envVarServiceOptions = this.serviceOfferingVersion.serviceOptionCategories.map(soc => soc.serviceOptions).flat().filter((option) => option.optionType === 'ENVIRONMENT_VARIABLE')
        let stringReplacementServiceOptions = this.serviceOfferingVersion.serviceOptionCategories.map(soc => soc.serviceOptions).flat().filter((option) => option.optionType === 'STRING_REPLACE')
        // console.log(this.serviceOfferingVersion.serviceOptionCategories)
        // console.log({envVarServiceOptions})
        // console.log({stringReplacementServiceOptions})

        // add chosen environment variables (checkbox in ui)
        this.serviceOfferingVersion.deploymentDefinition.environmentVariables?.forEach((envVar) => {
          if (envVar.isServiceOption === true) {

            // load data from previously saved option, if available. Else create new option item with defaults
            let previousOption = envVarServiceOptions?.find(option => option.relation === envVar.serviceName && option.key === envVar.key)
            if (typeof previousOption !== "undefined"){
              serviceOptions.push(previousOption)
            } else {
              serviceOptions.push({
                relation: `${envVar.serviceName}`,
                key: envVar.key,
                name: envVar.key,
                description: '',
                optionType: 'ENVIRONMENT_VARIABLE',
                valueType: 'STRING',
                value: envVar.value,
                required: false,
                editable: false,
              })
            }
          }
        })

        // add service category (env vars), and clear arry
        this.serviceOfferingVersion.serviceOptionCategories = []
        if(serviceOptions.length > 0){
          this.serviceOfferingVersion.serviceOptionCategories.push({
            id: 1,
            name: 'Environment Variables (common)',
            serviceOptions: serviceOptions,
          })
          serviceOptions = []
        }

        // add chosen string replacement variables (checkbox in ui)
        this.serviceOfferingVersion.deploymentDefinition.stringReplacements?.forEach((envVar) => {
          if (envVar.isServiceOption === true) {

            // load data from previously saved option, if available. Else create new option item with defaults
            let previousOption = stringReplacementServiceOptions?.find(option => option.relation === envVar.serviceName && option.key === envVar.key)
            if (typeof previousOption !== "undefined"){
              serviceOptions.push(previousOption)
            } else {
              serviceOptions.push({
                relation: "$",
                key: envVar.key,
                name: envVar.key,
                description: '',
                optionType: 'STRING_REPLACE',
                valueType: 'STRING',
                value: envVar.value,
                required: false,
                editable: false,
              })
            }
          }
        })

        // add service category (env vars)
        if(serviceOptions.length > 0){
          this.serviceOfferingVersion.serviceOptionCategories.push({
            id: 2,
            name: 'Environment Variables (string replacement)',
            serviceOptions: serviceOptions,
          })
        }

        this.$emit('step-completed', this.stepNumber)
      },


      onLoadManifestFileClicked () {
        if (!this.uploadedManifestFile) {
          this.serviceOfferingVersion.deploymentDefinition.manifestFile = 'No file chosen'
        } else {
          const reader = new FileReader()
          reader.readAsText(this.uploadedManifestFile)
          reader.onload = () => {
            this.serviceOfferingVersion.deploymentDefinition.manifestFile = reader.result
            this.parseManifestFile(this.serviceOfferingVersion.deploymentDefinition.manifestFile)
          }
        }
      },


      parseManifestFile(manifestYamlContent) {

        // define defaults, if not previously defined
        if (typeof this.serviceOfferingVersion.deploymentDefinition.stringReplacements === "undefined"){
          this.serviceOfferingVersion.deploymentDefinition.stringReplacements = []
        }
        if (typeof this.serviceOfferingVersion.deploymentDefinition.environmentVariables === "undefined"){
          this.serviceOfferingVersion.deploymentDefinition.environmentVariables = []
        }
        let stringReplacementsDefined = false;
        let envVarsDefined = false;

        // split yaml input, if multiple files are in it
        const parsedYamlDefinitions = YAML.parseAllDocuments(manifestYamlContent).map(obj => obj.toJSON())
        for (const parsedDefinition of parsedYamlDefinitions) {

          try {
            // check if definition is a deployment (-> currently only env vars for deployments are supported)
            if(parsedDefinition.kind && parsedDefinition.kind === "Deployment"){

              // check if definition has containers array in the template spec
              if(Object(parsedDefinition.spec.template.spec).hasOwnProperty("containers")) {

                // iterate through the env vars of the containers of the deployment
                for (const container of parsedDefinition.spec.template.spec.containers) {
                  if (Object(container).hasOwnProperty("env")) {
                    for (const envVar of container.env) {

                      // create env var item for later use
                      let item = {
                        key: envVar.name,
                        value: envVar.value,
                        serviceName: parsedDefinition.metadata.name + "|" + container.name,
                      }
                      this.serviceOfferingVersion.deploymentDefinition.environmentVariables.push(item)
                    }
                    envVarsDefined = true;
                  }
                }
              }

              // check if definition has init-containers array in the template spec
              if(Object(parsedDefinition.spec.template.spec).hasOwnProperty("initContainers")) {

                // iterate through the env vars of the containers of the deployment
                for (const container of parsedDefinition.spec.template.spec.initContainers) {
                  if (Object(container).hasOwnProperty("env")) {
                    for (const envVar of container.env) {

                      // create env var item for later use
                      let item = {
                        key: envVar.name,
                        value: envVar.value,
                        serviceName: parsedDefinition.metadata.name + "|" + container.name,
                      }
                      this.serviceOfferingVersion.deploymentDefinition.environmentVariables.push(item)
                    }
                    envVarsDefined = true;
                  }
                }
              }

            }
          } catch (e) {
            if (e instanceof TypeError) {
              // catch specific error
              if (typeof parsedDefinition !== "undefined" && parsedDefinition !== null){
                console.log(parsedDefinition)
                if (Object.hasOwn(parsedDefinition, 'apiVersion') && Object.hasOwn(parsedDefinition, 'kind') && Object.hasOwn(parsedDefinition, 'metadata')){
                  console.log("Seams like the parsed spec of '" + parsedDefinition.apiVersion + "/" + parsedDefinition.kind + "' (" + parsedDefinition.metadata.name + ")has no containers in template spec! Skipping...")
                }
              }
               else {
                console.error("TypeError. Cannot parse manifest...")
              }
            } else {
              throw e; // let others bubble up
            }
          }
        }

        // sort out duplicated env vars by key, for each deployment-container combination
        const uniqueEnvVars = [];
        if (typeof this.serviceOfferingVersion.deploymentDefinition.environmentVariables !== "undefined" && this.serviceOfferingVersion.deploymentDefinition.environmentVariables.length > 0){
          this.serviceOfferingVersion.deploymentDefinition.environmentVariables.map(x => uniqueEnvVars.filter(a => a.key === x.key && a.serviceName === x.serviceName).length > 0 ? null : uniqueEnvVars.push(x));
        }

        // try to find defined string replacements
        const regexMatches = manifestYamlContent.match(/\${{.*}}/gm)
        if(regexMatches && regexMatches.length > 0){

          // sort out duplicated matches, in order to achieve global uniqueness
          const uniqueRegexMatches = [... new Set(regexMatches)];
          for (const regexMatch of uniqueRegexMatches) {

            // create item for later use
            let item = {
              key: regexMatch,
              value: regexMatch,
              serviceName: "$",
            }
            this.serviceOfferingVersion.deploymentDefinition.stringReplacements.push(item)
            stringReplacementsDefined = true
          }
        }

        // enable the display for choosing service options
        this.envVarsDefined = envVarsDefined
        this.stringReplacementsDefined = stringReplacementsDefined
      },
    },
  }
</script>
