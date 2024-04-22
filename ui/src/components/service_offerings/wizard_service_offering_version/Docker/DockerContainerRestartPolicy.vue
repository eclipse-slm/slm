<template>
  <v-row>
    <v-col cols="3">
      <v-list-subheader>
        Restart Policy
      </v-list-subheader>
    </v-col>

    <v-col cols="9">
      <ValidationProvider
        v-slot="{ errors }"
        name="Image Tag"
        rules="required"
      >
        <v-select
          v-model="newServiceOffering.deploymentDefinition.restartPolicy"
          class="full-width"
          hide-details
          density="compact"
          flat
          :items="restartPoliciesList"
          item-title="prettyName"
          item-value="value"
          :error-messages="errors"

        />
      </ValidationProvider>
    </v-col>
  </v-row>
</template>

<script>
  export default {
    name: 'DockerContainerRestartPolicy',
    props: ['newServiceOffering'],
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
