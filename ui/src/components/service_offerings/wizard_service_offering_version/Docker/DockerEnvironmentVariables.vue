<template>
  <div>
    <v-row>
      <v-col cols="6">
        <v-list-subheader>
          {{ subheader }}
        </v-list-subheader>
      </v-col>

      <v-col cols="9">
        <v-btn
          v-if="addable"
          color="secondary"
          class="noTextTransform"
          @click="addEnvironmentVariable"
        >
          <v-icon
            density="compact"
            size="small"
            class="mr-2"
          >
            mdi-plus-circle
          </v-icon>
          Environment Variable
        </v-btn>
      </v-col>
    </v-row>

    <v-data-table
      v-if="environmentVariables.length > 0"
      :headers="tableHeaders"
      :items="environmentVariables"
      :hide-default-footer="environmentVariables.length < 5"
      class="elevation-1"
      disable-sort
      :footer-props="{
        'items-per-page-options': [5, 10, 20, -1],
      }"
    >
      <template #item.key="{ item }">
        <ValidationProvider
          v-slot="{ errors }"
          name="Key"
          rules="required|alpha_dash"
        >
          <v-text-field
            v-if="editable"
            v-model="item.key"
            placeholder="Key of environment variable"
            :error-messages="errors"

          />
          <div
            v-else
          >
            {{ item.key }} ({{ item.serviceName }})
          </div>
        </ValidationProvider>
      </template>

      <template #item.value="{ item }">
        <ValidationProvider
          v-slot="{ errors }"
          name="Value"
          rules="required"
        >
          <v-text-field
            v-if="editable"
            v-model="item.value"
            placeholder="Value of environment variable"
            :error-messages="errors"

          />
          <div
            v-else
          >
            {{ item.value }}
          </div>
        </ValidationProvider>
      </template>

      <template #item.isServiceOption="{ item }">
        <v-checkbox
          v-model="item.isServiceOption"
        />
      </template>

      <template #item.actions="{ item }">
        <v-btn
          color="error"
          :disabled="!editable"
          @click="removeEnvironmentVariable(item)"
        >
          <v-icon>
            mdi-delete
          </v-icon>
        </v-btn>
      </template>
    </v-data-table>
  </div>
</template>

<script>
  export default {
    name: 'DockerContainerConfigEnvironment',
    props: {
      environmentVariables: {},
      editable: {},
      addable: {},
      subheader: {
        default: "Environment Variables",
        type: String
        },
      },

    data () {
      return {
        tableHeaders: [
          { text: 'Key', value: 'key' },
          { text: 'Value', value: 'value' },
          { text: 'Service Option', value: 'isServiceOption' },
          { text: 'Actions', value: 'actions' },
        ],
      }
    },

    methods: {
      addEnvironmentVariable () {
        this.environmentVariables.push({
          key: '',
          value: '',
          isServiceOption: false,
        })
      },
      removeEnvironmentVariable (item) {
        const index = this.environmentVariables.indexOf(item)
        this.environmentVariables.splice(index, 1)
      },
    },
  }
</script>
