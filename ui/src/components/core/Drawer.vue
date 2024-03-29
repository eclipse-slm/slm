<template>
  <!--  :dark="barColor !== 'rgba(228, 226, 226, 1), rgba(255, 255, 255, 0.7)'"-->
  <v-navigation-drawer
    id="core-navigation-drawer"
    v-model="drawer"
    class="primary"
    dark
    :expand-on-hover="expandOnHover"
    :right="$vuetify.rtl"
    mobile-breakpoint="960"
    app
    width="260"
    v-bind="$attrs"
  >
    <template #img="props">
      <v-img
        :gradient="`to bottom, ${barColor}`"
        v-bind="props"
      />
    </template>

    <v-divider class="mb-1" />

    <v-list
      dense
      nav
    >
      <v-list-item
        class="mb-5"
      >
        <v-img
          src="../../assets/img/fabos_logo_fabweiss.png"
          max-height="30"
        />
      </v-list-item>
    </v-list>

    <v-divider class="mb-2" />

    <v-list
      expand
      nav
    >
      <!-- Style cascading bug  -->
      <!-- https://github.com/vuetifyjs/vuetify/pull/8574 -->
      <div />

      <template v-for="(item, i) in computedItems">
        <div
          v-if="item.visible"
          :key="i"
        >
          <base-item-group
            v-if="item.children"
            :key="`group-${i}`"
            :item="item"
          >
          <!--  -->
          </base-item-group>

          <base-item
            v-else
            :id="item.id"
            :key="`item-${i}`"
            :item="item"
          />
        </div>
      </template>

      <!-- Style cascading bug  -->
      <!-- https://github.com/vuetifyjs/vuetify/pull/8574 -->
      <div />
    </v-list>
  </v-navigation-drawer>
</template>

<script>

  import {
    mapGetters,
    mapState,
  } from 'vuex'
  import i18n from '@/localisation/i18n'

  export default {
    name: 'DashboardCoreDrawer',
    props: {
      expandOnHover: {
        type: Boolean,
        default: false,
      },
    },

    data () {
      return {
      }
    },

    computed: {
      ...mapState(['barColor']),
      ...mapGetters([
        'isUserDeveloper',
        'userRoles',
      ]),
      drawer: {
        get () {
          return this.$store.state.drawer
        },
        set (val) {
          this.$store.commit('SET_DRAWER', val)
        },
      },
      computedItems () {
        return this.items.map(this.mapItem)
      },
      profile () {
        return {
          avatar: true,
          title: this.$t('avatar'),
        }
      },
      items () {
        return [
          {
            id: 'main-menu-button-dashboard',
            icon: 'mdi-view-dashboard',
            title: i18n.t('drawer.section.dashboard.title'),
            to: '/',
            visible: true,
          },
          {
            id: 'main-menu-button-jobs',
            icon: 'mdi-account-hard-hat',
            title: i18n.t('drawer.section.jobs.title'),
            to: '/jobs',
            visible: true,
          },
          {
            id: 'main-menu-button-resources',
            icon: 'mdi-desktop-classic',
            title: i18n.t('drawer.section.resources.title'),
            to: '/resources',
            visible: true,
          },
          {
            id: 'main-menu-button-clusters',
            icon: 'mdi-server',
            title: i18n.t('drawer.section.clusters.title'),
            to: '/clusters',
            visible: true,
          },
          // {
          //   id: 'main-menu-button-provider',
          //   icon: 'mdi-usb',
          //   title: i18n.t('drawer.section.provider.title'),
          //   to: '/provider',
          //   visible: true,
          // },
          {
            id: 'main-menu-button-service-offering',
            title: i18n.t('drawer.section.serviceOfferings.title'),
            icon: 'mdi-offer',
            to: '/services/offerings',
            visible: true,
          },
          {
            id: 'main-menu-button-service-instances',
            title: i18n.t('drawer.section.services.title'),
            icon: 'apps',
            to: '/services/instances',
            visible: true,
          },
          {
            id: 'main-menu-button-service-vendors',
            title: i18n.t('drawer.section.serviceVendor.title'),
            icon: 'smart_button',
            to: '/services/vendors',
            visible: this.isUserDeveloper,
          },
          {
            id: 'main-menu-button-admin',
            title: i18n.t('drawer.section.admin.title'),
            icon: 'admin_panel_settings',
            to: '/admin',
            visible: this.userRoles.includes('slm-admin'),
          },
        ]
      },
    },

    mounted () { },

    methods: {
      mapItem (item) {
        return {
          ...item,
          children: item.children ? item.children.map(this.mapItem) : undefined,
          title: this.$t(item.title),
        }
      },
    },
  }
</script>

<style scoped>
#core-navigation-drawer {
  color: white;
}
</style>

<style lang="sass">
  @import '~vuetify/src/styles/tools/_rtl.sass'

  #core-navigation-drawer
    .v-list-group__header.v-list-item--active:before
      opacity: .24

    .v-list-item
      &__icon--text,
      &__icon:first-child
        justify-content: center
        text-align: center
        width: 20px

        +ltr()
          margin-right: 24px
          margin-left: 12px !important

        +rtl()
          margin-left: 24px
          margin-right: 12px !important

    .v-list--dense
      .v-list-item
        &__icon--text,
        &__icon:first-child
          margin-top: 10px

    .v-list-group--sub-group
      .v-list-item
        +ltr()
          padding-left: 8px

        +rtl()
          padding-right: 8px

      .v-list-group__header
        +ltr()
          padding-right: 0

        +rtl()
          padding-right: 0

        .v-list-item__icon--text
          margin-top: 19px
          order: 0

        .v-list-group__header__prepend-icon
          order: 2

          +ltr()
            margin-right: 8px

          +rtl()
            margin-left: 8px
</style>
