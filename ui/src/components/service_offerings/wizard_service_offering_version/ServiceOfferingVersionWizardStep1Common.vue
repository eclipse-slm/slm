<template>
  <div>
    <validation-observer
      ref="observer"
      v-slot="{ invalid, handleSubmit, validate }"
    >
      <v-row>
        <v-col cols="8">
          <v-card
            class="mx-1 my-1"
            variant="outlined"
          >
            <validation-provider
              v-slot="{ errors }"
              name="Version"
              rules="required"
            >
              <v-text-field
                v-model="serviceOfferingVersion.version"
                label="Version"
                variant="outlined"
                density="compact"
                :error-messages="errors"

              />
            </validation-provider>


            <v-tooltip
              location="bottom"
              :disabled="!editMode"
            >
              <template #activator="{ props }">
                <div
                  v-bind="props"
                >
                  <validation-provider
                    v-slot="{ errors }"
                    name="Service Deployment"
                    rules="required"
                  >
                    <v-select
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
                  </validation-provider>
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
          :color="invalid ? $vuetify.theme.disable : $vuetify.theme.themes.light.secondary"
          @click="invalid ? validate() : handleSubmit(onNextButtonClicked)"
        >
          {{ $t('buttons.Next') }}
        </v-btn>
      </v-card-actions>
    </validation-observer>
  </div>
</template>

<script>
import 'vue-json-pretty/lib/styles.css'
import {mapGetters} from "vuex";

const { parse } = require('dot-properties')

  export default {
    name: 'ServiceOfferingVersionWizardStep1Common',
    components: {},
    props: ['editMode', 'serviceOfferingVersion'],

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
