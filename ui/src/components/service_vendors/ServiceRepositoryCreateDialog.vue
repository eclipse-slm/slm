<template>
  <v-dialog
    v-model="active"
    width="400"
    @click:outside="$emit('canceled')"
  >
    <template>
      <validation-observer
        ref="observer"
        v-slot="{ invalid, handleSubmit, validate }"
      >
        <v-card>
          <v-toolbar
            color="primary"
            theme="dark"
          >
            Create new service repository
          </v-toolbar>
          <v-card-text>
            {{ repository.id }}
            <v-container class="pa-8">
              <validation-provider
                v-slot="{ errors }"
                name="Address"
                :rules="repository.id != null ? { regex: /^[0-9A-F]{8}-[0-9A-F]{4}-[4][0-9A-F]{3}-[89AB][0-9A-F]{3}-[0-9A-F]{12}$/i } : {}"
              >
                <v-text-field
                  v-model="repository.id"
                  label="Id"
                  placeholder="Generated"
                  prepend-icon="mdi-fingerprint"
                  :error-messages="errors"

                />
              </validation-provider>
              <validation-provider
                v-slot="{ errors }"
                name="Address"
                rules="required"
              >
                <v-text-field
                  v-model="repository.address"
                  label="Address"
                  prepend-icon="mdi-earth"
                  :error-messages="errors"

                />
              </validation-provider>
              <validation-provider
                v-slot="{ errors }"
                name="Username"
                rules="required"
              >
                <v-text-field
                  v-model="repository.username"
                  label="Username"
                  required
                  prepend-icon="mdi-account"
                  :error-messages="errors"

                />
              </validation-provider>
              <validation-provider
                v-slot="{ errors }"
                name="Password"
                rules="required"
              >
                <v-text-field
                  v-model="repository.password"
                  :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                  :type="showPassword ? 'text' : 'password'"
                  label="Password"
                  pass
                  prepend-icon="mdi-lock"
                  :error-messages="errors"

                  @click:append="showPassword = !showPassword"
                />
              </validation-provider>
              <v-select
                v-model="repository.type"
                :items="repositoryTypes"
                disabled
                prepend-icon="mdi-set-left"
              />
            </v-container>
          </v-card-text>
          <v-card-actions class="justify-center">
            <v-btn
              color="error"
              @click.native="$emit('canceled')"
            >
              Cancel
            </v-btn>
            <v-spacer />
            <v-btn
              :color="invalid ? $vuetify.theme.disable : $vuetify.theme.themes.light.secondary"
              @click="invalid ? validate() : handleSubmit(onConfirmedClicked)"
            >
              Create
            </v-btn>
          </v-card-actions>
        </v-card>
      </validation-observer>
    </template>
  </v-dialog>
</template>

<script>
  import {toRef} from "vue";

  export default {
    name: 'ServiceRepositoryCreateDialog',
    props: ['show'],
    data () {
      return {
        showPassword: false,
        repository: {
          id: null,
          address: null,
          username: null,
          password: null,
          type: 'DOCKER_REGISTRY',
        },
        repositoryTypes: ['DOCKER_REGISTRY'],
      }
    },
    methods: {
      onConfirmedClicked () {
        this.$emit('confirmed', this.repository)
        this.repository = {
          id: null,
          address: null,
          username: null,
          password: null,
          type: 'DOCKER_REGISTRY',
        }
      },
    },
    setup(props){
      const active = toRef(props, 'show')
      return{
        active
      }
    }
  }
</script>
