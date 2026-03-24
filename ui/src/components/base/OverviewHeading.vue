<template>
  <v-container
    fluid
    class="ma-0 pa-0"
  >
    <v-row class="bg-secondary">
      <v-col
        class="text-h3 font-weight-light"
      >
        {{ text }}
      </v-col>
    </v-row>
  </v-container>
</template>
<script>
export default {
  name: 'OverviewHeading',
  props: {
    text: {
      type: String,
      default: ""
    }
  }
}
</script>