<template>
  <!--  :dark="barColor !== 'rgba(228, 226, 226, 1), rgba(255, 255, 255, 0.7)'"-->
  <v-navigation-drawer
    id="core-navigation-drawer"
    v-model="drawer"
    class="bg-primary"
    theme="dark"
    :expand-on-hover="expandOnHover"
    :location="'left'"

    mobile-breakpoint="960"
    width="260"
    v-bind="$attrs"
  >
    <template #image="props">
      <v-img
        :gradient="`to bottom, ${mainStore.barColor}`"
        v-bind="props"
        style="height: 100%;width: 100%;"
      />
    </template>

    <v-divider
      class="mb-1"
      style="background-color: rgb(33,33,33) !important"
    />

    <v-list
      density="compact"
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
      nav
    >
      <template v-for="(item, i) in computedItems">
        <div
          v-if="item.visible"
          :key="i"
        >
          <base-item-group
            v-if="item.children"
            :key="`group-${i}`"
            :item="item"
            :sub-group="item.subGroup"
            :text="item.text"
          >
          <!--  -->
          </base-item-group>

          <div
            v-else-if="item.divider && !item.title"
            class="mt-4"
          >
            <v-divider />
          </div>

          <div
            v-else-if="item.divider && item.title"
            class="mt-4"
          >
            <v-divider>
              {{ item.title }}
            </v-divider>
          </div>

          <base-item
            v-else
            :id="item.id"
            :key="`item-${i}`"
            :item="item"
          />
        </div>
      </template>
    </v-list>
  </v-navigation-drawer>
</template>

<script>

import {right} from "core-js/internals/array-reduce";
import {useStore} from "@/stores/store";
import {useUserStore} from "@/stores/userStore";

export default {
    name: 'DashboardCoreDrawer',
    props: {
      expandOnHover: {
        type: Boolean,
        default: false,
      },
    },
    setup(){
      const mainStore = useStore();
      const userStore = useUserStore();
      const store = useStore();
      return {mainStore, userStore, store};
    },

    data () {
      return {
      }
    },

    computed: {
      drawer: {
        get () {
          return this.store.drawer;
        },
        set (val) {
          return this.store.drawer = val;
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
            title: this.$t('drawer.section.dashboard.title'),
            to: '/dashboard',
            visible: true,
          },
          {
            id: 'main-menu-button-jobs',
            icon: 'mdi-account-hard-hat',
            title: this.$t('drawer.section.jobs.title'),
            to: '/jobs',
            visible: true,
          },
          {
            id: 'main-menu-divider-resources',
            title: this.$t('drawer.section.resources.title'),
            divider: true,
            visible: true
          },
          {
            id: 'main-menu-button-resources',
            icon: 'mdi-desktop-classic',
            title: this.$t('drawer.section.resources.devices.title'),
            to: '/resources',
            visible: true,
          },
          {
            id: 'main-menu-button-clusters',
            icon: 'mdi-server',
            title: this.$t('drawer.section.resources.clusters.title'),
            to: '/clusters',
            visible: true,
          },
          {
            id: 'main-menu-button-discovery',
            icon: 'mdi-tab-search',
            title: this.$t('drawer.section.resources.discovery.title'),
            group: '/discovery',
            subGroup:true,
            text: true,
            to: '/discovery/inbox',
            visible: true,
            children: [
              {
                id: 'main-menu-button-admin-components',
                title: this.$t('drawer.section.resources.discovery.inbox.title'),
                icon: 'mdi-tray-full',
                to: 'inbox',
                visible: true
              },
              {
                id: 'main-menu-button-admin-service-categories',
                title: this.$t('drawer.section.resources.discovery.drivers.title'),
                icon: 'mdi-magnify-scan',
                to: 'drivers',
                visible: true
              }
            ]
          },
          {
            id: 'main-menu-divider-services',
            title: this.$t('drawer.section.services.title'),
            divider: true,
            visible: true
          },
          {
            id: 'main-menu-button-service-offering',
            title: this.$t('drawer.section.services.serviceOfferings.title'),
            icon: 'mdi-offer',
            to: '/services/offerings',
            visible: true,
          },
          {
            id: 'main-menu-button-service-instances',
            title: this.$t('drawer.section.services.services.title'),
            icon: 'mdi-apps',
            to: '/services/instances',
            visible: true,
          },
          {
            id: 'main-menu-button-service-vendors',
            title: this.$t('drawer.section.services.serviceVendor.title'),
            icon: 'mdi-toolbox',
            to: '/services/vendors',
            visible: this.userStore.isUserDeveloper,
          },
          {
            id: 'main-menu-divider-admin',
            divider: true,
            visible: true
          },
          {
            id: 'main-menu-button-admin',
            title: this.$t('drawer.section.admin.title'),
            icon: 'mdi-shield-account-variant-outline',
            group: '/admin',
            subGroup:true,
            text: true,
            visible: this.userStore.userRoles.includes('slm-admin'),
            children: [
              {
                id: 'main-menu-button-admin-components',
                title: this.$t('drawer.section.admin.components.title'),
                icon: 'mdi-view-module',
                to: 'components',
                visible: this.userStore.userRoles.includes('slm-admin'),
              },
              {
                id: 'main-menu-button-admin-service-categories',
                title: this.$t('drawer.section.admin.service-categories.title'),
                icon: 'mdi-shape-outline',
                to: 'service-categories',
                visible: this.userStore.userRoles.includes('slm-admin'),
              },
              {
                id: 'main-menu-button-admin-service-vendors',
                title: this.$t('drawer.section.admin.service-vendors.title'),
                icon: 'mdi-treasure-chest',
                to: 'service-vendors',
                visible: this.userStore.userRoles.includes('slm-admin'),
              }
            ],
          },
        ]
      },
    },

    mounted () { },

    methods: {
      right,
      mapItem (item) {
        return {
          ...item,
          children: item.children ? item.children.map(this.mapItem) : undefined,
          title: item.title,
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
  @use 'vuetify/lib/styles/tools/_rtl.sass' as *

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
