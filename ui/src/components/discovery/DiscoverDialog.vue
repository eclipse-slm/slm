<template>
  <v-dialog
    v-model="active"
    max-width="800px"
    @click:outside="closeDialog"
  >
    <template #default="{}">
      <v-toolbar
        color="primary"
        theme="dark"
      >
        <v-row
          align="center"
          justify="center"
        >
          <v-col>
            Discover resources
          </v-col>
          <v-spacer />
          <v-col
            cols="1"
          >
            <v-btn
              @click="closeDialog"
            >
              <v-icon>mdi-close</v-icon>
            </v-btn>
          </v-col>
        </v-row>
      </v-toolbar>
      <v-card>
        <ValidationForm
          ref="observer"
          v-slot="{ meta, handleSubmit, validate }"
        >
          <v-container>
            <v-row class="my-2">
              <Field
                v-slot="{ field, errors }"
                v-model="selectedDriver"
                name="resource id"
                :rules="required"
              >
                <v-select
                  v-bind="field"
                  v-model="selectedDriver"
                  :items="drivers"
                  item-title="instanceId"
                  class="mx-8"
                  return-object
                  label="Select driver to execute scan"
                  autofocus
                >
                  <template #selection="{ item }">
                    <span>{{ item.raw.name }} (Vendor: {{ item.raw.vendorName }} | Version: {{ item.raw.version }})</span>
                  </template>
                  <template #item="{ item, props }">
                    <v-list-item
                      v-bind="props"
                      :title="item.raw.name + ' (Vendor: ' + item.raw.vendorName + ' | Version: ' + item.raw.version + ')'"
                    />
                  </template>
                </v-select>
                <span>{{ errors[0] }}</span>
              </Field>
            </v-row>

            <div
              v-if="selectedDriver"
            >
              <v-row
                v-if="selectedDriver.discoveryRequestFilters.length > 0"
                class="mx-6 my-2"
              >
                Filters
              </v-row>
              <v-row
                v-for="filter in selectedDriver.discoveryRequestFilters"
                :key="filter.key"
              >
                <v-text-field
                  v-model="filterValues[filter.key]"
                  class="mx-8"
                  :label="filter.key"
                  clearable
                />
              </v-row>

              <v-row
                v-if="selectedDriver.discoveryRequestOptions.length > 0"
                class="mx-6 my-2"
              >
                Options
              </v-row>
              <v-row
                v-for="option in selectedDriver.discoveryRequestOptions"
                :key="option.key"
              >
                <v-text-field
                  v-model="optionValues[option.key]"
                  class="mx-8"
                  :label="option.key"
                  clearable
                />
              </v-row>
            </div>
          </v-container>
          <v-card-actions>
            <v-row class="mx-4">
              <v-btn
                @click="closeDialog"
              >
                Cancel
              </v-btn>
              <v-spacer />
              <v-btn
                justify="end"
                variant="text"
                :color="!meta.valid ? $vuetify.theme.themes.light.colors.disable : $vuetify.theme.themes.light.colors.secondary"
                @click="!meta.valid ? validate() : handleSubmit(onScanButtonClicked)"
              >
                Scan
              </v-btn>
            </v-row>
          </v-card-actions>
        </ValidationForm>
      </v-card>
    </template>
  </v-dialog>
</template>

<script>
import {toRef} from "vue";
import {Field, Form as ValidationForm} from "vee-validate";
import * as yup from "yup";

export default {
    name: 'DiscoverDialog',
    components: {
      Field,
      ValidationForm
    },
    props: {
      show: {
        type: Boolean,
        default: false
      },
      drivers: {
        type: Array,
        default: () => []
      }

    },
    setup(props){
      const active = toRef(props, 'show')
      const required = yup.object().shape({
          instanceId: yup.string().required("Is required")
      })
      return{
        active, required
      }
    },
    data () {
      return {
        dialog: this.active,
        selectedDriver: undefined,
        filterValues: {},
        optionValues: {}
      }
    },
  computed: {
    yup() {
      return yup
    }
  },
    methods: {
      closeDialog () {
        this.$emit('canceled')
      },
      onScanButtonClicked () {
        this.$emit('completed', this.selectedDriver, this.filterValues, this.optionValues)
        this.selectedDriver = undefined
      }
    }
  }
</script>
