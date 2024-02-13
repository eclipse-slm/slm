<template>
  <div>
    <v-form
      v-model="validForm"
    >
      <v-list expand>
        <!-- Codesys File !-->
        <v-list-group :value="true">
          <template #activator>
            <v-list-item-content>
              <v-list-item-title>
                Codesys Application Files
              </v-list-item-title>
            </v-list-item-content>
          </template>
          <v-row>
            <v-col>
              <v-file-input
                v-model="uploadedCodesysFile"
                accept=".zip"
                label="Click here to select Application Zip file"
                auto-grow
                dense
                outlined
                @change="onLoadCodesysZipFileClicked"
              />
            </v-col>
            <v-spacer />
          </v-row>
          <v-textarea
            v-model="serviceOfferingVersion.deploymentDefinition.codesysFile"
            class="full-width"
            outlined
            dense
          />
        </v-list-group>
      </v-list>
    </v-form>

    <!-- Navigation Buttons-->
    <v-card-actions>
      <v-btn
        :color="$vuetify.theme.themes.light.secondary"
        @click="onCancelButtonClicked()"
      >
        {{ $t('buttons.Back') }}
      </v-btn>
      <v-spacer />
      <v-btn
        :color="$vuetify.theme.themes.light.secondary"
        @click="onNextButtonClicked()"
      >
        {{ $t('buttons.Next') }}
      </v-btn>
    </v-card-actions>
  </div>
</template>

<script>
  import DockerContainerEnvironmentVariables
    from '@/components/service_offerings/wizard_service_offering_version/Docker/DockerEnvironmentVariables'
  import 'vue-json-pretty/lib/styles.css'
  import YAML from 'yaml'
  import ServiceRepositorySelect from '@/components/service_offerings/wizard_service_offering_version/ServiceRepositorySelect'
  const { parse } = require('dot-properties')

  export default {
    name: 'ServiceOfferingVersionWizardStep2DeploymentDefinitionCodesys',
    components: {
    },
    props: ['editMode', 'serviceOfferingVersion', 'serviceVendorId'],

    data () {
      return {
        stepNumber: 2,
        validForm: false,
        uploadedCodesysFile: null,
      }
    },
    mounted() {
      if (this.editMode) {

      }
    },
    methods: {
      onCancelButtonClicked () {
        this.$emit('step-canceled', this.stepNumber)
      },
      onNextButtonClicked () {

        this.$emit('step-completed', this.stepNumber)
      },
      onLoadCodesysZipFileClicked (files) {
        if (!this.uploadedCodesysFile) {
          this.serviceOfferingVersion.deploymentDefinition.codesysFile = 'No file chosen'
        } else {
          const reader = new FileReader()
          reader.readAsArrayBuffer(this.uploadedCodesysFile)
          reader.onload = () => {
            this.serviceOfferingVersion.deploymentDefinition.codesysFile = this.uploadedCodesysFile
          }
        }
      },
    },

  }
</script>
