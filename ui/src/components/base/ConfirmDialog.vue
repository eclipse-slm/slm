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
          :disabled="confirmLoading"
          @click="$emit('canceled')"
      >
        {{ cancelButtonLabel }}
      </v-btn>

      <v-spacer />

      <v-btn
          id="button-confirm-dialog"
          variant="text"
          :color="attention ? 'error' : ''"
          :disabled="confirmButtonDisabled || confirmLoading"
          @click="$emit('confirmed')"
      >
        <v-progress-circular
          v-if="confirmLoading"
          size="16"
          width="2"
          indeterminate
          class="mr-2"
        />
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
  confirmButtonDisabled: {
    type: Boolean,
    default: false
  },
  confirmLoading: {
    type: Boolean,
    default: false
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
