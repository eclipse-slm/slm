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
              @click="notificationStore.markAllAsRead()"
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
          {{ getFormatedDate(item.timestamp) }}
        </template>

        <template
          #item.text="{item}"
        >
          {{ getNotificationText(item) }}
        </template>
      </v-data-table>
    </base-material-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useNotificationStore } from '@/stores/notificationStore'
import NotificationTextGenerator from '@/utils/notificationTextGenerator'

const { t } = useI18n()
const notificationStore = useNotificationStore()

const sortBy = ref(['id'])
const sortDesc = ref(true)
const filterRead = ref(null)

const notifications = computed(() => notificationStore.notifications)
const notifications_unread = computed(() => notificationStore.notifications_unread)

const DataTableHeaders = computed(() => [
  {
    value: 'read',
    filter: value => {
      if (filterRead.value === null) return true
      return value === filterRead.value
    },
  },
  { title: 'ID', value: 'id', sortable: true },
  { title: 'Category', value: 'category', sortable: true },
  { title: 'Sub Category', value: 'subCategory', sortable: true },
  { title: 'Event Type', value: 'eventType', sortable: true },
  { title: 'Date', value: 'timestamp', sortable: true },
  { title: 'Text', value: 'text', sortable: false },
])

function getFormatedDate(time) {
  const options = { weekday: 'short', year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' }
  const location = 'de-DE'
  if (time !== null) {
    return new Date(time).toLocaleDateString(location, options)
  } else {
    return ''
  }
}

function getNotificationText(item) {
  return NotificationTextGenerator.generateLocalizedText(item, t)
}

function filter(value) {
  if (filterRead.value === value) {
    filterRead.value = null
  } else {
    filterRead.value = value
  }
}
</script>

<style>

</style>
