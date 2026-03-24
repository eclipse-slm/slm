<template>
  <v-overlay
    v-if="showAsOverlay"
    :absolute="true"
    :model-value="overlay"
    width="100%"
    height="100%"
  >
    <v-container class="fill-height">
      <v-row
        align="center"
        justify="center"
      >
        <v-col cols="auto">
          <!-- Your content here -->
          <div class="centered-content">
            <v-progress-circular
              :size="size"
              :width="width"
              :color="color"
              indeterminate
            />
          </div>
        </v-col>
      </v-row>
    </v-container>
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
