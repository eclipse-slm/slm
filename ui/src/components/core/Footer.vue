<template>
  <v-footer
    id="core-footer"
  >
    <v-row
      align="center"
      no-gutters
    >
      <v-spacer class="hidden-sm-and-down" />

      <v-col
        cols="12"
        md="auto"
      >
        <div class="text-body-1 font-weight-light pt-6 pt-md-0 text-center">
          &copy; 2023, Fraunhofer IPA
        </div>
      </v-col>
    </v-row>
  </v-footer>
</template>

<script>
  export default {
    name: 'DashboardCoreFooter',

    data: () => ({
      links: [
        {
          href: '#',
          text: 'About Us',
        },
      ],
    }),
  }
</script>

<style lang="sass">
  #dashboard-core-footer
    a
      font-size: .825rem
      font-weight: 500
      text-decoration: none
      text-transform: uppercase
</style>
