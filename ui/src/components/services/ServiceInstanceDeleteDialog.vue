<script setup lang="ts">
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";

const props = defineProps({
  service: {
    type: Object,
    default: null
  },
  show: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['confirmed', 'canceled'])
</script>

<template>
  <ConfirmDialog
      :show="props.show"
      title="Delete Service Instance"
      confirm-text="Delete"
      :text="`Do you want to delete the service instance '${props.service?.id}'?`"
      :attention="true"
      @confirmed="emit('confirmed')"
      @canceled="emit('canceled')"
  >
  </ConfirmDialog>
</template>