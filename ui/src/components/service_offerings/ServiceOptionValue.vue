<template>
  <div>
    <v-text-field
      v-if="disabled"
      :disabled="disabled"
    />

    <validation-provider
      v-else
      v-slot="{ errors, valid }"
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
        v-model="serviceOption.defaultValue"
        :type="(serviceOption.valueType === 'INTEGER' || serviceOption.valueType === 'DECIMAL') ? 'number' : serviceOption.valueType === 'PASSWORD' ? 'password' : 'text'"
        required
        :clearable="serviceOption.editable || definitionMode"
        :readonly="!(serviceOption.editable || definitionMode)"
        :disabled="!(serviceOption.editable || definitionMode)"
        :error-messages="errors"
        :success="valid"
      />

      <v-select
        v-if="serviceOption.valueType.startsWith('ENUM') && !definitionMode"
        v-model="serviceOption.defaultValue"
        :items="serviceOption.valueOptions"
        :error-messages="errors"
        :success="valid"
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
        :success="valid"
      />

      <!-- AAS !-->
      <v-select
        v-if="serviceOption.valueType === 'AAS_SM_TEMPLATE' && definitionMode"
        v-model="serviceOption.defaultValue"
        placeholder="Select required submodel template"
        item-value="semanticId"
        item-text="name"
        :items="aasSubmodelTemplates"
        :error-messages="errors"
        :success="valid"
      />

      <v-select
        v-if="serviceOption.valueType === 'AAS_SM_TEMPLATE' && !definitionMode"
        v-model="serviceOption.defaultValue"
        placeholder="Select AAS"
        item-value="id"
        item-text="name"
        :items="aasSubmodelTemplateInstances"
        :error-messages="errors"
        :success="valid"
      />

      <!-- Template Variables !-->
      <v-select
        v-if="serviceOption.valueType === 'TEMPLATE_VARIABLE' && definitionMode"
        v-model="serviceOption.defaultValue"
        placeholder="Select template variable"
        item-value="key"
        item-text="name"
        :items="serviceManagementTemplateVariables"
        :error-messages="errors"
        :success="valid"
      />

      <v-tooltip
        v-if="serviceOption.valueType === 'TEMPLATE_VARIABLE' && !definitionMode"
        bottom
      >
        <template #activator="{ on, attrs }">
          <div
            v-bind="attrs"
            v-on="on"
          >
            <v-text-field
              :value="valueOfTemplateVariable(serviceOption.defaultValue)"
              :readonly="true"
              :disabled="true"
            />
          </div>
        </template>
        <span>Template variables cannot be edited</span>
      </v-tooltip>
    </validation-provider>
  </div>
</template>

<script>
  import VueIp from 'vue-ip'
  import { serviceOptionMixin } from '@/utils/serviceOptionUtil'
  import AASRestApi from "@/api/resource-management/aasRestApi";
  import { mapGetters } from "vuex";

  export default {
    name: 'ServiceOptionValue',
    components: {
      VueIp,
    },
    mixins: [serviceOptionMixin],
    props: ['serviceOption', 'disabled', 'definitionMode'],
    data () {
      return {
        aasSubmodelTemplateInstances: [],
      }
    },
    computed: {
      ...mapGetters([
        'valueOfTemplateVariable',
        'serviceManagementTemplateVariables',
        'aasSubmodelTemplates'
      ]),
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
