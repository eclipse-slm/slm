<template>
  <v-btn
    class="ma-1"
    size="small"
    :color="colorRef"
    @click="$emit('click')"
  >
    <v-icon>
      {{ iconRef }}
    </v-icon>
  </v-btn>
</template>

<script setup>

import {ActionButtonType} from "@/components/base/ActionButtonType";
import {ref} from "vue";

const emit = defineEmits(['click'])

const props = defineProps({
  type: {
    type: String,
    required: true,
    validator(value) {
      return Object.values(ActionButtonType).includes(value);
    },
  },
  color: {
    type: String,
    default: "",
    required: false
  },
  icon: {
    type: String,
    default: "",
    required: false
  }
})

let iconRef = ref(undefined);
let colorRef = ref(undefined);

switch(props.type) {
  case ActionButtonType.DELETE:
    iconRef.value = "mdi-delete";
    colorRef.value = "error"
    break;
  case ActionButtonType.EDIT:
    iconRef.value = "mdi-pencil";
    colorRef.value = "info"
    break;
  case ActionButtonType.CUSTOM:
    iconRef.value = props.icon
    colorRef.value = props.color
    break;
  default:
    console.log("Unknown action button type: " + props.type);
    break;
}


</script>