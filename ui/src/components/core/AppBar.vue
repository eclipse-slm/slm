<template>
  <v-app-bar
    id="app-bar"
    absolute
    order="1"
    color="transparent"
    flat
    height="75"
  >
    <v-btn
      class="mr-3"
      elevation="1"
      size="small"
      :icon="value ? 'mdi-view-quilt' : 'mdi-dots-vertical'"
      @click="store.drawer = !drawer"
    />

    <v-toolbar-title
      class="hidden-sm-and-down font-weight-light"
    />

    <v-spacer />

    <div class="mx-3" />

    <v-menu
      v-model="menu"
      :close-on-content-click="false"
      location="bottom"
      start
      target="[y]"
      origin="top right"
      transition="scale-transition"
      content-class="v-settings"
      offset="8"
    >
      <template #activator="{ props }">
        <v-btn
          class="ml-2"
          min-width="0"
          variant="text"
          v-bind="props"
        >
          <v-icon icon="mdi-cog" />
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
                :model-value="currentTheme === 'dark'"
                class="ma-0 pa-0"
                color="secondary"
                hide-details
                @click="toggleTheme"
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
      location="bottom"
      start
      target="[y]"
      origin="top right"
    >
      <template #activator="{ props }">
        <v-btn
          v-if="notificationStore.notifications.length > 0"
          class="notification-btn ml-2"
          min-width="0"
          variant="text"
          v-bind="props"
          to="/notifications"
        >
          <v-badge
            v-if="notificationStore.notifications_unread.length"
            color="red"
            bordered
            :content="notificationStore.notifications_unread.length"
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
          variant="text"
        >
          <v-icon icon="mdi-bell" />
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
      location="bottom"
      start

      origin="top right"
      transition="scale-transition"
    >
      <template #activator="{ props }">
        <v-btn
          v-bind="props"
          id="user-menu-button"
          class="ml-2"
          min-width="0"
          variant="text"
        >
          <v-icon icon="mdi-account" />
        </v-btn>
      </template>

      <v-list>
        <v-list-item
          v-for="(item, index) in userIconMenuItems"
          :id="item.id"
          :key="index"
          :title="item.title"
          :append-icon="item.icon"
          @click="userIconMenuItemClick(index)"
        />
      </v-list>
    </v-menu>
  </v-app-bar>
</template>

<script>

import {useTheme} from "vuetify";
import {useNotificationStore} from "@/stores/notificationStore";
import {useStore} from "@/stores/store";

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
    setup(){
      const theme = useTheme();
      const currentTheme = theme.global.name;
      const toggleTheme = () => {
        theme.global.name = theme.global.current.dark ? 'light' : 'dark';
      };
      const notificationStore = useNotificationStore();
      const store = useStore();

      return {toggleTheme, currentTheme, notificationStore, store}
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
            this.$keycloak.logoutFn()
          },
        },
      ],
    }),

    computed: {
      drawer() {
        return this.store.drawer
      },
    },

    watch: {
      color (val) {
        this.$vuetify.theme.themes[this.isDark ? 'dark' : 'light'].primary = val

      },
    },

    methods: {
      userIconMenuItemClick (index) {
        this.userIconMenuItems[index].click.call(this)
      },
      removeNotification (index) {
        const notificationStore = useNotificationStore();
        // this.$store.commit('REMOVE_NOTIFICATION', index)
      },
    },
  }
</script>
