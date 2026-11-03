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
                icon="mdi-delete"
                size="small"
                color="error"
                :disabled="capabilitiesStore.installCountOfCapability(item.id) > 0"
                @click="capabilityToDelete = item"
              />
            </span>
          </template>
          Installed on {{ capabilitiesStore.installCountOfCapability(item.id) }} device(s). Uninstall it first.
        </v-tooltip>
      </template>

      <template #expanded-row="{ columns, item }">
        <tr>
          <td :colspan="columns.length">
            <div
              v-for="[actionType, action] in Object.entries(item.actions ?? {})"
              :key="actionType"
              class="py-1"
            >
              <strong>{{ actionType }}</strong>:
              repo {{ action.awxRepo }}, branch {{ action.awxBranch }}, playbook {{ action.playbook }}
            </div>
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

const capabilities = computed(() => capabilitiesStore.capabilities);

const headers = [
  { title: 'Logo', key: 'logo', sortable: false },
  { title: 'Name', key: 'name' },
  { title: 'Class', key: 'capabilityClass' },
  { title: 'Types', key: 'type', sortable: false },
  { title: 'Deployment types', key: 'supportedDeploymentTypes', sortable: false },
  { title: 'In use', key: 'installCount', sortable: false },
  { title: '', key: 'deleteAction', sortable: false },
];

async function onDeleteConfirmed() {
  const capability = capabilityToDelete.value;
  if (!capability) {
    return;
  }
  deleting.value = true;
  try {
    await capabilitiesStore.deleteCapability(capability.id);
    $toast.info('Capability deleted');
  } catch (e) {
    if (e instanceof CapabilityInUseError) {
      $toast.error(`Capability is installed on ${e.installCount} device(s) and cannot be deleted`);
    } else {
      logRequestError(e);
    }
  } finally {
    deleting.value = false;
    capabilityToDelete.value = null;
  }
}
</script>
