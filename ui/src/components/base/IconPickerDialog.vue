<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import CustomDialog from '@/components/base/CustomDialog.vue'
import { filterIcons, loadMdiIcons } from '@/utils/mdiIcons'
import type { MdiIconMeta } from '@/utils/mdiIcons'

const ICONS_PER_ROW = 8

const props = defineProps<{
  show: boolean
  modelValue?: string
}>()

const emit = defineEmits<{
  (e: 'selected', icon: string): void
  (e: 'canceled'): void
}>()

const icons = ref<MdiIconMeta[]>([])
const loading = ref(false)
const loadFailed = ref(false)
const searchInput = ref<string | null>('')
const query = ref('')
const manualName = ref('')

let debounceTimer: ReturnType<typeof setTimeout> | undefined

watch(searchInput, (value) => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    query.value = value ?? ''
  }, 150)
})

onBeforeUnmount(() => clearTimeout(debounceTimer))

async function load() {
  loading.value = true
  loadFailed.value = false
  try {
    icons.value = await loadMdiIcons()
  } catch {
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

watch(
  () => props.show,
  (visible) => {
    if (!visible) return
    searchInput.value = ''
    query.value = ''
    manualName.value = ''
    if (icons.value.length === 0) load()
  },
  { immediate: true },
)

const rows = computed(() => {
  const filtered = filterIcons(icons.value, query.value)
  const result: { id: number; icons: MdiIconMeta[] }[] = []
  for (let i = 0; i < filtered.length; i += ICONS_PER_ROW) {
    result.push({ id: i, icons: filtered.slice(i, i + ICONS_PER_ROW) })
  }
  return result
})

function select(name: string) {
  emit('selected', `mdi-${name}`)
}

function useManualName() {
  const name = manualName.value.trim()
  if (!name) return
  emit('selected', name.startsWith('mdi-') ? name : `mdi-${name}`)
}
</script>

<template>
  <CustomDialog
    :show="show"
    title="Select icon"
    width="720"
    hide-actions
    @canceled="emit('canceled')"
  >
    <template #content>
      <div v-if="loading" class="d-flex justify-center pa-8">
        <v-progress-circular indeterminate color="primary" />
      </div>
      <div v-else-if="loadFailed">
        <v-alert type="warning" variant="tonal" class="mb-4">
          Icon list could not be loaded. Enter an icon name instead.
        </v-alert>
        <div class="d-flex align-center ga-2">
          <v-text-field
            v-model="manualName"
            placeholder="mdi-docker"
            variant="outlined"
            density="compact"
            hide-details
            @keyup.enter="useManualName"
          />
          <v-btn color="primary" :disabled="!manualName.trim()" @click="useManualName">Use</v-btn>
        </div>
      </div>
      <div v-else>
        <v-text-field
          v-model="searchInput"
          label="Search icons"
          prepend-inner-icon="mdi-magnify"
          variant="outlined"
          density="compact"
          clearable
          autofocus
          hide-details
          class="mb-4"
        />
        <v-virtual-scroll :items="rows" :height="320" :item-height="48" item-key="id">
          <template #default="{ item }">
            <div class="icon-row">
              <v-btn
                v-for="icon in item.icons"
                :key="icon.name"
                :icon="`mdi-${icon.name}`"
                :color="modelValue === `mdi-${icon.name}` ? 'primary' : undefined"
                :variant="modelValue === `mdi-${icon.name}` ? 'flat' : 'text'"
                @click="select(icon.name)"
              >
                <v-icon :icon="`mdi-${icon.name}`" />
                <v-tooltip activator="parent" location="top">mdi-{{ icon.name }}</v-tooltip>
              </v-btn>
            </div>
          </template>
        </v-virtual-scroll>
        <div v-if="rows.length === 0" class="text-center text-medium-emphasis pa-4">
          No icons found
        </div>
      </div>
    </template>
  </CustomDialog>
</template>

<style scoped>
.icon-row {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  justify-items: center;
  height: 48px;
  align-items: center;
}
</style>
