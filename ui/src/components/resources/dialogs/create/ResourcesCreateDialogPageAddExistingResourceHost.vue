<template>
  <ValidationForm
    ref="observer"
    v-slot="{ meta, handleSubmit, validate }"
  >
    <v-card>
      <v-container class="pa-8">
        <v-row>
          <v-tooltip
            location="bottom"
          >
            <template #activator="{ props }">
              <v-icon
                class="mx-3"
                color="primary"
                theme="dark"
                v-bind="props"
              >
                mdi-information
              </v-icon>
            </template>
            <span>Sudo privileges must be configured for the SSH user</span>
          </v-tooltip>
          <v-switch
            id="resource-create-switch-ssh-available"
            v-model="resourceAccessAvailable"
            label="Remote access to resource available?"
          />
        </v-row>
        <Field 
          v-slot="{ field, errors }"
          v-model="resourceHostname"
          name="Hostname"
          :rules="string_required"
        >
          <v-text-field
            id="resource-create-text-field-hostname"
            v-bind="field"
            label="Hostname"
            required
            prepend-icon="mdi-dns"
            :error-messages="errors"
            :model-value="resourceHostname"
          />
        </Field>
        <Field
          v-slot="{ field, errors }"
          v-model="resourceIp"
          name="IP"
          :rules="ip_required"
        >
          <v-text-field
            id="resource-create-text-field-ip"
            v-bind="field"
            label="IP"
            required
            prepend-icon="mdi-ip"
            :error-messages="errors"
            :model-value="resourceIp"
          />
        </Field>
        <v-select
          v-if="locations.length>0"
          v-model="resourceLocation"
          label="Resource Location"
          prepend-icon="mdi-map-marker"
          :items="locations"
          item-title="name"
          item-value="id"
          clearable
        />
        <v-tooltip
          v-if="showBaseConfigurationSwitch()"
          location="right"
        >
          <template #activator="{ props }">
            <div
              style="width:min-content;border: 1px solid white"
              v-bind="props"
            >
              <v-switch
                id="resource-base-configuration-switch"
                v-model="addBaseConfigurationToResource"
                label=""
                prepend-icon="mdi-factory"
              />
            </div>
          </template>
          <span>Convert Target Host to FabOS Device</span>
        </v-tooltip>

        <v-row>
          <v-col cols="9">
            <Field
              v-if="resourceAccessAvailable"
              v-slot="{ errors, field }"
              v-model="resourceConnectionType"
              name="Resource Connection"
              :rules="string_required"
            >
              <v-select
                id="resource-select-connection-type"
                v-bind="field"
                required
                label="Connection Type"
                prepend-icon="mdi-connection"
                :items="resourceConnectionTypes"
                item-title="prettyName"
                item-value="name"
                :error-messages="errors"

                @update:modelValue="updateConnectionPort"
              />
            </Field>
          </v-col>
          <v-col cols="3">
            <Field
              v-if="resourceAccessAvailable"
              v-slot="{ errors, field }"
              v-model="resourceConnectionPort"
              name="Connection Port"
              :rules="string_required"
            >
              <v-text-field
                v-bind="field"
                type="number"
                required
                label="Connection Port"
                prepend-icon="mdi-counter"
                :error-messages="errors"
              />
            </Field>
          </v-col>
        </v-row>
        <Field
          v-if="resourceAccessAvailable"
          v-slot="{ errors, field }"
          v-model="resourceUsername"
          name="Username"
          :rules="string_required"
        >
          <v-text-field
            id="resource-create-text-field-username"
            v-bind="field"
            autocomplete="username"
            label="Username"
            required
            prepend-icon="mdi-account"
            :error-messages="errors"
            :model-value="resourceUsername"
          />
        </Field>
        <Field
          v-if="resourceAccessAvailable"
          v-slot="{ errors, field }"
          v-model="resourcePassword"
          name="Password"
          :rules="string_required"
        >
          <v-text-field
            id="resource-create-text-field-password"
            v-bind="field"
            autocomplete="current-password"
            label="Password"
            type="password"
            required
            prepend-icon="mdi-lock"
            :error-messages="errors"
          />
        </Field>
      </v-container>

      <v-card-actions>
        <v-btn
          variant="text"
          @click="onBackButtonClicked"
        >
          Back
        </v-btn>
        <v-spacer />
        <v-btn
          variant="text"
          @click="onCancelButtonClicked"
        >
          Cancel
        </v-btn>
        <v-btn
          id="resource-create-button-add"
          variant="text"
          :color="!meta.valid ? $vuetify.theme.themes.light.colors.disable : $vuetify.theme.themes.light.colors.secondary"
          @click="!meta.valid ? validate() : handleSubmit(onAddButtonClicked)"
        >
          Add
        </v-btn>
      </v-card-actions>
    </v-card>
  </ValidationForm>
</template>

<script>

import ResourcesCreateDialogPage from "@/components/resources/dialogs/create/ResourcesCreateDialogPage";

import {Field, Form as ValidationForm} from "vee-validate";
import * as yup from 'yup';
import {useResourcesStore} from "@/stores/resourcesStore";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";


export default {
    name: 'ResourcesCreateDialogPageAddExistingResourceHost',
    components: {Field, ValidationForm },
    enums: {
      ResourcesCreateDialogPage,
    },
    setup(){
      const string_required = yup.string().required();
      const ip_required = yup.string().ipv4();

      const resourceStore = useResourcesStore();

      return {string_required, ip_required, resourceStore}
    },
    data () {
      return {
        resourceAccessAvailable: false,
        resourceHostname: '',
        resourceIp: '',
        resourceLocation: '',
        resourceConnectionType: '',
        resourceConnectionPort: 0,
        resourceUsername: '',
        resourcePassword: '',
        addBaseConfigurationToResource: false
      }
    },
    computed: {
      resourceConnectionTypes() {
        return this.resourceStore.resourceConnectionTypes
      },
      locations () {
        return this.resourceStore.locations
      },
      availableBaseConfigurationCapabilities() {
        return this.resourceStore.availableBaseConfigurationCapabilities
      },

      resourceBaseConfigurationId() {
        if(this.availableBaseConfigurationCapabilities.length === 0 || !this.addBaseConfigurationToResource)
          return ''
        return this.availableBaseConfigurationCapabilities[0].id
      }
    },
    mounted() {
      this.$emit('title-changed', 'Add existing host resource')
      this.resourceStore.getResourceConnectionTypes();
    },
    methods: {
      updateConnectionPort(connectionTypeName) {
        let connectionType = this.resourceConnectionTypes.find(ct => {
          return ct.name === connectionTypeName
        });

        if(connectionType !== undefined)
          this.resourceConnectionPort = connectionType.defaultPort
      },
      clearForm () {
        this.resourceAccessAvailable = false
        this.resourceHostname = ''
        this.resourceIp = ''
        this.resourceLocation = ''
        this.resourceUsername = ''
        this.resourcePassword = ''
        this.resourceConnectionType = ''
        this.resourceConnectionPort = 0
        this.addBaseConfigurationToResource = false
      },
      showBaseConfigurationSwitch() {
        return this.availableBaseConfigurationCapabilities.length > 0 && this.resourceAccessAvailable
      },
      onBackButtonClicked () {
        this.clearForm()
        this.$emit('page-changed', ResourcesCreateDialogPage.START)
      },
      onCancelButtonClicked () {
        this.clearForm()
        this.$emit('canceled')
      },
      onAddButtonClicked () {
        ResourceManagementClient.resourcesApi.addExistingResource(
          this.resourceHostname,
          this.resourceIp,
          this.resourceConnectionType,
          this.resourceUsername,
          this.resourcePassword,
          this.resourceConnectionPort,
          this.resourceLocation,
          this.resourceBaseConfigurationId
        ).then().catch(logRequestError);

        this.clearForm()
        this.$emit('confirmed')
      },
      ipValueChanged (value) {
      },

    },
  }
</script>
