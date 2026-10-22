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

import {defaultNavigation, resolveNavItems} from "@/app/navigation";
import customizeNavigation from "@/customization/customizeNavigation";
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
        return resolveNavItems(
          customizeNavigation(defaultNavigation),
          {roles: this.userStore.userRoles, isDeveloper: this.userStore.isUserDeveloper},
          (key) => this.$t(key),
        )
      },
      profile () {
        return {
          avatar: true,
          title: this.$t('avatar'),
        }
      },
    },

    mounted () { },
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
