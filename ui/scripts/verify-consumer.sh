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
VITE_PID=""
cleanup() {
  if [ -n "$VITE_PID" ]; then kill "$VITE_PID" 2>/dev/null || true; wait "$VITE_PID" 2>/dev/null || true; fi
  rm -rf "$TMP"
}
trap cleanup EXIT

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

# Dev-mode smoke test: the dev server must start and serve the package entry plus an overridden module.
PORT="$(node -e "const s=require('net').createServer().listen(0,()=>{console.log(s.address().port);s.close()})")"
npx vite --port "$PORT" --strictPort >"$TMP/vite-dev.log" 2>&1 &
VITE_PID=$!
dev_fail() { echo "FAIL: dev smoke test: $1" >&2; echo "--- vite dev log ---" >&2; cat "$TMP/vite-dev.log" >&2; exit 1; }

BASE="http://localhost:$PORT"
ready=""
for _ in $(seq 1 60); do
  kill -0 "$VITE_PID" 2>/dev/null || dev_fail "vite dev server exited during startup"
  if curl -sf -o /dev/null "$BASE/src/main.ts"; then ready=1; break; fi
  sleep 1
done
[ -n "$ready" ] || dev_fail "dev server did not answer within 60s"

# Fetching a module also triggers dependency optimization / import analysis; give it a moment to surface crashes.
fetch() { curl -s -o "$TMP/body" -w '%{http_code}' "$BASE$1"; }
[ "$(fetch /src/main.ts)" = "200" ] || dev_fail "GET /src/main.ts did not return 200"
[ "$(fetch /src/components/core/Footer.vue)" = "200" ] || dev_fail "GET /src/components/core/Footer.vue did not return 200"
grep -q SLM_CONSUMER_FOOTER "$TMP/body" || dev_fail "Footer override marker SLM_CONSUMER_FOOTER missing from served Footer.vue"
sleep 3
kill -0 "$VITE_PID" 2>/dev/null || dev_fail "vite dev server died after serving requests"
if grep -qi "error during dependency optimization" "$TMP/vite-dev.log"; then dev_fail "dependency optimization error"; fi
kill "$VITE_PID"; wait "$VITE_PID" 2>/dev/null || true; VITE_PID=""
echo "OK: dev server smoke test passed"
