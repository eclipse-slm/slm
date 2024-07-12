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
      :name="serviceOption.name"
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

      <vue-ip
        v-if="serviceOption.valueType === 'IP'"
        :ip="serviceOption.defaultValue === undefined ? '127.0.0.1' : serviceOption.value"
        :on-change="ipValueChanged"
        theme="material"
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
  import VueIp from 'vue-ip'
  import { serviceOptionMixin } from '@/utils/serviceOptionUtil'
  import AASRestApi from "@/api/resource-management/aasRestApi";

  import {Field } from "vee-validate";
  import {useServicesStore} from "@/stores/servicesStore";
  import {useResourcesStore} from "@/stores/resourcesStore";
  import {useCatalogStore} from "@/stores/catalogStore";

  export default {
    name: 'ServiceOptionValue',
    components: {
      VueIp,
      Field
    },
    mixins: [serviceOptionMixin],
    props: ['serviceOption', 'disabled', 'definitionMode'],
    setup(){
      const servicesStore = useServicesStore();
      const resourceStore = useResourcesStore();
      const catalogStore = useCatalogStore();
      return {servicesStore, resourceStore, catalogStore};
    },
    data () {
      return {
        aasSubmodelTemplateInstances: [],
      }
    },
    computed: {
      valueOfTemplateVariable() {
        return this.resourceStore.valueOfTemplateVariable
      },
      serviceManagementSystemVariables() {
        return this.servicesStore.serviceManagementSystemVariables
      },
      serviceManagementDeploymentVariables () {
        return this.servicesStore.serviceManagementDeploymentVariables
      },
      aasSubmodelTemplates () {
        return this.catalogStore.aasSubmodelTemplates
      },
    },
    created() {
      if (this.serviceOption.valueType === 'AAS_SM_TEMPLATE' && !this.definitionMode) {
        AASRestApi.getSubmodelTemplateInstancesBySemanticId(this.serviceOption.defaultValue).then(aasSmTemplateInstances => {
          this.aasSubmodelTemplateInstances = aasSmTemplateInstances
        })
      }
    },
    methods: {
      ipValueChanged (value) {
        console.log(value)
      },
    },
  }
</script>
