<template>
  <v-row>
    <v-col cols="3">
      <v-list-subheader>
        Restart Policy
      </v-list-subheader>
    </v-col>

    <v-col cols="9">
      <Field
        v-slot="{ field, errors }"
        v-model="newServiceOffering.deploymentDefinition.restartPolicy"
        name="Restart Policy"
        :rules="required"
      >
        <v-select
          v-bind="field"
          class="full-width"
          hide-details
          density="compact"
          flat
          :items="restartPoliciesList"
          item-title="prettyName"
          item-value="value"
          :error-messages="errors"
          :model-value="newServiceOffering.deploymentDefinition.restartPolicy"
        />
      </Field>
    </v-col>
  </v-row>
</template>

<script>
import {Field} from "vee-validate";
import * as yup from 'yup';

export default {
    name: 'DockerContainerRestartPolicy',
    components: {Field},
    props: ['newServiceOffering'],
    setup(){
      const required = yup.string().required()
      return {
        required
      }
    },
    data () {
      return {
        restartPoliciesList: [
          { value: 'ALWAYS', prettyName: 'Always' },
          { value: 'UNLESS_STOPPED', prettyName: 'Unless stopped' },
          { value: 'ON_FAILURE', prettyName: 'On failure' },
          { value: 'NONE', prettyName: 'None' }
        ],
      }
    },
  }
</script>
