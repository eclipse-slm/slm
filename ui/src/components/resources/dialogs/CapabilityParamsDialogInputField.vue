<template>
  <v-container class="my-0 mx-8 pa-0">
    <v-container
      v-if="param.valueType==='STRING'"
      class="ma-0 pa-0"
    >
      <v-text-field
        v-if="param.secret"
        v-model="value"
        :label="param.prettyName"
        type="password"
        :prepend-icon="getParamLogo(param)"
      />
      <v-text-field
        v-else
        v-model="value"
        :label="param.prettyName"
        :prepend-icon="getParamLogo(param)"
      />
    </v-container>
    <v-text-field
      v-if="param.valueType==='SERVICE_PORT'"
      v-model="value"
      :label="param.prettyName"
      type="number"
      :prepend-icon="getParamLogo(param)"
    />
  </v-container>
</template>
<script>
export default {
  name: "CapabilityParamsDialogInputField",
  props: ['param'],
  data() {
    return {
      key: this.param.name,
      value: this.param.defaultValue
    }
  },
  watch: {
    value(newValue) {
      this.$emit('updateParamMap', this.key, newValue)
    }
  },
  methods: {
    getParamLogo(param) {
      const icon = "mdi-alpha-"+param.name[0]+"-box"
      return icon
    }
  }
}
</script>