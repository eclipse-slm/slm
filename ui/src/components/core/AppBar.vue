<template>
  <v-app-bar
    id="app-bar"
    absolute
    app
    color="transparent"
    flat
    height="75"
  >
    <v-btn
      class="mr-3"
      elevation="1"
      fab
      small
      @click="setDrawer(!drawer)"
    >
      <v-icon v-if="value">
        mdi-view-quilt
      </v-icon>

      <v-icon v-else>
        mdi-dots-vertical
      </v-icon>
    </v-btn>

    <v-toolbar-title
      class="hidden-sm-and-down font-weight-light"
    />

    <v-spacer />

    <div class="mx-3" />

    <v-menu
      v-model="menu"
      :close-on-content-click="false"
      bottom
      left
      offset-y
      origin="top right"
      transition="scale-transition"
      content-class="v-settings"
      nudge-left="8"
    >
      <template #activator="{ attrs, on }">
        <v-btn
          class="ml-2"
          min-width="0"
          text
          v-bind="attrs"
          v-on="on"
        >
          <v-icon color="primary">
            settings
          </v-icon>
        </v-btn>
      </template>

      <v-card
        class="text-center mb-0"
        width="300"
      >
        <v-card-text>
          <strong class="mb-3 d-inline-block">SETTINGS</strong>

          <!--          <v-item-group v-model="color">-->
          <!--            <v-item-->
          <!--              v-for="color in colors"-->
          <!--              :key="color"-->
          <!--              :value="color"-->
          <!--            >-->
          <!--              <template v-slot="{ active, toggle }">-->
          <!--                <v-avatar-->
          <!--                  :class="active && 'v-settings__item&#45;&#45;active'"-->
          <!--                  :color="color"-->
          <!--                  class="v-settings__item"-->
          <!--                  size="25"-->
          <!--                  @click="toggle"-->
          <!--                />-->
          <!--              </template>-->
          <!--            </v-item>-->
          <!--          </v-item-group>-->

          <!--          <v-divider class="my-4 secondary" />-->

          <v-row
            align="center"
            no-gutters
          >
            <v-col cols="auto">
              Dark Mode
            </v-col>

            <v-spacer />

            <v-col cols="auto">
              <v-switch
                v-model="$vuetify.theme.dark"
                class="ma-0 pa-0"
                color="secondary"
                hide-details
              />
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>
    </v-menu>

    <!--    <router-link-->
    <!--      to="/notifications"-->
    <!--      tag="button"-->
    <!--    >-->
    <!--      <v-badge-->
    <!--        color="red"-->
    <!--        overlap-->
    <!--        bordered-->
    <!--      >-->
    <!--        <template v-slot:badge>-->
    <!--          <span>{{ notifications_unread.length }}</span>-->
    <!--        </template>-->

    <!--        <v-icon color="primary">-->
    <!--          mdi-bell-->
    <!--        </v-icon>-->
    <!--      </v-badge>-->
    <!--    </router-link>-->

    <v-menu
      :close-on-content-click="false"
      bottom
      left
      offset-y
      origin="top right"
    >
      <template #activator="{ attrs }">
        <v-btn
          v-if="notifications.length > 0"
          class="notification-btn ml-2"
          min-width="0"
          text
          v-bind="attrs"
          to="/notifications"
        >
          <v-badge
            v-if="notifications_unread.length"
            color="red"
            overlap
            bordered
            :content="notifications_unread.length"
          >
            <v-icon color="primary">
              mdi-bell
            </v-icon>
          </v-badge>
          <v-icon
            v-else
            color="primary"
          >
            mdi-bell
          </v-icon>
        </v-btn>

        <v-btn
          v-else
          class="ml-2"
          min-width="0"
          disabled
          text
        >
          <v-icon color="primary">
            mdi-bell
          </v-icon>
        </v-btn>
      </template>

      <!--      <v-list-->
      <!--        v-if="notifications.length > 0"-->
      <!--        :tile="false"-->
      <!--        nav-->
      <!--      >-->
      <!--        <div>-->
      <!--          <app-bar-item-->
      <!--            v-for="(n, i) in notifications"-->
      <!--            :key="`item-${i}`"-->
      <!--          >-->
      <!--            <v-list-item-title-->
      <!--              @click="removeNotification(i)"-->
      <!--              v-text="n"-->
      <!--            />-->
      <!--          </app-bar-item>-->
      <!--        </div>-->
      <!--      </v-list>-->
    </v-menu>

    <v-menu
      bottom
      left
      offset-y
      origin="top right"
      transition="scale-transition"
    >
      <template #activator="{ attrs, on }">
        <v-btn
          v-bind="attrs"
          id="user-menu-button"
          class="ml-2"
          min-width="0"
          text
          v-on="on"
        >
          <v-icon color="primary">
            mdi-account
          </v-icon>
        </v-btn>
      </template>

      <v-list>
        <v-list-item
          v-for="(item, index) in userIconMenuItems"
          :id="item.id"
          :key="index"
          @click="userIconMenuItemClick(index)"
        >
          <v-list-item-icon>
            <v-icon
              color="primary"
            >
              {{ item.icon }}
            </v-icon>
          </v-list-item-icon>
          <v-list-item-title>{{ item.title }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>
  </v-app-bar>
</template>

<script>
  import { mapState, mapMutations, mapGetters } from 'vuex'
  import Vue from 'vue'

  export default {
    name: 'DashboardCoreAppBar',

    components: {
    },

    props: {
      value: {
        type: Boolean,
        default: false,
      },
    },

    data: () => ({
      color: '#179C7D',
      colors: [
        '#9C27b0',
        '#00CAE3',
        '#179C7D',
        '#4CAF50',
        '#ff9800',
        '#E91E63',
        '#FF5252',
      ],
      menu: false,
      userIconMenuItems: [
        {
          title: 'Profile',
          id: 'edit-button',
          icon: 'mdi-account',
          click () {
            this.$router.push({ path: '/user' })
          },
        },
        {
          title: 'Logout',
          id: 'logout-button',
          icon: 'mdi-logout',
          click () {
            Vue.prototype.$keycloak.logoutFn()
          },
        },
      ],
    }),

    computed: {
      ...mapState(['drawer']),
      ...mapGetters([
        'notifications',
        'notifications_unread',
      ]),
    },

    watch: {
      color (val) {
        this.$vuetify.theme.themes[this.isDark ? 'dark' : 'light'].primary = val
        this.setThemeColorMain(val)
      },
    },

    methods: {
      ...mapMutations({
        setDrawer: 'SET_DRAWER',
        setThemeColorMain: 'SET_THEME_COLOR_MAIN',
      }),
      userIconMenuItemClick (index) {
        this.userIconMenuItems[index].click.call(this)
      },
      removeNotification (index) {
        this.$store.commit('REMOVE_NOTIFICATION', index)
      },
    },
  }
</script>
