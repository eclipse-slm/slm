<template>
  <v-dialog
    v-model="active"
    width="400"
    @click:outside="$emit('canceled')"
  >
    <template #default="{}">
      <ValidationForm
        ref="observer"
        v-slot="{ meta, handleSubmit, validate }"
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
              <Field
                v-slot="{ field, errors }"
                v-model="repository.id"
                name="Id"
                :rules="repository.id != null ? reg_required : {}"
              >
                <v-text-field
                  v-bind="field"
                  label="Id"
                  placeholder="Generated"
                  prepend-icon="mdi-fingerprint"
                  :error-messages="errors"
                  :model-value="repository.id"
                />
              </Field>
              <Field
                v-slot="{ field, errors }"
                v-model="repository.address"
                name="Address"
                :rules="required"
              >
                <v-text-field
                  v-bind="field"
                  label="Address"
                  prepend-icon="mdi-earth"
                  :error-messages="errors"
                  :model-value="repository.address"
                />
              </Field>
              <Field
                v-slot="{ field, errors }"
                v-model="repository.username"
                name="Username"
                :rules="required"
              >
                <v-text-field
                  v-bind="field"
                  label="Username"
                  required
                  prepend-icon="mdi-account"
                  :error-messages="errors"
                  :model-value="repository.username"
                />
              </Field>
              <Field
                v-slot="{ field, errors }"
                v-model="repository.password"
                name="Password"
                :rules="required"
              >
                <v-text-field
                  v-bind="field"
                  :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                  :type="showPassword ? 'text' : 'password'"
                  label="Password"
                  pass
                  prepend-icon="mdi-lock"
                  :error-messages="errors"
                  :model-value="repository.password"
                  @click:append="showPassword = !showPassword"
                />
              </Field>
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
              variant="elevated"
              color="error"
              @click.native="$emit('canceled')"
            >
              Cancel
            </v-btn>
            <v-spacer />
            <v-btn
              variant="elevated"
              :color="!meta.valid ? $vuetify.theme.themes.light.colors.disable : $vuetify.theme.themes.light.colors.secondary"
              @click="!meta.valid ? validate() : handleSubmit(onConfirmedClicked)"
            >
              Create
            </v-btn>
          </v-card-actions>
        </v-card>
      </ValidationForm>
    </template>
  </v-dialog>
</template>

<script>
import {toRef} from "vue";
import {Field, Form as ValidationForm} from "vee-validate";
import * as yup from 'yup';

export default {
    name: 'ServiceRepositoryCreateDialog',
    components: {Field, ValidationForm},
    props: {
      show: {
        type: Boolean,
        default: false
      },
    },
    setup(props){
      const required = yup.string().required();
      const reg_required = yup.string().matches(/^[0-9A-F]{8}-[0-9A-F]{4}-[4][0-9A-F]{3}-[89AB][0-9A-F]{3}-[0-9A-F]{12}$/i)
      const active = toRef(props, 'show')
      return {
        required,
        active,
        reg_required
      }
    },
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
  }
</script>
