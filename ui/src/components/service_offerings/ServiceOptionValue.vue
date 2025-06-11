<template>
  <div>
    <v-text-field
      v-if="disabled"
      :disabled="disabled"
    />

    <Field
      v-else
      v-slot="{ field, errors }"
      v-model="serviceOption.defaultValue"
      :name="serviceOption.name + '_DEFAULT_VALUE'"
      :rules="getValidationRulesForServiceOption(serviceOption)"
    >
      <v-text-field
        v-if="serviceOption.valueType === 'STRING' ||
          serviceOption.valueType === 'PASSWORD' ||
          serviceOption.valueType === 'EMAIL' ||
          serviceOption.valueType === 'NUMBER' ||
          serviceOption.valueType === 'INTEGER' ||
          serviceOption.valueType === 'DECIMAL' ||
          serviceOption.valueType === 'PORT' ||
          serviceOption.valueType === 'VOLUME'"
        v-bind="field"
        :type="(serviceOption.valueType === 'INTEGER' || serviceOption.valueType === 'DECIMAL') ? 'number' : serviceOption.valueType === 'PASSWORD' ? 'password' : 'text'"
        required
        :clearable="serviceOption.editable || definitionMode"
        :readonly="!(serviceOption.editable || definitionMode)"
        :disabled="!(serviceOption.editable || definitionMode)"
        :error-messages="errors"
        :model-value="serviceOption.defaultValue"
      />

      <v-select
        v-if="serviceOption.valueType.startsWith('ENUM') && !definitionMode"
        v-model="serviceOption.defaultValue"
        :items="serviceOption.valueOptions"
        :error-messages="errors"
      />

      <v-text-field
        v-if="serviceOption.valueType === 'IP'"
        v-bind="field"
        type="text"
        :clearable="serviceOption.editable || definitionMode"
        :readonly="!(serviceOption.editable || definitionMode)"
        :disabled="!(serviceOption.editable || definitionMode)"
        :error-messages="errors"
        :model-value="serviceOption.defaultValue"
      />


      <v-checkbox
        v-if="serviceOption.valueType === 'BOOLEAN'"
        v-model="serviceOption.defaultValue"
        :readonly="!serviceOption.editable && !definitionMode"
        :error-messages="errors"
      />

      <!-- AAS !-->
      <v-select
        v-if="serviceOption.valueType === 'AAS_SM_TEMPLATE' && definitionMode"
        v-model="serviceOption.defaultValue"
        placeholder="Select required submodel template"
        item-value="semanticId"
        item-title="name"
        :items="aasSubmodelTemplates"
        :error-messages="errors"
      />

      <v-select
        v-if="serviceOption.valueType === 'AAS_SM_TEMPLATE' && !definitionMode"
        v-model="serviceOption.defaultValue"
        placeholder="Select AAS"
        item-value="id"
        item-title="name"
        :items="aasSubmodelTemplateInstances"
        :error-messages="errors"
      />

      <!-- System Variables !-->
      <v-select
        v-if="serviceOption.valueType === 'SYSTEM_VARIABLE' && definitionMode"
        v-model="serviceOption.defaultValue"
        placeholder="Select system variable"
        item-value="key"
        item-title="name"
        :items="serviceManagementSystemVariables"
        :error-messages="errors"
      />

      <v-tooltip
        v-if="serviceOption.valueType === 'SYSTEM_VARIABLE' && !definitionMode"
        location="bottom"
      >
        <template #activator="{ props }">
          <div
            v-bind="props"
          >
            <v-text-field
              :model-value="valueOfTemplateVariable(serviceOption.defaultValue)"
              :readonly="true"
              :disabled="true"
            />
          </div>
        </template>
        <span>Template variables cannot be edited</span>
      </v-tooltip>

      <!-- Deployment Variables !-->
      <v-select
        v-if="serviceOption.valueType === 'DEPLOYMENT_VARIABLE' && definitionMode"
        v-model="serviceOption.defaultValue"
        placeholder="Select deployment variable"
        item-value="key"
        item-title="prettyName"
        :items="serviceManagementDeploymentVariables"
        :error-messages="errors"
      />

      <v-tooltip
        v-if="serviceOption.valueType === 'DEPLOYMENT_VARIABLE' && !definitionMode"
        location="bottom"
      >
        <template #activator="{ props }">
          <div
            v-bind="props"
          >
            <v-text-field
              :model-value="serviceOption.defaultValue"
              :readonly="true"
              :disabled="true"
            />
          </div>
        </template>
        <span>Deployment variables cannot be edited</span>
      </v-tooltip>
    </Field>
  </div>
</template>

<script>
import {serviceOptionMixin} from '@/utils/serviceOptionUtil'

import {Field} from "vee-validate";
import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import {useCatalogStore} from "@/stores/catalogStore";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";

export default {
    name: 'ServiceOptionValue',
    components: {
      Field
    },
    mixins: [serviceOptionMixin],
    props: {
      serviceOption: {
        type: Object,
        default: null
      },
      disabled: {
        type: Boolean,
        default: false
      },
      definitionMode: {
        type: Boolean,
        default: false
      }
    },
    setup(){
      const serviceOfferingsStore = useServiceOfferingsStore();
      const resourceDevicesStore = useResourceDevicesStore();
      const catalogStore = useCatalogStore();
      return {serviceOfferingsStore, resourceDevicesStore, catalogStore};
    },
    data () {
      return {
        aasSubmodelTemplateInstances: [],
      }
    },
    computed: {
      serviceManagementSystemVariables() {
        return this.serviceOfferingsStore.serviceManagementSystemVariables
      },
      serviceManagementDeploymentVariables () {
        return this.serviceOfferingsStore.serviceManagementDeploymentVariables
      },
      aasSubmodelTemplates () {
        return this.catalogStore.aasSubmodelTemplates
      },
    },
    created() {
      if (this.serviceOption.valueType === 'AAS_SM_TEMPLATE' && !this.definitionMode) {
        ResourceManagementClient.submodelTemplatesRestControllerApi.getSubmodelTemplateInstancesBySemanticId(this.serviceOption.defautlValue)
            .then(response => {
              if(response.data){
                this.aasSubmodelTemplateInstances = response.data;
              }
            }).catch(logRequestError)
      }
    },
    methods: {
      ipValueChanged (value) {
        console.log(value)
      },
      valueOfTemplateVariable(templateVariableKey) {
        return this.serviceOfferingsStore.valueOfTemplateVariable(templateVariableKey)
      },
    },
  }
</script>
