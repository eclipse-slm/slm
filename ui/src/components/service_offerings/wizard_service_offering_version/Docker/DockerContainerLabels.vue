<template>
  <div>
    <v-row>
      <v-col cols="3">
        <v-list-subheader>
          Labels
        </v-list-subheader>
      </v-col>

      <v-col cols="9">
        <v-btn
          class="noTextTransform"
          color="secondary"
          @click="addLabel"
        >
          <v-icon
            density="compact"
            size="small"
            class="mr-2"
          >
            mdi-plus-circle
          </v-icon>
          Label
        </v-btn>
      </v-col>
    </v-row>

    <v-row>
      <v-col cols="12">
        <v-data-table
          v-if="labels.length > 0"
          :headers="tableHeaders"
          :items="labels"
          :hide-default-footer="labels.length < 5"
          class="elevation-1"
          disable-sort
          :footer-props="{
            'items-per-page-options': [5, 10, 20, -1],
          }"
        >
          <template #item.name="{ item }">
            <Field
              v-slot="{ field, errors }"
              v-model="item.name"
              name="Label"
              :rules="required"
            >
              <v-text-field
                v-if="editable"
                v-bind="field"
                placeholder="Name of label"
                :error-messages="errors"

              />
              <div
                v-else
              >
                {{ item.key }}
              </div>
            </Field>
          </template>

          <template #item.value="{ item }">
            <Field
              v-slot="{ field, errors }"
              v-model="item.value"
              name="Value"
              :rules="required_string"
            >
              <v-text-field
                v-if="editable"
                v-bind="field"
                placeholder="Value of label"
                :error-messages="errors"

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
              @click="removeLabel(item)"
            >
              <v-icon>
                mdi-delete
              </v-icon>
            </v-btn>
          </template>
        </v-data-table>
      </v-col>
    </v-row>
  </div>
</template>

<script>
import {Field} from "vee-validate";
import * as yup from "yup";



  export default {
    name: 'DockerContainerLabels',
    components: {Field},
    props: ['labels', 'editable'],
    setup(){
      const required = yup.number().required()
      const required_string = yup.string().required()
      return {
        required, required_string
      }
    },
    data () {
      return {
        tableHeaders: [
          { text: 'Label', value: 'name' },
          { text: 'Value', value: 'value' },
          { text: 'Service Option', value: 'isServiceOption' },
          { text: 'Actions', value: 'actions' },
        ],
      }
    },
    methods: {
      addLabel () {
        this.labels.push({
          name: '',
          value: '',
          isServiceOption: false,
        })
      },

      removeLabel (item) {
        const index = this.labels.indexOf(item)
        this.labels.splice(index, 1)
      },
    },
  }
</script>
