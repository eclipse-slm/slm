<template>
  <v-tooltip
    :text="tooltip"
    :disabled="tooltip === ''"
    :open-delay="tooltipDelay"
  >
    <template #activator="{ props }">
      <div v-bind="props">
        <div class="text-grey mb-0 text-caption">
          {{ label }}
        </div>
        <div>
          {{ text }}
        </div>
      </div>
    </template>
  </v-tooltip>
</template>

<script setup lang="ts">
const props = defineProps({
  label: {
    type: String,
    required: true,
  },
  text: {
    type: String,
    required: true,
  },
  tooltip: {
    type: String,
    default: "",
    required: false
  },
  tooltipDelay: {
    type: Number,
    default: 0,
    required: false
}
});
</script>