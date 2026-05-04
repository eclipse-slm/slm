<template>
  <v-card class="installer-panel overflow-visible">
    <v-card-text class="installer-panel__body">
      <v-form @submit.prevent="startJob">
        <v-row>
          <v-col cols="12" md="4">
            <v-text-field
              v-model.trim="form.slmHostname"
              label="SLM Hostname"
              placeholder="myhost.local"
              required
              prepend-inner-icon="mdi-web"
            />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field
              v-model.trim="form.slmIp"
              label="SLM IP"
              placeholder="172.17.0.1"
              required
              prepend-inner-icon="mdi-ip-network"
            />
          </v-col>
          <v-col cols="12" md="4">
            <v-text-field
              :model-value="FIXED_SLM_VERSION"
              label="SLM Version"
              prepend-inner-icon="mdi-tag-outline"
              readonly
            />
          </v-col>
        </v-row>

        <div class="d-flex align-center ga-3 mb-6 flex-wrap">
          <span class="text-subtitle-2">Job status</span>
          <v-chip :color="statusColor">{{ status }}</v-chip>
          <v-chip color="secondary" variant="outlined">{{ operationLabel }}</v-chip>
          <v-chip v-if="cancelRequested" color="warning" variant="outlined">Cancellation requested</v-chip>
        </div>

        <v-alert v-if="errorMessage" type="error" class="mb-4">
          {{ errorMessage }}
        </v-alert>

        <div class="d-flex ga-3 flex-wrap installer-action-row">
          <v-select
            v-model="form.logLevel"
            :items="logLevelOptions"
            item-title="title"
            item-value="value"
            label="Log Level"
            prepend-inner-icon="mdi-format-list-bulleted-square"
            density="comfortable"
            variant="outlined"
            hide-details
            class="installer-action-row__log-level"
          />

          <v-btn color="primary" type="submit" size="large" :loading="submitting" :disabled="submitting || canceling || !canStart || isBusy">
            <v-icon start>mdi-play-circle-outline</v-icon>
            Install
          </v-btn>

          <v-btn color="error" variant="outlined" size="large" :loading="uninstalling" :disabled="submitting || uninstalling || canceling || isBusy" @click="confirmUninstallDialog = true">
            <v-icon start>mdi-delete-outline</v-icon>
            Uninstall
          </v-btn>

          <v-btn v-if="isBusy" color="warning" variant="tonal" size="large" :loading="canceling" :disabled="canceling" @click="confirmCancelDialog = true">
            <v-icon start>mdi-stop-circle-outline</v-icon>
            Cancel
          </v-btn>
        </div>
      </v-form>
    </v-card-text>

    <v-card-text class="pt-0">
      <div class="installer-panel__heading installer-panel__heading--logs d-flex align-center ga-3 flex-wrap">
        <v-icon>mdi-text-box-search-outline</v-icon>
        <div>
          <div class="text-h6 font-weight-medium">Log</div>
        </div>
      </div>

      <div class="installer-log-surface mt-6">
        <div ref="logContainer" class="log-window">
            <div v-if="restoringJob" class="text-medium-emphasis">Checking active job…</div>
            <div v-else-if="events.length === 0" class="text-medium-emphasis">No events available yet.</div>
          <div v-for="(event, index) in events" :key="`${event.timestamp}-${index}`" class="log-line">
            <span class="log-time">{{ event.timestamp }}</span>
            <span class="log-type">{{ event.type }}</span>
            <span class="log-message">{{ event.message }}</span>
          </div>
        </div>
      </div>
    </v-card-text>
  </v-card>

  <ConfirmDialog
    v-model="confirmUninstallDialog"

    title="Uninstall"
    text="Do you want to delete this SLM installation?"
    confirm-text="Delete"
    cancel-text="Cancel"
    confirm-color="error"
    :loading="uninstalling"
    @confirm="startUninstall"
    @cancel="confirmUninstallDialog = false"
  />

  <ConfirmDialog
    v-model="confirmCancelDialog"
    title="Cancel"
    text="Do you want to cancel the current job?"
    confirm-text="Cancel"
    cancel-text="No"
    confirm-color="warning"
    :loading="canceling"
    @confirm="cancelJob"
    @cancel="confirmCancelDialog = false"
  />
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import ConfirmDialog from './ConfirmDialog.vue';
import { type JobEvent, useJobStream } from '../composables/useJobStream';
import { getApiBaseUrl } from '../config/runtimeConfig';

type JobStatus = 'idle' | 'queued' | 'running' | 'successful' | 'failed' | 'canceled';
type JobOperation = 'install' | 'uninstall';
type LogLevel = 'standard' | 'detailed' | 'debug' | 'trace';
type JobResponse = {
  operation: JobOperation;
  status: JobStatus;
  startTime?: string | null;
  endTime?: string | null;
  exitCode?: number | null;
  cancelRequested: boolean;
};
type JobLogsResponse = {
  logs: JobEvent[];
};

const FIXED_SLM_VERSION = '1.5.0-SNAPSHOT';

const form = reactive({
  slmHostname: '',
  slmIp: '',
  logLevel: 'standard' as LogLevel
});

const logLevelOptions: Array<{ title: string; value: LogLevel }> = [
  { title: 'Standard', value: 'standard' },
  { title: 'Detailed', value: 'detailed' },
  { title: 'Debug', value: 'debug' },
  { title: 'Trace', value: 'trace' }
];

const canStart = computed(() => {
  return form.slmHostname.length > 0 && form.slmIp.length > 0;
});

const statusColor = computed(() => {
  switch (status.value) {
    case 'successful':
      return 'success';
    case 'canceled':
      return 'warning';
    case 'failed':
      return 'error';
    case 'running':
      return 'warning';
    case 'queued':
      return 'info';
    default:
      return 'secondary';
  }
});

const operation = ref<JobOperation>('install');
const cancelRequested = ref(false);
const uninstalling = ref(false);
const canceling = ref(false);
const restoringJob = ref(false);
const confirmUninstallDialog = ref(false);
const confirmCancelDialog = ref(false);

const isBusy = computed(() => status.value === 'queued' || status.value === 'running');

const operationLabel = computed(() => {
  if (status.value === 'idle') {
    return 'No active job';
  }
  return operation.value === 'uninstall' ? 'Uninstallation' : 'Installation';
});

const status = ref<JobStatus>('idle');
const submitting = ref(false);
const events = ref<JobEvent[]>([]);
const errorMessage = ref('');
const logContainer = ref<HTMLDivElement | null>(null);

const { connect, disconnect } = useJobStream(getApiBaseUrl);

function resetJobState(nextOperation: JobOperation) {
  disconnect();
  operation.value = nextOperation;
  status.value = 'idle';
  cancelRequested.value = false;
  events.value = [];
  errorMessage.value = '';
}

function applyJobPayload(payload: JobResponse) {
  status.value = payload.status;
  operation.value = payload.operation === 'uninstall' ? 'uninstall' : 'install';
  cancelRequested.value = payload.cancelRequested;
}

function connectToJob() {
  connect(
    (event) => {
      events.value.push(event);
      if (event.type === 'status') {
        const nextStatus = event.raw?.newStatus;
        const nextOperation = event.raw?.operation;
        const nextCancelRequested = event.raw?.cancelRequested;

        if (typeof nextStatus === 'string') {
          status.value = nextStatus as JobStatus;
        }
        if (typeof nextOperation === 'string') {
          operation.value = nextOperation as JobOperation;
        }
        if (typeof nextCancelRequested === 'boolean') {
          cancelRequested.value = nextCancelRequested;
        }
      }
    },
    (message) => {
      errorMessage.value = message;
    }
  );
}

async function restoreLastJob() {
  restoringJob.value = true;
  errorMessage.value = '';

  try {
    const apiBaseUrl = getApiBaseUrl();
    const [jobResponse, logsResponse] = await Promise.all([
      fetch(`${apiBaseUrl}/api/job`),
      fetch(`${apiBaseUrl}/api/logs`)
    ]);

    if (!jobResponse.ok) {
      throw new Error(`Failed to restore job state (HTTP ${jobResponse.status})`);
    }
    if (!logsResponse.ok) {
      throw new Error(`Failed to restore job logs (HTTP ${logsResponse.status})`);
    }

    const jobPayload = (await jobResponse.json()) as JobResponse;
    const logsPayload = (await logsResponse.json()) as JobLogsResponse;

    applyJobPayload(jobPayload);
    events.value = logsPayload.logs;

    if (jobPayload.status === 'queued' || jobPayload.status === 'running') {
      connectToJob();
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Unexpected error while checking the active job.';
  } finally {
    restoringJob.value = false;
  }
}

async function startJob() {
  if (!canStart.value) {
    return;
  }

  submitting.value = true;
  resetJobState('install');

  try {
    const apiBaseUrl = getApiBaseUrl();
    const response = await fetch(`${apiBaseUrl}/api/install`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        slmHostname: form.slmHostname,
        slmIp: form.slmIp,
        logLevel: form.logLevel
      })
    });

    if (!response.ok) {
      errorMessage.value = `Failed to start installation (HTTP ${response.status})`;
      status.value = 'failed';
      return;
    }

    const payload = (await response.json()) as JobResponse;
    applyJobPayload(payload);
    connectToJob();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Unexpected error while starting job.';
    status.value = 'failed';
  } finally {
    submitting.value = false;
  }
}

async function startUninstall() {
  confirmUninstallDialog.value = false;
  uninstalling.value = true;
  resetJobState('uninstall');

  try {
    const apiBaseUrl = getApiBaseUrl();
    const response = await fetch(`${apiBaseUrl}/api/uninstall`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        logLevel: form.logLevel
      })
    });

    if (!response.ok) {
      errorMessage.value = `Failed to start uninstallation (HTTP ${response.status})`;
      status.value = 'failed';
      return;
    }

    const payload = (await response.json()) as JobResponse;
    applyJobPayload(payload);
    connectToJob();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Unexpected error while starting uninstallation.';
    status.value = 'failed';
  } finally {
    uninstalling.value = false;
  }
}

async function cancelJob() {
  confirmCancelDialog.value = false;
  canceling.value = true;
  errorMessage.value = '';

  try {
    const apiBaseUrl = getApiBaseUrl();
    const response = await fetch(`${apiBaseUrl}/api/cancel`, {
      method: 'POST'
    });

    if (!response.ok) {
      errorMessage.value = `Failed to cancel job (HTTP ${response.status})`;
      return;
    }

    const payload = (await response.json()) as JobResponse;
    applyJobPayload(payload);
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Unexpected error while canceling job.';
  } finally {
    canceling.value = false;
  }
}

watch(
  () => events.value.length,
  async () => {
    await nextTick();
    if (logContainer.value) {
      logContainer.value.scrollTop = logContainer.value.scrollHeight;
    }
  }
);


onMounted(() => {
  void restoreLastJob();
});

onBeforeUnmount(() => {
  disconnect();
});
</script>

<style scoped>
.log-window {
  border-radius: 6px;
  background: #f8fafc;
  color: #3c4858;
  min-height: 280px;
  max-height: 380px;
  overflow-y: auto;
  font-family: Consolas, 'Courier New', monospace;
  font-size: 0.85rem;
  padding: 0.75rem;
}

.log-line {
  display: grid;
  grid-template-columns: minmax(220px, 260px) minmax(70px, 90px) 1fr;
  gap: 0.5rem;
  padding: 0.125rem 0;
}

.log-time {
  color: #00a0e3;
}

.log-type {
  color: #17a6a6;
  text-transform: uppercase;
  font-weight: 600;
}

.log-message {
  word-break: break-word;
}

.installer-action-row {
  margin-bottom: 0.75rem;
}

.installer-action-row__log-level {
  min-width: 210px;
  max-width: 260px;
}
</style>

