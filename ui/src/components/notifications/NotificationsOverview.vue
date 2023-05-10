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
              class="secondary text-h3 font-weight-light"
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
              outlined
              icon
              :text="filterRead !== true"
              @click="filter(true)"
            >
              <v-icon>
                mdi-email-open
              </v-icon>
            </v-btn>

            <v-btn
              outlined
              icon
              :text="filterRead !== false"
              @click="filter(false)"
            >
              <v-icon>
                mdi-email
              </v-icon>
            </v-btn>
          </v-col>
          <v-col class="text-right">
            <v-btn
              v-if="notifications_unread.length > 0"
              text
              outlined
              @click="markAsRead"
            >
              Mark all
              <v-icon
                right
              >
                mdi-email-open-outline
              </v-icon>
            </v-btn>
          </v-col>
        </v-row>
      </v-container>

      <v-divider />

      <v-data-table
        id="notificationsTable"
        :sort-by.sync="sortBy"
        :sort-desc.sync="sortDesc"
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
  import { mapActions, mapGetters } from 'vuex'

  export default {
    name: 'NotificationsOverview',
    components: {},
    data: function () {
      return {
        sortBy: 'id',
        sortDesc: true,
        filterRead: null,
      }
    },
    computed: {
      ...mapGetters([
        'notifications',
        'notifications_unread',
      ]),
      DataTableHeaders () {
        return [
          {
            value: 'read',
            filter: value => {
              if (this.filterRead === null) return true
              return value === this.filterRead
            },
          },
          { text: 'ID', value: 'id', sortable: true },
          { text: 'Category', value: 'category', sortable: true },
          { text: 'Date', value: 'date', sortable: true },
          { text: 'Text', value: 'text', sortable: false },
        ]
      },
    },
    methods: {
      ...mapActions(['markAsRead']),
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
