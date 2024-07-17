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
        <Field
          v-slot="{ field, errors }"
          v-model="item.key"
          name="Key"
          :rules="required_alpha_dash"
        >
          <v-text-field
            v-if="editable"
            v-bind="field"
            placeholder="Key of environment variable"
            :error-messages="errors"
            :model-value="item.key"
          />
          <div
            v-else
          >
            {{ item.key }} ({{ item.serviceName }})
          </div>
        </Field>
      </template>

      <template #item.value="{ item }">
        <Field
          v-slot="{ field, errors }"
          v-model="item.value"
          name="Value"
          :rules="required"
        >
          <v-text-field
            v-if="editable"
            v-bind="field"
            placeholder="Value of environment variable"
            :error-messages="errors"
            :model-value="item.value"
          />
          <div
            v-else
          >
            {{ item.value }}
          </div>
        </Field>
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
import {Field } from "vee-validate";
import * as yup from 'yup';

  export default {
    name: 'DockerContainerConfigEnvironment',
    components: {Field},
    props: {
      environmentVariables: {},
      editable: {},
      addable: {},
      subheader: {
        default: "Environment Variables",
        type: String
        },
      },
    setup(){
      const required_alpha_dash = yup.string().required().matches(new RegExp(".[a-zA-Z0-9_\/]"))
      const required = yup.string().required()
      return {
        required,required_alpha_dash
      }
    },

    data () {
      return {
        tableHeaders: [
          { title: 'Key', value: 'key' },
          { title: 'Value', value: 'value' },
          { title: 'Service Option', value: 'isServiceOption' },
          { title: 'Actions', value: 'actions' },
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
