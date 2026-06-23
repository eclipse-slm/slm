# Branch Docker Images – Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Docker Images für `feature/*`- und `fix/*`-Branches automatisch mit sanitiertem Branch-Namen taggen, pushen und beim Branch-Delete aufräumen.

**Architecture:** `build-core.yml` bekommt zwei neue Outputs im `prepare`-Job und zwei neue Steps im `build`-Job. Ein neuer `cleanup-branch-images.yml`-Workflow löscht Images beim Branch-Delete via GitHub API. Das fabric8 docker-maven-plugin baut Images bereits während `mvn verify` lokal — kein zweiter Maven-Build nötig.

**Tech Stack:** GitHub Actions YAML, fabric8 docker-maven-plugin, ghcr.io (GitHub Container Registry), `gh` CLI, `docker tag` / `docker push`

---

## File Structure

| Datei | Aktion | Was ändert sich |
|---|---|---|
| `.github/workflows/build-core.yml` | Modify | `prepare`-Outputs + evaluate-Script erweitern; 2 neue Steps im `build`-Job |
| `.github/workflows/cleanup-branch-images.yml` | Create | Neuer Workflow für Branch-Delete-Cleanup |

---

## Task 1: `prepare`-Job um `should_branch_deploy` und `branch_tag` erweitern

**Files:**
- Modify: `.github/workflows/build-core.yml` (Zeilen 23–70)

Der `prepare`-Job braucht zwei neue Outputs und die entsprechende Berechnung im evaluate-Script.

- [ ] **Step 1: Neue Outputs zum `prepare`-Job hinzufügen**

Den `outputs`-Block (aktuell Zeilen 23–28) so erweitern:

```yaml
    outputs:
      has_release_tag: ${{ steps.evaluate.outputs.has_release_tag }}
      should_snapshot_deploy: ${{ steps.evaluate.outputs.should_snapshot_deploy }}
      should_release_deploy: ${{ steps.evaluate.outputs.should_release_deploy }}
      should_verify: ${{ steps.evaluate.outputs.should_verify }}
      should_run_staging: ${{ steps.evaluate.outputs.should_run_staging }}
      should_branch_deploy: ${{ steps.evaluate.outputs.should_branch_deploy }}
      branch_tag: ${{ steps.evaluate.outputs.branch_tag }}
```

- [ ] **Step 2: Berechnung im evaluate-Script ergänzen**

Direkt vor dem letzten `echo`-Block im evaluate-Script (nach Zeile 64, vor dem ersten `echo`), diesen Block einfügen:

```bash
          SHOULD_BRANCH_DEPLOY=false
          BRANCH_TAG=""
          if [[ "$GITHUB_REF_NAME" =~ ^feature/ || "$GITHUB_REF_NAME" =~ ^fix/ ]]; then
            SHOULD_BRANCH_DEPLOY=true
            BRANCH_TAG=$(echo "$GITHUB_REF_NAME" | tr '/' '-')
          fi
```

Den echo-Block (Zeilen 66–70) um zwei neue Zeilen erweitern:

```bash
          echo "has_release_tag=$HAS_RELEASE_TAG" >> "$GITHUB_OUTPUT"
          echo "should_snapshot_deploy=$SHOULD_SNAPSHOT_DEPLOY" >> "$GITHUB_OUTPUT"
          echo "should_release_deploy=$SHOULD_RELEASE_DEPLOY" >> "$GITHUB_OUTPUT"
          echo "should_verify=$SHOULD_VERIFY" >> "$GITHUB_OUTPUT"
          echo "should_run_staging=$SHOULD_RUN_STAGING" >> "$GITHUB_OUTPUT"
          echo "should_branch_deploy=$SHOULD_BRANCH_DEPLOY" >> "$GITHUB_OUTPUT"
          echo "branch_tag=$BRANCH_TAG" >> "$GITHUB_OUTPUT"
```

- [ ] **Step 3: YAML-Syntax prüfen**

```bash
python3 -c "import yaml; yaml.safe_load(open('.github/workflows/build-core.yml'))" && echo "YAML OK"
```

Erwartete Ausgabe: `YAML OK`

- [ ] **Step 4: Commit**

```bash
git add .github/workflows/build-core.yml
git commit -m "ci: add should_branch_deploy and branch_tag outputs to prepare job"
```

---

## Task 2: GHCR-Login und Retag+Push im `build`-Job ergänzen

**Files:**
- Modify: `.github/workflows/build-core.yml` (nach Zeile 137, vor dem Coveralls-Step)

Nach dem `Build '${{ matrix.build.type }}' with Maven`-Step (Zeile 137) und vor dem `Build Coveralls Report`-Step (Zeile 138) zwei neue Steps einfügen.

- [ ] **Step 1: GHCR-Login Step einfügen**

```yaml
      - name: Log in to GitHub Container Registry (branch deploy)
        if: needs.prepare.outputs.should_branch_deploy == 'true'
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
```

`docker/login-action@v3` ist bereits im GitHub Actions Marketplace verfügbar, kein weiteres Secret nötig — `GITHUB_TOKEN` hat `packages: write` (bereits im `build`-Job gesetzt).

- [ ] **Step 2: Retag+Push Step einfügen**

```yaml
      - name: Retag and push Docker images with branch tag
        if: needs.prepare.outputs.should_branch_deploy == 'true'
        run: |
          VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
          BRANCH_TAG="${{ needs.prepare.outputs.branch_tag }}"

          echo "Retagging images from :${VERSION} to :${BRANCH_TAG}"

          docker images --format "{{.Repository}}:{{.Tag}}" \
            | grep "^ghcr.io/eclipse-slm/.*:${VERSION}$" \
            | while IFS= read -r image; do
                REPO="${image%:*}"
                docker tag "$image" "${REPO}:${BRANCH_TAG}"
                docker push "${REPO}:${BRANCH_TAG}"
                echo "Pushed ${REPO}:${BRANCH_TAG}"
              done
```

Erklärung:
- `mvn help:evaluate` liest `${project.version}` aus dem Root-POM (z.B. `1.5.0-SNAPSHOT`)
- `docker images --format` listet alle lokalen Images
- `grep "^ghcr.io/eclipse-slm/.*:${VERSION}$"` filtert auf genau die vom fabric8-Plugin gebauten Images
- `${image%:*}` schneidet den Tag ab → Repository-Name
- Läuft für beide Matrix-Typen (`frontend` und `backend`)

- [ ] **Step 3: YAML-Syntax prüfen**

```bash
python3 -c "import yaml; yaml.safe_load(open('.github/workflows/build-core.yml'))" && echo "YAML OK"
```

Erwartete Ausgabe: `YAML OK`

- [ ] **Step 4: Commit**

```bash
git add .github/workflows/build-core.yml
git commit -m "ci: retag and push Docker images with branch tag for feature/* and fix/*"
```

---

## Task 3: Cleanup-Workflow `cleanup-branch-images.yml` erstellen

**Files:**
- Create: `.github/workflows/cleanup-branch-images.yml`

- [ ] **Step 1: Datei erstellen**

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

          gh api "/orgs/${ORG}/packages?package_type=container&per_page=100" \
            --jq '.[].name' \
          | while IFS= read -r pkg; do
              # Package-Namen mit '/' müssen URL-encoded werden (/ -> %2F)
              ENCODED_PKG=$(python3 -c "import urllib.parse, sys; print(urllib.parse.quote(sys.argv[1], safe=''))" "$pkg")

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

Hinweise:
- `delete`-Event feuert für Branch- und Tag-Deletes — der `if`-Block auf Job-Ebene filtert auf `ref_type == 'branch'` und feature/fix-Präfix
- `GITHUB_TOKEN` reicht, `packages: write` ist auf Job-Ebene gesetzt
- `python3 -c "import urllib.parse..."` ist auf `ubuntu-latest` Runnern immer verfügbar
- Package-Namen wie `slm/service-management` enthalten `/` → müssen als `slm%2Fservice-management` in der API-URL stehen

- [ ] **Step 2: YAML-Syntax prüfen**

```bash
python3 -c "import yaml; yaml.safe_load(open('.github/workflows/cleanup-branch-images.yml'))" && echo "YAML OK"
```

Erwartete Ausgabe: `YAML OK`

- [ ] **Step 3: Commit**

```bash
git add .github/workflows/cleanup-branch-images.yml
git commit -m "ci: add cleanup workflow for branch Docker images on branch delete"
```

---

## Task 4: End-to-End-Verifikation

Keine automatisierten Tests möglich für GitHub Actions Workflows — manuelle Verifikation auf einem Test-Branch.

- [ ] **Step 1: Änderungen auf `develop` oder einen Test-Branch pushen**

```bash
git push origin feature/branch-docker-images
```

- [ ] **Step 2: Build-Workflow in GitHub Actions beobachten**

Unter `https://github.com/eclipse-slm/slm/actions` den Workflow-Run für den Push öffnen.

Erwartung im `build`-Job:
- Step `Log in to GitHub Container Registry (branch deploy)` läuft durch
- Step `Retag and push Docker images with branch tag` gibt aus:
  ```
  Retagging images from :1.5.0-SNAPSHOT to :feature-branch-docker-images
  Pushed ghcr.io/eclipse-slm/slm/service-management:feature-branch-docker-images
  Pushed ghcr.io/eclipse-slm/slm/resource-management:feature-branch-docker-images
  ... (weitere Images)
  ```

- [ ] **Step 3: Images in GHCR prüfen**

Unter `https://github.com/orgs/eclipse-slm/packages` nachsehen, ob die Images mit dem Branch-Tag erscheinen.

Alternativ via `gh` CLI:
```bash
gh api "/orgs/eclipse-slm/packages/container/slm%2Fservice-management/versions" \
  --jq '.[] | .metadata.container.tags'
```

Erwartung: Tag `feature-branch-docker-images` ist sichtbar.

- [ ] **Step 4: Branch löschen und Cleanup prüfen**

```bash
git push origin --delete feature/branch-docker-images
```

Unter `https://github.com/eclipse-slm/slm/actions` den `Cleanup Branch Docker Images`-Run öffnen.

Erwartete Ausgabe:
```
Cleaning up images for branch tag: feature-branch-docker-images
Deleting slm/service-management:feature-branch-docker-images (id=12345678)
...
```

- [ ] **Step 5: Images in GHCR sind weg**

```bash
gh api "/orgs/eclipse-slm/packages/container/slm%2Fservice-management/versions" \
  --jq '.[] | .metadata.container.tags'
```

Erwartung: Tag `feature-branch-docker-images` ist nicht mehr vorhanden.
