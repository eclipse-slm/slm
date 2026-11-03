<template>
  <base-material-card>
    <template #heading>
      <overview-heading text="Capabilities" />
    </template>

    <no-item-available-note
      v-if="capabilities.length === 0"
      item="Capability"
    />

    <v-data-table
      v-else
      :headers="headers"
      :items="capabilities"
      item-value="id"
      show-expand
      v-model:expanded="expanded"
      @click:row="onRowClick"
    >
      <template #item.logo="{ item }">
        <v-icon>{{ item.logo }}</v-icon>
      </template>

      <template #item.capabilityClass="{ item }">
        {{ item.capabilityClass === 'DeploymentCapability' ? 'Deployment' : 'Base configuration' }}
      </template>

      <template #item.type="{ item }">
        <v-chip
          v-for="type in item.type"
          :key="type"
          class="mr-1"
          size="small"
        >
          {{ type }}
        </v-chip>
      </template>

      <template #item.supportedDeploymentTypes="{ item }">
        <template v-if="item.capabilityClass === 'DeploymentCapability' && item.supportedDeploymentTypes?.length">
          <v-chip
            v-for="deploymentType in item.supportedDeploymentTypes"
            :key="deploymentType"
            class="mr-1"
            size="small"
          >
            {{ deploymentType }}
          </v-chip>
        </template>
        <template v-else>
          –
        </template>
      </template>

      <template #item.installCount="{ item }">
        {{ capabilitiesStore.installCountOfCapability(item.id) }} device(s)
      </template>

      <template #item.deleteAction="{ item }">
        <v-tooltip
          :disabled="capabilitiesStore.installCountOfCapability(item.id) === 0"
          location="top"
        >
          <template #activator="{ props: tooltipProps }">
            <span v-bind="tooltipProps">
              <v-btn
                prepend-icon="mdi-delete"
                size="small"
                color="error"
                :disabled="capabilitiesStore.installCountOfCapability(item.id) > 0"
                @click.stop="capabilityToDelete = item"
              >
                Delete
              </v-btn>
            </span>
          </template>
          Installed on {{ capabilitiesStore.installCountOfCapability(item.id) }} device(s). Uninstall it first.
        </v-tooltip>
      </template>

      <template #expanded-row="{ columns, item }">
        <tr>
          <td
            :colspan="columns.length"
            class="expanded-actions-cell"
          >
            <table class="action-details-table">
              <thead>
                <tr>
                  <th>Action type</th>
                  <th>Repository</th>
                  <th>Branch</th>
                  <th>Playbook</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="[actionType, action] in Object.entries(item.actions ?? {})"
                  :key="actionType"
                >
                  <td>{{ actionType }}</td>
                  <td>{{ action.awxRepo }}</td>
                  <td>{{ action.awxBranch }}</td>
                  <td>{{ action.playbook }}</td>
                </tr>
              </tbody>
            </table>
          </td>
        </tr>
      </template>
    </v-data-table>

    <ConfirmDialog
      :show="capabilityToDelete !== null"
      attention
      title="Delete capability"
      :text="`Delete capability '${capabilityToDelete?.name}'? Its AWX job templates and projects are removed too.`"
      confirm-button-label="Delete"
      cancel-button-label="Cancel"
      :confirm-loading="deleting"
      @canceled="capabilityToDelete = null"
      @confirmed="onDeleteConfirmed"
    />
  </base-material-card>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useToast } from 'vue-toast-notification';
import logRequestError from '@/api/restApiHelper';
import ConfirmDialog from '@/components/base/ConfirmDialog.vue';
import NoItemAvailableNote from '@/components/base/NoItemAvailableNote.vue';
import OverviewHeading from '@/components/base/OverviewHeading.vue';
import { CapabilityInUseError, useCapabilitiesStore } from '@/stores/capabilitiesStore';

const $toast = useToast();
const capabilitiesStore = useCapabilitiesStore();

const capabilityToDelete = ref<any>(null);
const deleting = ref(false);
const expanded = ref<Array<string | number>>([]);

const capabilities = computed(() => capabilitiesStore.capabilities);

const headers = [
  { title: '', key: 'data-table-expand', sortable: false },
  { title: 'Logo', key: 'logo', sortable: false },
  { title: 'Name', key: 'name' },
  { title: 'Class', key: 'capabilityClass' },
  { title: 'Types', key: 'type', sortable: false },
  { title: 'Deployment types', key: 'supportedDeploymentTypes', sortable: false },
  { title: 'In use', key: 'installCount', sortable: false },
  { title: '', key: 'deleteAction', sortable: false },
];

function onRowClick(_event: MouseEvent, { item }: { item: { id: string | number } }) {
  expanded.value = expanded.value.includes(item.id)
    ? expanded.value.filter((id) => id !== item.id)
    : [...expanded.value, item.id];
}

async function onDeleteConfirmed() {
  const capability = capabilityToDelete.value;
  if (!capability) {
    return;
  }
  deleting.value = true;
  try {
    await capabilitiesStore.deleteCapability(capability.id);
    $toast.success('Capability deleted');
  } catch (e) {
    if (e instanceof CapabilityInUseError) {
      $toast.error(`Capability is installed on ${e.installCount} device(s) and cannot be deleted`);
    } else {
      logRequestError(e);
      $toast.error('Failed to delete capability');
    }
  } finally {
    deleting.value = false;
    capabilityToDelete.value = null;
  }
}
</script>

<style scoped>
.action-details-table {
  border-collapse: collapse;
  max-width: 100%;
  margin: 8px 0 8px 48px;
  text-align: left;
  white-space: nowrap;
  width: auto;
}

.expanded-actions-cell {
  background-color: rgba(var(--v-theme-on-surface), 0.04);
}

.action-details-table th,
.action-details-table td {
  border: 0;
  padding: 4px 8px;
}

.action-details-table th {
  font-weight: 700;
}
</style>
