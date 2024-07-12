<template>
  <div>
    <base-material-card>
      <template #heading>
        <v-container
          fluid
          class="ma-0 pa-0"
        >
          <v-row>
            <v-col
              class="bg-secondary text-h3 font-weight-light"
            >
              Notifications
            </v-col>
          </v-row>
        </v-container>
      </template>
      <v-container
        fluid
        class="mt-4 mx-0"
      >
        <v-row>
          <v-col class="text-left">
            <span class="mr-2">
              Show only:
            </span>
            <v-btn
              class="mr-2"
              variant="outlined"
              @click="filter(true)"
            >
              <v-icon icon="mdi-email-open" />
            </v-btn>
            <v-btn
              variant="outlined"

              @click="filter(false)"
            >
              <v-icon icon="mdi-email" />
            </v-btn>
          </v-col>
          <v-col class="text-right">
            <v-btn
              v-if="notifications_unread.length > 0"
              variant="outlined"
              prepend-icon="mdi-email-open-outline"
              @click="notificationStore.markAsRead()"
            >
              Mark all
            </v-btn>
          </v-col>
        </v-row>
      </v-container>

      <v-divider />

      <v-data-table
        id="notificationsTable"
        :sort-by.sync="sortBy"
        :footer-props="{
          'items-per-page-options': [5, 10, 20, -1],
        }"
        :headers="DataTableHeaders"
        item-key="id"
        :items="notifications"
      >
        <template
          #item.read="{item}"
        >
          <v-icon v-if="item.read">
            mdi-email-open-outline
          </v-icon>
          <v-icon
            v-else
          >
            mdi-email
          </v-icon>
        </template>

        <template
          #item.date="{item}"
        >
          {{ getFormatedDate(item.date) }}
        </template>
      </v-data-table>
    </base-material-card>
  </div>
</template>

<script>
  import {useNotificationStore} from "@/stores/notificationStore";

  export default {
    name: 'NotificationsOverview',
    components: {},
    setup(){
      const notificationStore = useNotificationStore();
      return {notificationStore}
    },
    data: function () {
      return {
        sortBy: ['id'],
        sortDesc: true,
        filterRead: null,
      }
    },
    computed: {
      notifications () {
        return this.notificationStore.notifications
      },
      notifications_unread () {
        return this.notificationStore.notifications_unread
      },
      DataTableHeaders () {
        return [
          {
            value: 'read',
            filter: value => {
              if (this.filterRead === null) return true
              return value === this.filterRead
            },
          },
          { title: 'ID', value: 'id', sortable: true },
          { title: 'Category', value: 'category', sortable: true },
          { title: 'Date', value: 'date', sortable: true },
          { title: 'Text', value: 'text', sortable: false },
        ]
      },
    },
    methods: {
      getFormatedDate (time) {
        const options = { weekday: 'short', year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' }
        const location = 'de-DE'

        if (time !== null) {
          return new Date(time).toLocaleDateString(location, options)
        } else {
          return ''
        }
      },
      filter (value) {
        if (this.filterRead === value) {
          this.filterRead = null
        } else {
          this.filterRead = value
        }
      },
    },
  }

</script>

<style></style>
