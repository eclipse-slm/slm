<template>
  <validation-observer
    ref="observer"
    v-slot="{ invalid, handleSubmit, validate }"
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
        <validation-provider
          v-slot="{ errors }"
          name="Hostname"
          rules="required"
        >
          <v-text-field
            id="resource-create-text-field-hostname"
            v-model="resourceHostname"
            label="Hostname"
            required
            prepend-icon="mdi-dns"
            :error-messages="errors"

          />
        </validation-provider>
        <validation-provider
          v-slot="{ errors }"
          name="IP"
          :rules="{ required: true, regex: '^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$' }"
        >
          <v-text-field
            id="resource-create-text-field-ip"
            v-model="resourceIp"
            label="IP"
            required
            prepend-icon="mdi-ip"
            :error-messages="errors"

          />
        </validation-provider>
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
              style="width:min-content;border: 1px solid bg-white"
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
            <validation-provider
              v-if="resourceAccessAvailable"
              v-slot="{ errors }"
              name="Resource Connection"
              rules="required"
            >
              <v-select
                id="resource-select-connection-type"
                v-model="resourceConnectionType"
                required
                label="Connection Type"
                prepend-icon="mdi-connection"
                :items="resourceConnectionTypes"
                item-title="prettyName"
                item-value="name"
                :error-messages="errors"

                @update:modelValue="updateConnectionPort"
              />
            </validation-provider>
          </v-col>
          <v-col cols="3">
            <validation-provider
              v-if="resourceAccessAvailable"
              v-slot="{ errors }"
              name="Connection Port"
              rules="required"
            >
              <v-text-field
                v-model="resourceConnectionPort"
                type="number"
                required
                label="Connection Port"
                prepend-icon="mdi-counter"
                :error-messages="errors"

              />
            </validation-provider>
          </v-col>
        </v-row>
        <validation-provider
          v-if="resourceAccessAvailable"
          v-slot="{ errors }"
          name="Username"
          rules="required"
        >
          <v-text-field
            id="resource-create-text-field-username"
            v-model="resourceUsername"
            autocomplete="username"
            label="Username"
            required
            prepend-icon="mdi-account"
            :error-messages="errors"

          />
        </validation-provider>
        <validation-provider
          v-if="resourceAccessAvailable"
          v-slot="{ errors }"
          name="Password"
          rules="required"
        >
          <v-text-field
            id="resource-create-text-field-password"
            v-model="resourcePassword"
            autocomplete="current-password"
            label="Password"
            type="password"
            required
            prepend-icon="mdi-lock"
            :error-messages="errors"

          />
        </validation-provider>
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
          :color="invalid ? $vuetify.theme.disable : $vuetify.theme.themes.light.secondary"
          @click="invalid ? validate() : handleSubmit(onAddButtonClicked)"
        >
          Add
        </v-btn>
      </v-card-actions>
    </v-card>
  </validation-observer>
</template>

<script>

import resourcesRestApi from '@/api/resource-management/resourcesRestApi'
  import ResourcesCreateDialogPage from "@/components/resources/dialogs/create/ResourcesCreateDialogPage";
  import NotificationServiceWebsocketClient from "@/api/notification-service/notificationServiceWebsocketClient";
  import {mapGetters} from "vuex";

  export default {
    name: 'ResourcesCreateDialogPageAddExistingResourceHost',
    enums: {
      ResourcesCreateDialogPage,
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
      ...mapGetters(['resourceConnectionTypes','locations','availableBaseConfigurationCapabilities']),
      resourceBaseConfigurationId() {
        if(this.availableBaseConfigurationCapabilities.length == 0 || !this.addBaseConfigurationToResource)
          return ''
        return this.availableBaseConfigurationCapabilities[0].id
      }
    },
    mounted() {
      this.$emit('title-changed', 'Add existing host resource')
      this.$store.dispatch('getResourceConnectionTypes')
    },
    methods: {
      updateConnectionPort(connectionTypeName) {
        let connectionType = this.resourceConnectionTypes.find(ct => {
          return ct.name == connectionTypeName
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
        const show = this.availableBaseConfigurationCapabilities.length>0 && this.resourceAccessAvailable
        return show
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
        resourcesRestApi.addExistingResource(
          this.resourceHostname,
          this.resourceIp,
          this.resourceLocation,
          this.resourceUsername,
          this.resourcePassword,
          'BareMetal',
          this.resourceAccessAvailable,
          this.resourceConnectionType,
          this.resourceConnectionPort,
          this.resourceBaseConfigurationId
        )

        this.clearForm()
        this.$emit('confirmed')
      },
      ipValueChanged (value) {
      },

    },
  }
</script>
