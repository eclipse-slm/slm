<template>
  <v-icon
    size="small"
    :class="connectionState"
  >
    {{ icon }}
  </v-icon>
</template>

<script>
  export default {
    name: 'ResourceConnectionIcon',
    props: [
      'resource',
    ],
    data () {
      return {
        icon: '',
      }
    },
    computed: {
      // eslint-disable-next-line vue/return-in-computed-property
      connectionState () {
        if (this.resource.connection.connectionState === 'CONNECTED') {
          // eslint-disable-next-line vue/no-side-effects-in-computed-properties
          this.icon = 'sync'
          return 'green--text'
        } else if (this.resource.connection.connectionState === 'UNCONNECTED') {
          // eslint-disable-next-line vue/no-side-effects-in-computed-properties
          this.icon = 'sync_disabled'
          return 'red--text'
        }
      },
    },
  }
</script>

<style scoped>

</style>
