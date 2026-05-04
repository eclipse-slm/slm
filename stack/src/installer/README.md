# Installer

This directory now supports two execution paths that share the same playbook runner core:

1. **UI mode** (Vue 3 + FastAPI + WebSocket live logs)
2. **Direct non-interactive mode** (Docker) without UI

The existing Ansible playbook remains the deployment source of truth in `ansible/installer/src/main.yml`.

## Architecture

- `backend/runner.py` contains the shared execution core for Ansible runs.
- `backend/main.py` exposes REST and WebSocket APIs for job-based execution.
- `backend/cli_runner.py` is the internal one-shot execution target used by the backend container.
- `backend/entrypoint-api.sh` decides whether the backend container starts the API or runs a one-shot install/uninstall execution.
- `frontend/` is a Vue 3 UI for job submission and live logs.
- `docker-compose.yml` uses the dedicated Compose project name `slm-installer` so the uninstaller can remove the deployed `eclipse-slm` stack without stopping the installer control plane.

## Job API

All installer backend endpoints are served under the `/api` prefix.

### REST

- `POST /api/install`: start the installer playbook
- `POST /api/uninstall`: start the uninstaller playbook
- `POST /api/cancel`: request cancellation of the currently active job
- `GET /api/job`: get the currently active job state (or `idle`)
- `GET /api/logs`: get buffered events of the currently active job

`POST /api/install` expects exactly these fields:

```json
{
  "slmHostname": "myhost.local",
  "slmIp": "172.17.0.1",
  "logLevel": "standard"
}
```

The API always executes the fixed installer playbook `main.yml` from `/ansible/installer/src`.
`SLM_HOSTNAME` and `SLM_IP` come from the request payload.
`SLM_VERSION` is fixed to `1.5.0-SNAPSHOT` and is not user-configurable in the UI/API flow.
`logLevel` maps to Ansible verbosity flags as follows: `standard -> -v`, `detailed -> -vv`, `debug -> -vvv`, `trace -> -vvvv`.

`POST /api/uninstall` expects the same `logLevel` field and runs the fixed uninstaller playbook from `/ansible/uninstaller/src/main.yml`.

There are only two job types (`install`, `uninstall`), and only one job can be active at a time.
`POST /api/cancel` always applies to the currently active job.
The UI checks the active job on startup and resumes streaming when a job is running.

### WebSocket

- `GET /api/stream`: stream live events as JSON

### API documentation

- Swagger UI: `/api/docs`
- OpenAPI JSON: `/api/openapi.json`
- ReDoc: `/api/redoc`

Event schema:

```json
{
  "operation": "install|uninstall",
  "type": "status|log|task|host|summary|error",
  "timestamp": "ISO-8601",
  "level": "debug|info|warn|error",
  "message": "string",
  "task": "optional string",
  "host": "optional string",
  "raw": {}
}
```

## Common Runner Core

Both UI and the backend container one-shot modes use `backend/runner.py`:

- Backend path: `POST /api/install` or `POST /api/uninstall` -> starts thread -> executes `runner.run(...)`
- One-shot container path: `install` or `uninstall` -> `backend/entrypoint-api.sh` -> `python -m backend.cli_runner` -> executes `runner.run(...)`

This keeps behavior consistent and avoids duplicate execution logic.

For API-triggered jobs, install and uninstall playbook paths are intentionally fixed to `main.yml` in their respective working directories.

## Files

- `backend/main.py`
- `backend/job_manager.py`
- `backend/runner.py`
- `backend/Dockerfile`
- `backend/entrypoint-api.sh`
- `frontend/src/components/PlaybookRunner.vue`
- `frontend/src/composables/useJobStream.ts`
- `ansible/installer/docker-compose.yml`

## Configuration

Environment variables used by backend/runner:

- `INSTALLER_WORKING_DIR` (default: `/ansible/installer/src`)
- `INSTALLER_PLAYBOOK` (default: `main.yml`)
- `INSTALLER_UNINSTALL_WORKING_DIR` (default: `/ansible/uninstaller/src`)
- `INSTALLER_UNINSTALL_PLAYBOOK` (default: `main.yml`)
- `INSTALLER_INVENTORY` (optional)
- `INSTALLER_ENV_FILE` (default: `/env/env.yml`)
- `INSTALLER_RUNNER_MODE` (`subprocess` or `ansible-runner`)
- `ANSIBLE_PLAYBOOK_BIN` (default: `ansible-playbook`)
- `INSTALLER_FIXED_SLM_VERSION` (default: `1.5.0-SNAPSHOT`)
- `INSTALLER_API_HOST` (default: `0.0.0.0`)
- `INSTALLER_API_PORT` (default: `8080`)

## Python compatibility

- Local backend development is supported with Python `3.11`, `3.12`, or `3.13`.
- Python `3.14` is currently blocked on purpose because `pydantic-core` can fall back to a Rust source build that fails with the PyO3 version in the dependency chain.
- If your workstation only has Python `3.14`, use the Docker-based workflow until upstream wheels are available.

## Start with UI (docker compose)

```bash
docker compose -f ansible/installer/docker-compose.yml up --build installer-api installer-ui
```

- UI: `http://localhost:5173`
- API: `http://localhost:8080`

The UI asks for confirmation before starting uninstallation or requesting cancellation of an active job.
It also lets the user choose a log level that the backend maps to the matching Ansible verbosity flag.
The UI uses the embedded Nginx reverse proxy and always calls the backend via same-origin `/api`.

If you already started the installer control plane before this change, recreate it once so the containers pick up the new Compose project name:

```bash
COMPOSE_PROJECT_NAME=eclipse-slm docker compose down
docker compose down
docker compose up --build
```

## Direct non-interactive mode (without UI)

The `installer-api` image now supports the following startup modes through `backend/entrypoint-api.sh`:

- no argument: start the FastAPI backend and keep the container running
- `install`: run the installer playbook once and exit
- `uninstall`: run the uninstaller playbook once and exit

### Backend container in API mode via docker compose

Start the backend container without an additional argument to run the API continuously:

```bash
docker compose up --build installer-api
```

### Existing path (fully preserved)

```bash
docker compose -f ansible/installer/docker-compose.yml run --rm installer
```


### Backend container one-shot install mode

```bash
docker compose -f ansible/installer/docker-compose.yml run --rm \
  -e SLM_HOSTNAME=myhost.local \
  -e SLM_IP=172.17.0.1 \
  installer-api install
```

This runs `/ansible/installer/src/main.yml` with `SLM_HOSTNAME`, `SLM_IP`, and the fixed `SLM_VERSION`, then exits.
`SLM_HOSTNAME` and `SLM_IP` are required in this mode.

### Backend container one-shot uninstall mode

```bash
docker compose -f ansible/installer/docker-compose.yml run --rm installer-api uninstall
```

This runs `/ansible/uninstaller/src/main.yml` with the fixed `SLM_VERSION`, then exits.

## Local development

### Backend

```powershell
Set-Location stack/src/installer/backend
py -3.13 -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
python -m pip install -e .
uvicorn backend.main:app --reload --host 0.0.0.0 --port 8080
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

`vite` proxies `/api` to `http://localhost:8080` in local dev, so no extra API base URL variable is required.

## Notes and assumptions

- This implementation keeps existing roles/inventory/playbook files untouched.
- Job repository is in-memory by design; replace `InMemoryJobRepository` for Redis/DB later.
- Authentication/authorization is intentionally not implemented yet, but API boundaries are ready for it.
- `INSTALLER_RUNNER_MODE=ansible-runner` is available if `ansible-runner` is installed and desired.
- `backend/pyproject.toml` is the single source of truth for backend runtime dependencies and supported Python versions.


