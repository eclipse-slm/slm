<template>
  <div>
    <v-form
      v-model="validForm"
    >
      <v-list :opened="['Codesys Application Files']">
        <!-- Codesys File !-->
        <v-list-group value="Codesys Application Files">
          <template #activator="{props}">
            <v-list-item
              v-bind="props"
              title="Codesys Application Files"
            />
          </template>
          <v-row>
            <v-col>
              <v-file-input
                v-model="uploadedCodesysFile"
                accept=".zip"
                label="Click here to select Application Zip file"
                auto-grow
                density="compact"
                variant="outlined"
                @update:modelValue="onLoadCodesysApplicationFileClicked($event)"
              />
            </v-col>
            <v-spacer />
          </v-row>
          <v-row>
            <v-col>
              <v-btn
                :disabled="!isApplicationEmpty"
                :color="$vuetify.theme.themes.light.primary"
                @click="onDownloadCodesysApplicationClick()"
              >
                application.zip
              </v-btn>
            </v-col>
          </v-row>
        </v-list-group>
      </v-list>
    </v-form>

    <!-- Navigation Buttons-->
    <v-card-actions>
      <v-btn
        variant="elevated"
        :color="$vuetify.theme.themes.light.colors.secondary"
        @click="onCancelButtonClicked()"
      >
        {{ $t('buttons.Back') }}
      </v-btn>
      <v-spacer />
      <v-btn
        variant="elevated"
        :color="$vuetify.theme.themes.light.colors.secondary"
        @click="onNextButtonClicked()"
      >
        {{ $t('buttons.Next') }}
      </v-btn>
    </v-card-actions>
  </div>
</template>

<script>
import 'vue-json-pretty/lib/styles.css'
import ServiceOfferingVersionsRestApi from "@/api/service-management/serviceOfferingVersionsRestApi";

  export default {
    name: 'ServiceOfferingVersionWizardStep2DeploymentDefinitionCodesys',
    components: {
    },
    props: {
      editMode: {
        type: Boolean,
        default: false
      },
      serviceOfferingVersion: {
        type: Object,
        default: null
      },
      serviceVendorId: {
        type: String,
        default: null
      },
    },

    data () {
      return {
        stepNumber: 2,
        validForm: false,
        uploadedCodesysFile: null,
      }
    },
    computed: {
      isApplicationEmpty(){
        return this.serviceOfferingVersion.deploymentDefinition.applicationPath && this.serviceOfferingVersion.deploymentDefinition.applicationPath.length > 0;
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
      onLoadCodesysApplicationFileClicked (files) {
        if (!files) {
          this.serviceOfferingVersion.deploymentDefinition.codesysFile = 'No file chosen'
        } else {
          this.serviceOfferingVersion.deploymentDefinition.codesysFile = files;
          this.uploadedCodesysFile = files;
        }
      },
      onDownloadCodesysApplicationClick(){
        ServiceOfferingVersionsRestApi.downloadFileForServiceOfferingVersion(this.serviceOfferingVersion.serviceOfferingId, this.serviceOfferingVersion.id, 'application.zip').then(response => {
          const url = window.URL.createObjectURL(new Blob([response.data]));
          const link = document.createElement('a');
          link.href = url;
          link.setAttribute('download', 'application.zip');
          document.body.appendChild(link);
          link.click();
        });
      }
    },

  }
</script>
