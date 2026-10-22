#!/usr/bin/env bash
# Packs @eclipse-slm/ui, builds test-consumer against the tarball and checks the override markers.
set -euo pipefail
UI_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$UI_DIR"
rm -f eclipse-slm-ui.tgz eclipse-slm-ui-*.tgz
TARBALL="$(npm pack --silent | tail -n 1)"
mv "$TARBALL" eclipse-slm-ui.tgz
cd test-consumer
rm -rf node_modules dist package-lock.json
npm install
npx vite build
for marker in SLM_CONSUMER_FOOTER SLM_CONSUMER_PAGE '#123456' SLM_CONSUMER_BASE_CARD; do
  if ! grep -rq -- "$marker" dist/; then
    echo "FAIL: marker $marker not found in test-consumer/dist" >&2
    exit 1
  fi
done
echo "OK: all consumer markers found"
