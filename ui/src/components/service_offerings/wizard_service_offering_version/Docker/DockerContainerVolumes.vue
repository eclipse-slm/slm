<template>
  <div>
    <v-row>
      <v-col cols="3">
        <v-list-subheader>
          Volumes
        </v-list-subheader>
      </v-col>

      <v-col cols="9">
        <v-btn
          class="noTextTransform"
          color="secondary"
          @click="addVolume"
        >
          <v-icon
            density="compact"
            size="small"
            class="mr-2"
          >
            mdi-plus-circle
          </v-icon>
          Volume
        </v-btn>
      </v-col>
    </v-row>

    <v-row>
      <v-col cols="12">
        <v-data-table
          v-if="volumes.length > 0"
          :headers="tableHeaders"
          :items="volumes"
          class="elevation-1"
          :hide-default-footer="volumes.length < 5"
          disable-sort
          :footer-props="{
            'items-per-page-options': [5, 10, 20, -1],
          }"
        >
          <template #item.name="{ item }">
            <Field
              v-slot="{ field, errors }"
              v-model="item.name"
              name="Volume Name"
              :rules="required_alpha_dash"
            >
              <v-text-field
                v-if="editable"
                v-bind="field"
                placeholder="Name of volume"
                :error-messages="errors"
                :model-value="item.name"

              />
              <div
                v-else
              >
                {{ item.key }}
              </div>
            </Field>
          </template>

          <template #item.containerPath="{ item }">
            <Field
              v-slot="{ field, errors }"
              v-model="item.containerPath"
              name="Container Path"
              :rules="required"
            >
              <v-text-field
                v-if="editable"
                v-bind="field"
                placeholder="e.g. /path/in/container"
                :error-messages="errors"
                :model-value="item.containerPath"
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
              @click="removeVolume(item)"
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
import {Field } from "vee-validate";
import * as yup from 'yup';

  export default {
    name: 'DockerContainerVolumeMappings',
    components: {Field},
    props: ['volumes', 'editable'],
    setup(){
      const required_alpha_dash = yup.string().required().matches(new RegExp("/.[a-zA-Z0-9_\/]/"))
      const required = yup.string().required()
      return {
        required,required_alpha_dash
      }
    },
    data () {
      return {
        tableHeaders: [
          { text: 'Volume Name', value: 'name' },
          { text: 'Container Path', value: 'containerPath' },
          { text: 'Service Option', value: 'isServiceOption' },
          { text: 'Actions', value: 'actions' },
        ],
      }
    },
    methods: {
      addVolume () {
        this.volumes.push({
          name: '',
          containerPath: '',
        })
      },

      removeVolume (item) {
        const index = this.volumes.indexOf(item)
        this.volumes.splice(index, 1)
      },
    },
  }
</script>
