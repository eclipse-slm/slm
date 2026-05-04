import { ref } from 'vue';

export type JobEvent = {
  jobId: string;
  type: 'status' | 'log' | 'task' | 'host' | 'summary' | 'error';
  timestamp: string;
  level: 'debug' | 'info' | 'warn' | 'error';
  message: string;
  task?: string;
  host?: string;
  raw: Record<string, unknown>;
};

function toWsUrl(apiBaseUrl: string, path: string): string {
  const normalized = apiBaseUrl.trim().replace(/\/+$/, '');

  if (normalized.startsWith('http://') || normalized.startsWith('https://')) {
    const wsBase = normalized.startsWith('https://')
      ? normalized.replace(/^https:/, 'wss:')
      : normalized.replace(/^http:/, 'ws:');
    return `${wsBase}${path}`;
  }

  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const relativeBase = normalized ? (normalized.startsWith('/') ? normalized : `/${normalized}`) : '';
  return `${wsProtocol}//${window.location.host}${relativeBase}${path}`;
}

export function useJobStream(apiBaseUrl: string | (() => string)) {
  const socket = ref<WebSocket | null>(null);
  const connected = ref(false);

  function resolveApiBaseUrl(): string {
    return typeof apiBaseUrl === 'function' ? apiBaseUrl() : apiBaseUrl;
  }

  function connect(onEvent: (event: JobEvent) => void, onError: (message: string) => void) {
    disconnect();
    const wsUrl = toWsUrl(resolveApiBaseUrl(), '/api/stream');
    const ws = new WebSocket(wsUrl);
    socket.value = ws;

    ws.onopen = () => {
      connected.value = true;
    };

    ws.onmessage = (messageEvent) => {
      try {
        const parsed = JSON.parse(messageEvent.data) as JobEvent;
        onEvent(parsed);
      } catch {
        onError('Invalid event payload from server.');
      }
    };

    ws.onerror = () => {
      onError('WebSocket connection failed.');
    };

    ws.onclose = () => {
      connected.value = false;
      socket.value = null;
    };
  }

  function disconnect() {
    if (socket.value) {
      socket.value.close();
      socket.value = null;
    }
    connected.value = false;
  }

  return {
    connected,
    connect,
    disconnect
  };
}

