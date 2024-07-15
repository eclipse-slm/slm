<template>
  <div>
    <ValidationForm
      ref="observer"
      v-slot="{ meta, handleSubmit, validate }"
    >
      <v-row>
        <v-col cols="8">
          <v-card
            class="mx-1 my-1 pt-5"
          >
            <Field
              v-slot="{ field, errors }"
              v-model="serviceOfferingVersion.version"
              name="Version"
              :rules="required"
            >
              <v-text-field
                v-bind="field"
                label="Version"
                variant="outlined"
                density="compact"
                :error-messages="errors"
                :model-value="serviceOfferingVersion.version"
              />
            </Field>


            <v-tooltip
              location="bottom"
              :disabled="!editMode"
            >
              <template #activator="{ props }">
                <div
                  v-bind="props"
                >
                  <Field
                    v-slot="{ field, errors }"
                    v-model="serviceOfferingVersion.deploymentDefinition.deploymentType"
                    name="Service Deployment"
                    :rules="required"
                  >
                    <v-select
                      v-bind="field"
                      v-model="serviceOfferingVersion.deploymentDefinition.deploymentType"
                      :items="serviceOfferingDeploymentTypes"
                      item-title="prettyName"
                      item-value="value"
                      label="Deployment Type"
                      variant="outlined"
                      density="compact"
                      :readonly="editMode"
                      :error-messages="errors"
                    />
                    <span>{{ errors[0] }}</span>
                  </Field>
                </div>
              </template>
            </v-tooltip>
          </v-card>
        </v-col>
        <v-col cols="4" />
      </v-row>

      <!-- Navigation Buttons-->
      <v-card-actions>
        <v-btn
          variant="tonal"
          :color="$vuetify.theme.themes.light.colors.secondary"
          @click="$emit('step-canceled', stepNumber)"
        >
          {{ $t('buttons.Cancel') }}
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
  </div>
</template>

<script>
import 'vue-json-pretty/lib/styles.css'

import {Field, Form as ValidationForm } from "vee-validate";
import * as yup from 'yup';
import {useServicesStore} from "@/stores/servicesStore";
const { parse } = require('dot-properties')

  export default {
    name: 'ServiceOfferingVersionWizardStep1Common',
    components: {Field, ValidationForm},
    props: ['editMode', 'serviceOfferingVersion'],
    setup(){
      const required = yup.string().required();
      const servicesStore = useServicesStore();
      return {
        required, servicesStore
      }
    },
    data () {
      return {
        stepNumber: 1,
      }
    },
    computed: {
      serviceOfferingDeploymentTypes() {
        return this.servicesStore.serviceOfferingDeploymentTypes
      },
      serviceOfferings () {
        return this.servicesStore.serviceOfferings
      },

      apiStateLoaded() {
        return true;
      },
      apiStateLoading() {
        return false;
      },
      apiStateError() {
        return false;
      },
    },
    methods: {
      async onNextButtonClicked () {

        // if in editMode aka during update, do not check version
        if (this.editMode) {
            this.$emit('step-completed', this.stepNumber)
        } else {
          // check if service version has already been used in service offering
          if(this.serviceOfferings.length > 0 && (this.serviceOfferings.find(so => so.id === this.serviceOfferingVersion.serviceOfferingId)?.versions.map(sov => sov.version)).includes(this.serviceOfferingVersion.version)){
            this.$toast.error(`Version '${this.serviceOfferingVersion.version}' has already been used for this service offering!`)
          } else {
            this.$emit('step-completed', this.stepNumber)
          }
        }
      },
    }
  }
</script>
