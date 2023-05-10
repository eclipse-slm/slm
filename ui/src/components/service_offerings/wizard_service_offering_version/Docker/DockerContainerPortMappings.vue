<template>
  <div>
    <v-row>
      <v-col cols="3">
        <v-subheader>
          Port Mappings
        </v-subheader>
      </v-col>

      <v-col cols="9">
        <v-btn
          class="noTextTransform"
          color="secondary"
          @click="addPortMapping"
        >
          <v-icon
            dense
            small
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
            <ValidationProvider
              v-slot="{ errors, valid }"
              name="Container Port"
              rules="required|numeric|min_value:1|max_value:65536"
            >
              <v-text-field
                v-if="editable"
                v-model="item.hostPort"
                :error-messages="errors"
                :success="valid"
              />
              <div
                v-else
              >
                {{ item.key }}
              </div>
            </ValidationProvider>
          </template>

          <template #item.containerPort="{ item }">
            <ValidationProvider
              v-slot="{ errors, valid }"
              name="Container Port"
              rules="required|numeric|min_value:1|max_value:65536"
            >
              <v-text-field
                v-if="editable"
                v-model="item.containerPort"
                :error-messages="errors"
                :success="valid"
              />
              <div
                v-else
              >
                {{ item.value }}
              </div>
            </ValidationProvider>
          </template>

          <template #item.protocol="{ item }">
            <v-btn-toggle
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
  export default {
    name: 'DockerContainerPortMappings',
    props: ['portMappings', 'editable'],
    data () {
      return {
        tableHeaders: [
          { text: 'Host Port', value: 'hostPort', width: '35%' },
          { text: 'Container Port', value: 'containerPort', width: '35%' },
          { text: 'Protocol', value: 'protocol', width: '10%' },
          { text: 'Service Option', value: 'isServiceOption', width: '10%' },
          { text: 'Actions', value: 'actions', width: '10%' },
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
