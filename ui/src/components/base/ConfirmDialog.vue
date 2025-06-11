<template>
  <v-dialog
    v-model="dialogActive"
    :width="width"
    @click:outside="$emit('canceled')"
  >
    <template #default="{isActive}">
      <v-card v-if="isActive">
        <v-toolbar
          color="primary"
          theme="dark"
        >
          {{ title }}
        </v-toolbar>
        <v-card-text class="my-4">
          <slot name="content">
            {{ text }}
          </slot>
        </v-card-text>
        <v-card-actions class="justify-center">
          <v-btn
            id="button-confirm-dialog"
            variant="text"
            @click.native="$emit('canceled')"
          >
            {{ cancelButtonLabel }}
          </v-btn>

          <v-spacer />

          <v-btn
            id="button-confirm-dialog"
            variant="text"
            :color="attention ? 'error' : ''"
            @click="$emit('confirmed')"
          >
            {{ confirmButtonLabel }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </template>
  </v-dialog>
</template>

<script setup>
import {ref, toRef, watch} from 'vue';

const emit = defineEmits(['confirmed', 'canceled']);

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ""
  },
  text: {
    type: String,
    default: ""
  },
  confirmButtonLabel: {
    type: String,
    default: "Yes"
  },
  cancelButtonLabel: {
    type: String,
    default: "No"
  },
  width: {
    type: String,
    default: "400"
  },
  attention: {
    type: Boolean,
    default: false
  }
});


const dialogActive = ref(false)
const showProp = toRef(props,'show'); // react to prop

watch(showProp, (value) => {
  dialogActive.value = showProp.value;
});

</script>
