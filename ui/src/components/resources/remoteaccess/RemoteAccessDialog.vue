<script setup lang="ts">

import ConfirmDialog from "@/components/base/ConfirmDialog.vue";
import * as yup from "yup";
import {storeToRefs} from "pinia";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import {toRef} from "vue";
import {Field, Form} from "vee-validate";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";
import {useToast} from "vue-toast-notification";

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  resourceId: {
    type: String,
    required: true
  }
});

const emit = defineEmits(['canceled', 'confirmed']);

const $toast = useToast();

const active = toRef(props, 'show');

const string_required = yup.string().required();

const resourceDevicesStores = useResourceDevicesStore();
const { resourceConnectionTypes } = storeToRefs(resourceDevicesStores);

const remoteAccess = {
  connectionType: resourceConnectionTypes.value[0]?.name,
  connectionPort: resourceConnectionTypes.value[0]?.defaultPort,
  username: '',
  password: ''
}

const updateConnectionPort = (connectionTypeName: string) => {
  console.log(connectionTypeName)
  let connectionType = resourceConnectionTypes.value.find(ct => {
    return ct.name === connectionTypeName
  });

  if(connectionType !== undefined)
    remoteAccess.connectionPort = connectionType.defaultPort
}

const onConfirmClicked = () => {
  ResourceManagementClient.resourcesApi.setRemoteAccessOfResource(
      props.resourceId,
      remoteAccess.connectionType,
      remoteAccess.username,
      remoteAccess.password,
      remoteAccess.connectionPort
  ).then(() => {
    $toast.info("Remote access successfully added")
    clearForm()
    emit('confirmed')
  }
  ).catch((e) => {
    $toast.error("Error adding remote access")
    logRequestError(e)
  })
}

const clearForm = () => {
  remoteAccess.connectionType = resourceConnectionTypes.value[0]?.name
  remoteAccess.connectionPort = resourceConnectionTypes.value[0]?.defaultPort
  remoteAccess.username = ''
  remoteAccess.password = ''
}

</script>

<template>
  <Form
      ref="observer"
      v-slot="{ meta, handleSubmit, validate }"
  >
  <confirm-dialog
      :show="active"
      title="Add remote access"
      cancel-button-label="Cancel"
      confirm-button-label="Add"
      width="30%"
      @canceled="clearForm(); $emit('canceled');"
      @confirmed="!meta.valid ? validate() : handleSubmit(onConfirmClicked)"
  >
    <template #content>
      <v-row>
        <v-col cols="9">
          <Field
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
                :items="resourceConnectionTypes"
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
    </template>
  </confirm-dialog>
  </Form>
</template>

<style scoped>

</style>