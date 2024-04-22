<template>
  <v-container fluid>
    <validation-observer
      ref="observer"
      v-slot="{ invalid, handleSubmit, validate }"
    >
      <v-row>
        <v-col cols="8">
          <!-- Git Repository URL !-->
          <validation-provider
            v-slot="{ errors }"
            name="Git Repository URL"
            rules="required"
          >
            <v-text-field
              v-model="serviceOfferingGitRepository.repositoryUrl"
              label="Git Repository URL"
              variant="outlined"
              required
              density="compact"
              :error-messages="errors"

            />
          </validation-provider>

          <!-- Git Tag Regular Expression !-->
          <validation-provider
            v-slot="{ errors }"
            name="Git Tag Regular Expression"
            rules="required"
          >
            <v-text-field
              v-model="serviceOfferingGitRepository.gitTagRegEx"
              label="Git Tag Regular Expression"
              variant="outlined"
              required
              density="compact"
              :error-messages="errors"

            />
          </validation-provider>

          <v-switch
            v-model="privateRepository"
            label="Private Repository"
          />

          <!-- Git Repository Username !-->
          <validation-provider
            v-if="privateRepository"
            v-slot="{ errors }"
            name="Username"
            rules="required"
          >
            <v-text-field
              v-model="serviceOfferingGitRepository.gitUsername"
              label="Username"
              variant="outlined"
              required
              density="compact"
              :error-messages="errors"

            />
          </validation-provider>

          <!-- Git Repository Password !-->
          <validation-provider
            v-if="privateRepository"
            v-slot="{ errors }"
            name="Password"
            rules="required"
          >
            <v-text-field
              v-model="serviceOfferingGitRepository.gitPassword"
              :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
              :type="showPassword ? 'text' : 'password'"
              label="Password"
              variant="outlined"
              required
              density="compact"
              :error-messages="errors"

              @click:append="showPassword = !showPassword"
            />
          </validation-provider>
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
          :color="$vuetify.theme.themes.light.secondary"
          @click="$emit('step-canceled', stepNumber)"
        >
          {{ $t('buttons.Cancel') }}
        </v-btn>
        <v-spacer />
        <v-btn
          :color="invalid ? $vuetify.theme.disable : $vuetify.theme.themes.light.secondary"
          @click="invalid ? validate() : handleSubmit(onNextButtonClicked)"
        >
          <div v-if="editMode">
            Update
          </div>
          <div v-else>
            Create
          </div>
        </v-btn>
      </v-card-actions>
    </validation-observer>
  </v-container>
</template>

<script>
  // import ServiceOfferingCardGrid from '@/components/service_offerings/ServiceOfferingCardGrid'
  import { mapGetters } from 'vuex'
  import ServiceOfferingsRestApi from "@/api/service-management/serviceOfferingsRestApi.ts";
  import Vue from "vue";
  import ProgressCircular from "@/components/base/ProgressCircular";

  export default {
    name: 'ServiceOfferingWizardStep1Common',
    components: {ProgressCircular},
    // components: { ServiceOfferingCardGrid },
    props: ['editMode', 'serviceVendorId'],
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
    mounted() {
      this.serviceOfferingGitRepository.serviceVendorId = this.serviceVendorId
    },
    computed: {
      ...mapGetters([
        'serviceOfferingCategories',
        'serviceOfferingDeploymentTypes',
      ]),
    },
    methods: {
      async onNextButtonClicked () {
        this.loading = true
        ServiceOfferingsRestApi.addServiceOfferingViaGit(this.serviceOfferingGitRepository).then(
            response => {
              this.loading = false
              if (response.status === 200) {
                Vue.$toast.info('Successfully created git-based service offering')
                this.$store.dispatch('getServiceOfferings')
                this.$router.push({ path: `/services/vendors/${this.serviceVendorId}` })
              } else {
                console.log(response)
              }
            })
            .catch(exception => {
              this.loading = false
              Vue.$toast.error('Failed to create git-based service offering')
              console.log('Service offering creation failed: ' + exception.response.data.message)
              console.log(exception)
            })
      },
    },
  }
</script>
