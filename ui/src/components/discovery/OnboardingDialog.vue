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
            Onboarding
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
            Do you want to onboard the following resources?
            <div
              v-for="(discoveredResourceId, index) in discoveredResourcesIds"
              :key="index"
            >
              {{ discoveredResourceId }}
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
                @click="!meta.valid ? validate() : handleSubmit(onAddButtonClicked)"
              >
                Add
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
import {Form as ValidationForm} from "vee-validate";
import * as yup from "yup";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";

export default {
    name: 'DiscoverDialog',
    components: {
      ValidationForm
    },
    props: {
      show: {
        type: Boolean,
        default: false
      },
      discoveredResourcesIds: {
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
        selectedDriver: undefined
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
      onAddButtonClicked () {
        ResourceManagementClient.discoveryApi
            .onboardDiscoveredResources({ 'resultIds': this.discoveredResourcesIds })
            .then(() => {
          this.$emit('completed')
        })
      }
    }
  }
</script>
