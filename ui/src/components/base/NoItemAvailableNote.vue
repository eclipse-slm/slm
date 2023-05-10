<template>
  <v-card-text>
    <v-alert
      outlined
      type="info"
    >
      No {{ item }} available
    </v-alert>
  </v-card-text>
</template>

<script>
export default {
  name: 'NoItemAvailableNote',
  props: ['item']
}
</script>