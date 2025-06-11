<template>
  <v-container fluid>
    <ValidationForm
      ref="observer"
      v-slot="{ meta, handleSubmit, validate }"
    >
      <v-row>
        <v-col cols="8">
          <!-- Git Repository URL !-->
          <Field
            v-slot="{ field, errors }"
            v-model="serviceOfferingGitRepository.repositoryUrl"
            name="Git Repository URL"
            :rules="required"
          >
            <v-text-field
              v-bind="field"
              label="Git Repository URL"
              variant="outlined"
              required
              density="compact"
              :error-messages="errors"
              :model-value="serviceOfferingGitRepository.repositoryUrl"
            />
          </Field>

          <!-- Git Tag Regular Expression !-->
          <Field
            v-slot="{ field, errors }"
            v-model="serviceOfferingGitRepository.gitTagRegEx"
            name="Git Tag Regular Expression"
            :rules="required"
          >
            <v-text-field
              v-bind="field"
              label="Git Tag Regular Expression"
              variant="outlined"
              required
              density="compact"
              :error-messages="errors"
              :model-value="serviceOfferingGitRepository.gitTagRegEx"
            />
          </Field>

          <v-switch
            v-model="privateRepository"
            label="Private Repository"
          />

          <!-- Git Repository Username !-->
          <Field
            v-if="privateRepository"
            v-slot="{ field, errors }"
            v-model="serviceOfferingGitRepository.gitUsername"
            name="Username"
            :rules="required"
          >
            <v-text-field
              v-bind="field"
              label="Username"
              variant="outlined"
              required
              density="compact"
              :error-messages="errors"
              :model-value="serviceOfferingGitRepository.gitUsername"
            />
          </Field>

          <!-- Git Repository Password !-->
          <Field
            v-if="privateRepository"
            v-slot="{ field, errors }"
            v-model="serviceOfferingGitRepository.gitPassword"
            name="Password"
            :rules="required"
          >
            <v-text-field
              v-bind="field"
              :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
              :type="showPassword ? 'text' : 'password'"
              label="Password"
              variant="outlined"
              required
              density="compact"
              :error-messages="errors"
              :model-value="serviceOfferingGitRepository.gitPassword"

              @click:append="showPassword = !showPassword"
            />
          </Field>
        </v-col>
        <v-col cols="4">
          <!--          <service-offering-card-grid-->
          <!--            :service-offering="newServiceOffering"-->
          <!--            :passive="true"-->
          <!--            :create-or-edit-mode="true"-->
          <!--            :show-only-latest-version="true"-->
          <!--          />-->
        </v-col>
      </v-row>

      <progress-circular
        :show-as-overlay="true"
        :overlay="loading"
      />

      <!-- Navigation Buttons-->
      <v-card-actions>
        <v-btn
          variant="elevated"
          color="secondary"
          @click="$emit('step-canceled', stepNumber)"
        >
          {{ $t('buttons.Cancel') }}
        </v-btn>
        <v-spacer />
        <v-btn
          variant="elevated"
          :color="!meta.valid ? $vuetify.theme.themes.light.colors.disable : $vuetify.theme.themes.light.colors.secondary"
          @click="!meta.valid ? validate() : handleSubmit(onNextButtonClicked)"
        >
          <div v-if="editMode">
            Update
          </div>
          <div v-else>
            Create
          </div>
        </v-btn>
      </v-card-actions>
    </ValidationForm>
  </v-container>
</template>

<script>
  // import ServiceOfferingCardGrid from '@/components/service_offerings/ServiceOfferingCardGrid'

  import ProgressCircular from "@/components/base/ProgressCircular.vue";
  import {Field, Form as ValidationForm} from "vee-validate";
  import * as yup from 'yup';
  import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";
  import ServiceManagementClient from "@/api/service-management/service-management-client";

  export default {
    name: 'ServiceOfferingWizardStep1Common',
    components: {ProgressCircular, Field, ValidationForm},
    // components: { ServiceOfferingCardGrid },
    props: {
      editMode: {
        type: Boolean,
        default: false
      },
      serviceVendorId: {
        type: String,
        default: null
      }
    },
    setup(){
      const required = yup.string().required();
      const serviceOfferingsStore = useServiceOfferingsStore();

      return {
        required, serviceOfferingsStore
      }
    },
    data () {
      return {
        stepNumber: 1,
        showPassword: false,
        privateRepository: false,
        serviceOfferingGitRepository: {
          repositoryUrl: "https://",
          gitUsername: '',
          gitPassword: '',
          gitTagRegEx: '\\d+.\\d+.\\d+',
          serviceVendorId: null
        },
        loading: false,
      }
    },
    computed: {
      serviceOfferingCategories() {
        return this.serviceOfferingsStore.serviceOfferingCategories
      },
      serviceOfferingDeploymentTypes () {
        return this.serviceOfferingsStore.serviceOfferingDeploymentTypes
      },
    },
    mounted() {
      this.serviceOfferingGitRepository.serviceVendorId = this.serviceVendorId
    },
    methods: {
      async onNextButtonClicked () {
        this.loading = true
        ServiceManagementClient.serviceOfferingsApi.addServiceOfferingWithAutoGeneratedIdFromGitRepo(this.serviceOfferingGitRepository).then(
            response => {
              this.loading = false
              if (response.status === 200) {
                this.$toast.info('Successfully created git-based service offering')
                this.serviceOfferingsStore.getServiceOfferings();
                this.$router.push({ path: `/services/vendors/${this.serviceVendorId}` })
              } else {
                console.log(response)
              }
            })
            .catch(exception => {
              this.loading = false
              this.$toast.error('Failed to create git-based service offering')
              console.log('Service offering creation failed: ' + exception.response.data.message)
              console.log(exception)
            })
      },
    },
  }
</script>
