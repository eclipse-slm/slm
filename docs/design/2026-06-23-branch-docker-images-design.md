# Branch Docker Images – Implementation Design

**Goal:** Docker Images für `feature/*`- und `fix/*`-Branches automatisch bauen, mit dem sanitierten Branch-Namen taggen, pushen und beim Branch-Delete wieder aufräumen.

**Architecture:** Minimale Erweiterung von `build-core.yml` (zwei neue Steps im bestehenden Build-Job) plus ein neuer `cleanup-branch-images.yml`-Workflow.

**Tech Stack:** GitHub Actions, fabric8 docker-maven-plugin, GitHub Container Registry (ghcr.io), GitHub API via `gh` CLI

---

## Kontext

Das Projekt nutzt den **fabric8 docker-maven-plugin**. Das `build`-Goal läuft in der `package`-Phase (lokal im Docker-Daemon des Runners), das `push`-Goal in der `deploy`-Phase. Nach `mvn verify` liegen alle Images also bereits lokal vor — mit dem Tag `${project.version}` (z.B. `1.5.0-SNAPSHOT`). Es ist kein zweiter Maven-Build nötig.

Aktuell pushen `feature/*`- und `fix/*`-Branches keine Images (nur `verify`). `develop` und `main` pushen via `mvn deploy`.

---

## Tag-Format

Branch-Name wird per `tr '/' '-'` sanitiert:

| Branch | Docker-Tag |
|---|---|
| `feature/refactor-service-mgmt` | `feature-refactor-service-mgmt` |
| `fix/login-bug` | `fix-login-bug` |

---

## Änderungen an `build-core.yml`

### 1. `prepare`-Job: zwei neue Outputs

```yaml
outputs:
  should_branch_deploy: ${{ steps.evaluate.outputs.should_branch_deploy }}
  branch_tag: ${{ steps.evaluate.outputs.branch_tag }}
```

Im Shell-Script des `evaluate`-Steps:

```bash
SHOULD_BRANCH_DEPLOY=false
BRANCH_TAG=""

if [[ "$GITHUB_REF_NAME" =~ ^feature/ || "$GITHUB_REF_NAME" =~ ^fix/ ]]; then
  SHOULD_BRANCH_DEPLOY=true
  BRANCH_TAG=$(echo "$GITHUB_REF_NAME" | tr '/' '-')
fi

echo "should_branch_deploy=$SHOULD_BRANCH_DEPLOY" >> "$GITHUB_OUTPUT"
echo "branch_tag=$BRANCH_TAG" >> "$GITHUB_OUTPUT"
```

Die bestehende Logik bleibt unverändert — feature/fix-Branches laufen weiterhin durch `SHOULD_VERIFY=true`.

### 2. `build`-Job: Maven-Settings für GHCR-Login

Der bestehende `maven-settings-action`-Step ist auf `should_snapshot_deploy` und `should_release_deploy` eingeschränkt. Für Branch-Deploys wird ein eigener GHCR-Login-Step hinzugefügt:

```yaml
- name: Log in to GitHub Container Registry (branch deploy)
  if: needs.prepare.outputs.should_branch_deploy == 'true'
  uses: docker/login-action@v3
  with:
    registry: ghcr.io
    username: ${{ github.actor }}
    password: ${{ secrets.GITHUB_TOKEN }}
```

### 3. `build`-Job: Retag + Push Step

Nach dem bestehenden `verify`-Step (der die Images lokal gebaut hat):

```yaml
- name: Retag and push Docker images with branch tag
  if: needs.prepare.outputs.should_branch_deploy == 'true'
  run: |
    VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
    BRANCH_TAG="${{ needs.prepare.outputs.branch_tag }}"

    docker images --format "{{.Repository}}:{{.Tag}}" \
      | grep "^ghcr.io/eclipse-slm/.*:${VERSION}$" \
      | while IFS= read -r image; do
          REPO="${image%:*}"
          docker tag "$image" "${REPO}:${BRANCH_TAG}"
          docker push "${REPO}:${BRANCH_TAG}"
          echo "Pushed ${REPO}:${BRANCH_TAG}"
        done
```

Dieser Step läuft für beide Matrix-Typen (`frontend` und `backend`), da fabric8 für beide Images baut.

---

## Neuer Workflow `cleanup-branch-images.yml`

**Datei:** `.github/workflows/cleanup-branch-images.yml`

**Trigger:** `delete`-Event — gefiltert auf `ref_type == 'branch'` und feature/* / fix/* via Job-Condition.

**Ablauf:**
1. Branch-Tag aus `github.event.ref` berechnen
2. Alle Container-Packages der Organisation `eclipse-slm` via GitHub API auflisten
3. Je Package: Version mit dem Branch-Tag suchen und löschen

```yaml
name: Cleanup Branch Docker Images

on:
  delete:

jobs:
  cleanup:
    if: |
      github.event.ref_type == 'branch' &&
      (startsWith(github.event.ref, 'feature/') || startsWith(github.event.ref, 'fix/'))
    runs-on: ubuntu-latest
    permissions:
      packages: write
    steps:
      - name: Delete branch Docker images from GHCR
        env:
          GH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          BRANCH_TAG=$(echo "${{ github.event.ref }}" | tr '/' '-')
          ORG="eclipse-slm"

          echo "Cleaning up images for branch tag: ${BRANCH_TAG}"

          # Alle Container-Packages der Org auflisten (paginiert)
          gh api "/orgs/${ORG}/packages?package_type=container&per_page=100" \
            --jq '.[].name' \
          | while IFS= read -r pkg; do
              # Package-Name URL-encoden (/ → %2F)
              ENCODED_PKG=$(python3 -c "import urllib.parse; print(urllib.parse.quote('${pkg}', safe=''))")

              # Version mit diesem Branch-Tag suchen
              VERSION_ID=$(gh api \
                "/orgs/${ORG}/packages/container/${ENCODED_PKG}/versions?per_page=100" \
                --jq ".[] | select(.metadata.container.tags // [] | index(\"${BRANCH_TAG}\") != null) | .id" \
                2>/dev/null || true)

              if [ -n "$VERSION_ID" ]; then
                echo "Deleting ${pkg}:${BRANCH_TAG} (id=${VERSION_ID})"
                gh api --method DELETE \
                  "/orgs/${ORG}/packages/container/${ENCODED_PKG}/versions/${VERSION_ID}"
              fi
            done
```

**Permissions:** `packages: write` auf Job-Ebene — kein weiteres Secret nötig, `GITHUB_TOKEN` reicht für Packages der eigenen Org.

---

## Nicht im Scope

- Keine Änderung am Maven-Profil (`-Psnapshot` bleibt auch für Branch-Images)
- Kein Staging-Deployment für Branch-Images
- Keine Retention-Policy (Images bleiben bis Branch-Delete erhalten)
- Kein Cleanup alter Branch-Images, die vor Einführung dieses Workflows existieren
