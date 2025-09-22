<template>
  <CustomDialog
    v-bind="$props"
    v-on="$attrs"
    @canceled="emit('canceled')">

    <template v-for="(_, name) in $slots" v-slot:[name]="slotProps">
      <slot v-if="slotProps" :name="name" v-bind="slotProps" />
      <slot v-else :name="name" />
    </template>

    <template #actions>
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
    </template>
  </CustomDialog>
</template>

<script setup>
import CustomDialog from "@/components/base/CustomDialog.vue";

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
</script>
