<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PlatformManagementClient from '@/api/platform-management/platform-management-client'
import type { CredentialReadDTO } from '@/api/platform-management/client'
import CredentialDisplay from '@/components/credentials/CredentialDisplay.vue'
import { useToast } from 'vue-toast-notification'
import ConfirmDialog from '@/components/base/ConfirmDialog.vue'
import RowWithLabel from '@/components/base/RowWithLabel.vue'
import CredentialCreateDialog from '@/components/credentials/CredentialCreateDialog.vue'
import { useUserStore } from '@/stores/userStore'

const credentials = ref<CredentialReadDTO[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const selectedScope = ref<string | null>(null)
const deletingId = ref<string | null>(null)
const showConfirmDeleteDialog = ref(false)
const credentialToDelete = ref<CredentialReadDTO | null>(null)
const searchQuery = ref('')
const showCreateDialog = ref(false)

const tableHeaders = [
  { title: 'Type', value: 'data.credentialDataType' },
  { title: 'Scopes', value: 'scopes' },
  { title: 'ID', value: 'id' },
]

const $toast = useToast()
const userStore = useUserStore()

onMounted(() => {
  loadUserCredentials()
})

function loadUserCredentials() {
  loading.value = true
  error.value = null
  PlatformManagementClient.credentialsApi.getCredentialsOfUser()
    .then((response) => {
      credentials.value = response.data ?? []
    })
    .catch((err) => {
      error.value = err?.message ?? 'Error loading credentials'
    })
    .finally(() => {
      loading.value = false
    })
}

const scopeOptions = computed(() => {
  const scopes = credentials.value.flatMap((c) => c.scopesRaw ?? [])
  return Array.from(new Set(scopes))
})

const filteredCredentials = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()
  const byScope = !selectedScope.value
    ? credentials.value
    : credentials.value.filter((c) => c.scopesRaw?.includes(selectedScope.value as string))

  if (!query) return byScope
  return byScope.filter((c) => {
    const scopes = (c.scopesRaw ?? []).join(' ').toLowerCase()
    const type = (c.data as any)?.credentialDataType?.toString().toLowerCase() ?? ''
    const username = (c.data as any)?.username?.toString().toLowerCase() ?? ''
    const id = c.id?.toString().toLowerCase() ?? ''
    return [scopes, type, username, id].some((value) => value.includes(query))
  })
})

const createScopes = computed(() => (selectedScope.value ? [selectedScope.value] : []))
const createEntityLinks = computed(() => (userStore.fullPathUserGroupId
  ? [{ entityType: 'USER_GROUP', entityId: userStore.fullPathUserGroupId }]
  : []
))

function getEntityLinks(credential: CredentialReadDTO) {
  return ((credential as any).entityLinks ?? []) as Array<{ entityType: string; entityId: string }>
}

function isDeleteDisabled(credential: CredentialReadDTO) {
  const links = getEntityLinks(credential)
  return links.length === 0 || links.some((link) => link.entityType !== 'USER_GROUP')
}

function requestDeleteCredential(credential: CredentialReadDTO) {
  credentialToDelete.value = credential
  showConfirmDeleteDialog.value = true
}

async function confirmDeleteCredential() {
  if (!credentialToDelete.value) return
  const credentialId = credentialToDelete.value.id
  deletingId.value = credentialId
  try {
    await PlatformManagementClient.credentialsApi.deleteCredential(credentialId)
    await loadUserCredentials()
    $toast.success('Credential deleted')
  } catch (err) {
    console.error('Error deleting credential', err)
    $toast.error('Error deleting credential')
  } finally {
    deletingId.value = null
    showConfirmDeleteDialog.value = false
    credentialToDelete.value = null
  }
}

const confirmDeleteLoading = computed(() => deletingId.value !== null)
</script>

<template>
  <base-material-card color="secondary">
    <template #heading>
      <div class="text-h3 font-weight-light">
        User Credentials
      </div>
    </template>
    <div>
      <v-alert v-if="error" type="error" variant="tonal" density="comfortable">
        {{ error }}
      </v-alert>

      <div v-else>
        <div class="d-flex justify-center my-6" v-if="loading">
          <v-progress-circular indeterminate color="primary" />
        </div>

        <div v-else>
          <v-row class="align-center my-8">
            <v-col cols="5">
              <v-text-field
                v-model="searchQuery"
                label="Search"
                density="comfortable"
                clearable
                hide-details
              />
            </v-col>
            <v-col cols="5">
              <v-select
                v-model="selectedScope"
                :items="scopeOptions"
                label="Credential Scope"
                density="comfortable"
                clearable
                hide-details
                placeholder="No filter (show all)"
              />
            </v-col>
            <v-col cols="2" class="d-flex justify-end pl-8">
              <v-btn
                color="secondary"
                @click="showCreateDialog = true"
              >
                <v-icon color="white">mdi-plus</v-icon>
              </v-btn>
            </v-col>
          </v-row>

          <v-alert v-if="!filteredCredentials.length" type="info" variant="tonal" density="comfortable">
            No credentials found.
          </v-alert>

          <v-data-table
            v-else
            :items="filteredCredentials"
            :headers="tableHeaders"
            item-key="id"
            item-value="id"
            show-expand
            class="elevation-1"
          >
            <template #item.scopes="{ item }">
              <v-chip-group>
                <v-chip
                  v-for="scope in (item as any).scopesRaw ?? []"
                  :key="scope"
                  size="small"
                  color="secondary"
                  variant="tonal"
                  class="ma-1"
                >
                  {{ scope }}
                </v-chip>
              </v-chip-group>
            </template>

            <template #expanded-row="{ item }">
              <tr>
                <td colspan="3">
                  <v-card flat>
                    <v-card-text>
                      <div class="d-flex justify-end mb-4">
                        <v-tooltip v-if="isDeleteDisabled(item as any)" location="top">
                          <template #activator="{ props: tp }">
                            <span v-bind="tp" class="d-inline-flex">
                              <v-btn
                                color="secondary"
                                size="small"
                                :loading="deletingId === (item as any).id"
                                :disabled="true"
                              >
                                <v-icon icon="mdi-delete" color="white" />
                              </v-btn>
                            </span>
                          </template>
                          <span>Credential cannot be deleted if it is linked to other entities than user groups.</span>
                        </v-tooltip>

                        <v-btn
                          v-else
                          color="secondary"
                          size="small"
                          :loading="deletingId === (item as any).id"
                          :disabled="deletingId !== null"
                          @click="requestDeleteCredential(item as any)"
                        >
                          <v-icon icon="mdi-delete" color="white" />
                        </v-btn>
                      </div>

                      <credential-display :credential="item" />

                      <RowWithLabel label="Entity Links" :divider="false">
                        <template #content>
                          <div v-if="getEntityLinks(item as any).length">
                            <v-chip
                              v-for="link in getEntityLinks(item as any)"
                              :key="`${link.entityType}-${link.entityId}`"
                              size="small"
                              class="ma-1"
                              variant="outlined"
                            >
                              {{ link.entityType }}: {{ link.entityId }}
                            </v-chip>
                          </div>
                          <div v-else>
                            No entity links.
                          </div>
                        </template>
                      </RowWithLabel>
                    </v-card-text>
                  </v-card>
                </td>
              </tr>
            </template>
          </v-data-table>
        </div>
      </div>
    </div>
  </base-material-card>

  <CredentialCreateDialog
    :show="showCreateDialog"
    :scopes="createScopes"
    :entity-links="createEntityLinks"
    @created="showCreateDialog = false; loadUserCredentials()"
    @canceled="showCreateDialog = false"
  />

  <ConfirmDialog
    :show="showConfirmDeleteDialog"
    title="Delete credential"
    :text="`Do you want to delete this credential?`"
    confirm-button-label="Delete"
    cancel-button-label="Cancel"
    :attention="true"
    :confirm-loading="confirmDeleteLoading"
    @canceled="showConfirmDeleteDialog = false"
    @confirmed="confirmDeleteCredential"
  />
</template>
