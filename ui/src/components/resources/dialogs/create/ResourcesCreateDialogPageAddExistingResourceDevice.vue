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
            v-model="remoteAccess.available"
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

        <v-row>
          <v-col cols="9">
            <Field
              v-if="remoteAccess.available"
              v-slot="{ errors, field }"
              v-model="remoteAccess.connectionType"
              name="Resource Connection"
              :rules="string_required"
            >
              <v-select
                id="resource-select-connection-type"
                v-bind="field"
                required
                label="Connection Type"
                prepend-icon="mdi-connection"
                :items="availableConnectionTypes"
                item-title="prettyName"
                item-value="name"
                :error-messages="errors"
                persistent-placeholder

                @update:modelValue="updateConnectionPort"
              />
            </Field>
          </v-col>
          <v-col cols="3">
            <Field
              v-if="remoteAccess.available"
              v-slot="{ errors, field }"
              v-model="remoteAccess.connectionPort"
              name="Connection Port"
              :rules="string_required"
            >
              <v-text-field
                v-bind="field"
                type="number"
                required
                label="Connection Port"
                prepend-icon="mdi-counter"
                persistent-placeholder
                :error-messages="errors"
              />
            </Field>
          </v-col>
        </v-row>
        <Field
          v-if="remoteAccess.available"
          v-slot="{ errors, field }"
          v-model="remoteAccess.username"
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
            :model-value="remoteAccess.username"
          />
        </Field>
        <Field
          v-if="remoteAccess.available"
          v-slot="{ errors, field }"
          v-model="remoteAccess.password"
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
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
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

      const resourceDevicesStore = useResourceDevicesStore();

      return {string_required, ip_required, resourceDevicesStore}
    },
    data () {
      return {
        resourceHostname: '',
        resourceIp: '',
        resourceLocation: '',
        remoteAccess: {
          available: false,
          connectionType: '',
          connectionPort: 0,
          username: '',
          password: ''
        },
      }
    },
    computed: {
      availableConnectionTypes() {
        return this.resourceDevicesStore.resourceConnectionTypes
      },
      remoteAccessAvailable() {
        return this.remoteAccess.available
      },
      locations () {
        return this.resourceDevicesStore.locations
      },
    },
    watch: {
      remoteAccessAvailable (newVal, oldVal) {
        if (oldVal === false && newVal === true) {
          this.remoteAccess.connectionType = this.availableConnectionTypes[0].name
          this.remoteAccess.connectionPort = this.availableConnectionTypes[0].defaultPort
        }
      }
    },
    mounted() {
      this.$emit('title-changed', 'Add existing host resource')
      this.resourceDevicesStore.getRemoteConnectionTypes();
    },
    methods: {
      updateConnectionPort(connectionTypeName) {
        let connectionType = this.availableConnectionTypes.find(ct => {
          return ct.name === connectionTypeName
        });

        if (connectionType !== undefined)
          this.remoteAccess.connectionPort = connectionType.defaultPort
      },
      clearForm () {
        this.resourceHostname = ''
        this.resourceIp = ''
        this.resourceLocation = ''
        this.remoteAccess.available = false
        this.remoteAccess.username = ''
        this.remoteAccess.password = ''
        this.remoteAccess.connectionType = ''
        this.remoteAccess.connectionPort = 0
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
        ResourceManagementClient.resourcesApi.addExistingResource({
          resourceHostname: this.resourceHostname,
          resourceIp: this.resourceIp,
          digitalNameplateV3: {}
        }).then(
          response => {
            if (response.status === 201) {
              let resourceId = response.data;

              if (this.resourceLocation !== '') {
                ResourceManagementClient.resourcesApi.setLocationOfResource(
                    resourceId, this.resourceLocation
                ).then().catch(logRequestError);
              }

              if (this.remoteAccess.available) {
                ResourceManagementClient.resourcesApi.setRemoteAccessOfResource(
                    resourceId,
                    this.remoteAccess.connectionType,
                    this.remoteAccess.username,
                    this.remoteAccess.password,
                    this.remoteAccess.connectionPort
                ).then().catch(logRequestError);
              }

              this.clearForm()
              this.$emit('confirmed')
            }
          }
        ).catch(logRequestError);
      },
    },
  }
</script>
