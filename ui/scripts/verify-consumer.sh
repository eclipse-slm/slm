#!/usr/bin/env bash
# Packs @eclipse-slm/ui, builds test-consumer against the tarball in an isolated temp dir and checks the override markers.
# The consumer is built OUTSIDE the repo so that undeclared dependencies of the package cannot resolve via ui/node_modules.
# The consumer install is deliberately a fresh resolution (no lock file): it mirrors what real consumers get and
# catches breakage from newer dependency versions.
set -euo pipefail
UI_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SERVICES="resource-management platform-management service-management notification-service"

for s in $SERVICES; do
  if [ ! -f "$UI_DIR/src/api/$s/client/index.ts" ]; then
    echo "FAIL: generated API client missing: $UI_DIR/src/api/$s/client/index.ts (generate it with openapi-generator)" >&2
    exit 1
  fi
done

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

cd "$UI_DIR"
TARBALL="$(npm pack --json --pack-destination "$TMP" | node -e "process.stdout.write(JSON.parse(require('fs').readFileSync(0,'utf8'))[0].filename)")"
mv "$TMP/$TARBALL" "$TMP/eclipse-slm-ui.tgz"

mkdir "$TMP/consumer"
(cd test-consumer && tar --exclude=node_modules --exclude=dist --exclude=package-lock.json -cf - .) | tar -xf - -C "$TMP/consumer"
for s in $SERVICES; do
  mkdir -p "$TMP/consumer/src/api/$s"
  cp -r "$UI_DIR/src/api/$s/client" "$TMP/consumer/src/api/$s/client"
done

cd "$TMP/consumer"
npm install
npx vite build
for marker in SLM_CONSUMER_FOOTER SLM_CONSUMER_PAGE '#123456' SLM_CONSUMER_BASE_CARD; do
  if ! grep -rq -- "$marker" dist/; then
    echo "FAIL: marker $marker not found in consumer dist" >&2
    exit 1
  fi
done
echo "OK: all consumer markers found"
