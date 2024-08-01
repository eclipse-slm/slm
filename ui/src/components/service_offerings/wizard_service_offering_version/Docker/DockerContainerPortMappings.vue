<template>
  <div>
    <v-row>
      <v-col cols="3">
        <v-list-subheader>
          Port Mappings
        </v-list-subheader>
      </v-col>

      <v-col cols="9">
        <v-btn
          class="noTextTransform"
          color="secondary"
          @click="addPortMapping"
        >
          <v-icon
            density="compact"
            size="small"
            class="mr-2"
          >
            mdi-plus-circle
          </v-icon>
          Port Mapping
        </v-btn>
      </v-col>
    </v-row>

    <v-row>
      <v-col cols="12">
        <v-data-table
          v-if="portMappings.length > 0"
          :headers="tableHeaders"
          :items="portMappings"
          class="elevation-1"
          disable-sort
          :hide-default-footer="portMappings.length < 5"
          :footer-props="{
            'items-per-page-options': [5, 10, 20, -1],
          }"
        >
          <template #item.hostPort="{ item }">
            <Field
              v-slot="{ field, errors }"
              v-model="item.hostPort"
              name="Host Port"
              :rules="required"
            >
              <v-text-field
                v-if="editable"
                v-bind="field"
                :error-messages="errors"
                :model-value="item.hostPort"
              />
              <div
                v-else
              >
                {{ item.key }}
              </div>
            </Field>
          </template>

          <template #item.containerPort="{ item }">
            <Field
              v-slot="{ field, errors }"
              v-model="item.containerPort"
              name="Container Port"
              :rules="required"
            >
              <v-text-field
                v-if="editable"
                v-bind="field"
                :error-messages="errors"
                :model-value="item.containerPort"
              />
              <div
                v-else
              >
                {{ item.value }}
              </div>
            </Field>
          </template>

          <template #item.protocol="{ item }">
            <v-btn-toggle
              mandatory="force"
              class="ml-5"
            >
              <v-btn
                :color="item.protocol === 'TCP' ? 'secondary' : 'grey'"
                @click="item.protocol = item.protocol === 'TCP' ? '' : 'TCP'"
              >
                TCP
              </v-btn>
              <v-btn
                :color="item.protocol === 'UDP' ? 'secondary' : 'grey'"
                @click="item.protocol = item.protocol === 'UDP' ? '' : 'UDP'"
              >
                UDP
              </v-btn>
            </v-btn-toggle>
          </template>

          <template #item.isServiceOption="{ item }">
            <div class="d-flex justify-center">
              <v-checkbox
                v-model="item.isServiceOption"
              />
            </div>
          </template>

          <template #item.actions="{ item }">
            <v-btn
              color="error"
              :disabled="!editable"
              @click="removePortMapping(item)"
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
import * as yup from 'yup';

export default {
    name: 'DockerContainerPortMappings',
    components: {Field},
    props: ['portMappings', 'editable'],
    setup(){
      const required = yup.number().required().min(1).max(65536).typeError("Number needs to be between 1 and 65536");
      return {
        required
      }
    },
    data () {
      return {
        tableHeaders: [
          { title: 'Host Port', value: 'hostPort', width: '35%' },
          { title: 'Container Port', value: 'containerPort', width: '35%' },
          { title: 'Protocol', value: 'protocol', width: '10%' },
          { title: 'Service Option', value: 'isServiceOption', width: '10%' },
          { title: 'Actions', value: 'actions', width: '10%' },
        ],
      }
    },
    methods: {
      addPortMapping () {
        this.portMappings.push({
          hostPort: '',
          containerPort: '',
          protocol: '',
          isServiceOption: false,
        })
      },

      removePortMapping (index) {
        this.portMappings.splice(index, 1)
      },
    },
  }
</script>
