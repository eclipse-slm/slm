export function getApiBaseUrl(): string {
  // Force same-origin API routing through the installer UI host (Nginx /api proxy).
  return window.location.origin;
}

