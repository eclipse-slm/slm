<script setup lang="ts">

const props = defineProps({
  label: {
    type: String,
    default: ""
  },
  text: {
    type: [String, Number],
    default: ""
  },
  divider: {
    type: Boolean,
    default: true
  }
});

</script>

<template>
  <div>
    <v-row>
      <v-col
        cols="2"
        class="ma-4"
        style="font-weight:bold"
      >
        <slot name="label">
          {{ props.label }}
        </slot>
      </v-col>
      <v-col
        class="ma-4"
      >
        <slot name="content">
          {{ props.text }}
        </slot>
      </v-col>
    </v-row>

    <v-divider v-if="props.divider" />
  </div>
</template>

<style scoped>
.v-divider {
  border-color: #000000;
}
</style>