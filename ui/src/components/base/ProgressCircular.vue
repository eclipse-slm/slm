<template>
  <v-overlay
    v-if="showAsOverlay"
    :absolute="true"
    :value="overlay"
  >
    <v-progress-circular
      :size="size"
      :width="width"
      :color="color"
      indeterminate
    />
  </v-overlay>

  <v-progress-circular
    v-else
    :size="size"
    :width="width"
    :color="color"
    indeterminate
  />
</template>

<script>
export default {
  name: "ProgressCircular",
  props: {
    size: {
      default: "70",
      type: String
    },
    width: {
      default: "7",
      type: String
    },
    color: {
      default: "secondary",
      type: String
    },
    showAsOverlay: {
      default: false,
      type: Boolean
    },
    overlay: {
      default: false,
      type: Boolean
    }
  }
}
</script>

<style scoped>

</style>
