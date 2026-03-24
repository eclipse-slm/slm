<template>
  <v-container class="my-0 mx-8 pa-0">
    <div
      v-if="parameter.valueType==='STRING'"
    >
      <v-text-field
        v-if="parameter.secret"
        v-model="value"
        :label="parameter.prettyName"
        type="password"
        :prepend-icon="getParamLogo(parameter)"
      />
      <v-text-field
        v-else
        v-model="value"
        :label="parameter.prettyName"
        :prepend-icon="getParamLogo(parameter)"
      />
    </div>
    <v-text-field
      v-if="parameter.valueType==='SERVICE_PORT'"
      v-model="value"
      :label="parameter.prettyName"
      type="number"
      :prepend-icon="getParamLogo(parameter)"
    />
  </v-container>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  parameter: {
    type: Object,
    default: undefined
  }
});

const emit = defineEmits(['parameter-value-changed']);

const value = ref(props.parameter.defaultValue);

watch(value, (newValue) => {
  emit('parameter-value-changed', props.parameter.name, newValue);
});

const getParamLogo = (param) => {
  return "mdi-alpha-" + param.name[0] + "-box";
};
</script>