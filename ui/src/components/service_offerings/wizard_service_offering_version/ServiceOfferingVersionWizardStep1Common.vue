<template>
  <div>
    <ValidationForm
      ref="observer"
      v-slot="{ meta, handleSubmit, validate }"
    >
      <v-row>
        <v-col cols="8">
          <v-card
            class="mx-1 my-1"
            variant="outlined"
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
          :color="$vuetify.theme.themes.light.secondary"
          @click="$emit('step-canceled', stepNumber)"
        >
          {{ $t('buttons.Cancel') }}
        </v-btn>
        <v-spacer />
        <v-btn
          :color="!meta.valid ? $vuetify.theme.disable : $vuetify.theme.themes.light.secondary"
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
import {mapGetters} from "vuex";
import {Field, Form as ValidationForm } from "vee-validate";
import * as yup from 'yup';
const { parse } = require('dot-properties')

  export default {
    name: 'ServiceOfferingVersionWizardStep1Common',
    components: {Field, ValidationForm},
    props: ['editMode', 'serviceOfferingVersion'],
    setup(){
      const required = yup.string().required();
      return {
        required
      }
    },
    data () {
      return {
        stepNumber: 1,
      }
    },
    computed: {
      ...mapGetters(['serviceOfferingDeploymentTypes', 'serviceOfferings']),
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
