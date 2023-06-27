<template>
  <v-data-table
    :headers="tableHeaders"
    :items="serviceOptionCategory.serviceOptions"
    item-key="id"
    :show-select="false"
    :disable-pagination="true"
    :hide-default-footer="true"
    class="page__table"
  >
    <template #body="props">
      <draggable
        :list="serviceOptionCategory.serviceOptions"
        tag="tbody"
        group="a"
      >
        <tr
          v-for="(serviceOption, index) in props.items"
          :key="index"
        >
          <td>
            <v-icon
              small
              class="page__grab-icon"
            >
              mdi-arrow-all
            </v-icon>
          </td>
          <td> {{ serviceOption.key }} </td>
          <td>
            <ValidationProvider
              v-slot="{ errors, valid }"
              name="Display Name"
              rules="required"
            >
              <v-text-field
                v-model="serviceOption.name"
                :error-messages="errors"
                :success="valid"
              />
            </ValidationProvider>
          </td>
          <td>
            <ValidationProvider
              v-slot="{ errors, valid }"
              name="Description"
              rules="required"
            >
              <v-text-field
                v-model="serviceOption.description"
                :error-messages="errors"
                :success="valid"
              />
            </ValidationProvider>
          </td>
          <td>
            <v-tooltip
              bottom
              :disabled="!serviceOption.required"
            >
              <template #activator="{ on, attrs }">
                <div v-on="on">
                  <service-option-value
                    :service-option="serviceOption"
                    :disabled="serviceOption.required && serviceOption.valueType !== 'AAS_SM_TEMPLATE'"
                    :definition-mode="true"
                    v-bind="attrs"
                  />
                </div>
              </template>
              <span>If service option is required to be set by user no value is needed.</span>
            </v-tooltip>
          </td>
          <td>
            <v-select
              v-model="serviceOption.valueType"
              :items="validatorList"
              :disabled="serviceOption.optionType == 'VOLUME' || serviceOption.optionType == 'PORT_MAPPING'"
              @change="onValueTypeChanged(serviceOption, serviceOption.valueType)"
            />
          </td>
          <td>
            <v-tooltip bottom>
              <template #activator="{ on, attrs }">
                <v-simple-checkbox
                  v-model="serviceOption.required"
                  v-bind="attrs"
                  color="primary"
                  :ripple="false"
                  :disabled="serviceOption.valueType === 'SYSTEM_VARIABLE' || serviceOption.valueType === 'DEPLOYMENT_VARIABLE' || serviceOption.valueType === 'AAS_SM_TEMPLATE'"
                  v-on="on"
                  @click="onServiceOptionRequiredChanged(serviceOption)"
                />
              </template>
              <span>If service option is required to be set by user it needs to be editable.</span>
            </v-tooltip>
          </td>
          <td>
            <v-simple-checkbox
              v-model="serviceOption.editable"
              color="primary"
              :ripple="false"
              :disabled="serviceOption.valueType === 'SYSTEM_VARIABLE' || serviceOption.valueType === 'DEPLOYMENT_VARIABLE' || serviceOption.valueType === 'AAS_SM_TEMPLATE'"
              @click="onServiceOptionEditableChanged(serviceOption)"
            />
          </td>
        </tr>
      </draggable>
    </template>
  </v-data-table>
</template>

<script>
import draggable from 'vuedraggable'
import ServiceOptionValue from "@/components/service_offerings/ServiceOptionValue";

export default {
  name: "ServiceOptionsDefinitionTable",
  components: {
    draggable,
    ServiceOptionValue
  },
  props: ['serviceOptionCategory'],
  data () {
    return {
      validatorList: [
        'STRING',
        'PASSWORD',
        'BOOLEAN',
        'NUMBER',
        'INTEGER',
        'DECIMAL',
        'EMAIL',
        'IP',
        'ENUM',
        'AUTO_GENERATED_UUID',
        'PORT',
        'VOLUME',
        'AAS_SM_TEMPLATE',
        'SYSTEM_VARIABLE',
        'DEPLOYMENT_VARIABLE',
      ],
      tableHeaders: [
        { text: '', value: '', sortable: false },
        { text: 'Key', value: 'key', sortable: false },
        { text: 'Display Name', value: 'name', sortable: false },
        { text: 'Description', value: 'description', sortable: false },
        { text: 'Value', value: 'value', sortable: false },
        { text: 'Value Type', value: 'valueType', sortable: false },
        { text: 'Required', value: 'required', sortable: false },
        { text: 'Editable', value: 'editable', sortable: false },
      ],
    }
  },
  methods: {
    onServiceOptionRequiredChanged (serviceOption) {
      if (serviceOption.required) {
        serviceOption.editable = true
        serviceOption.defaultValue = null
      }
    },
    onServiceOptionEditableChanged (serviceOption) {
      if (!serviceOption.editable) {
        serviceOption.required = false
      }
    },
    onValueTypeChanged (serviceOption, newValueType) {
      if (newValueType === "IP") {
        serviceOption.defaultValue = undefined
      }
      else if (newValueType === "AAS_SM_TEMPLATE") {
        serviceOption.required = true
        serviceOption.editable = true
      }
    }
  }
}
</script>

<style scoped>

</style>
