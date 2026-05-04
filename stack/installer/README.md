# Installer

## Download and run the script

```bash
curl -fsSL -o run-installer.sh https://raw.githubusercontent.com/eclipse-slm/slm/develop/stack/installer/run-installer.sh
chmod +x run-installer.sh
./run-installer.sh
```

Alternative with `wget`:

```bash
wget -O run-installer.sh https://raw.githubusercontent.com/eclipse-slm/slm/develop/stack/installer/run-installer.sh
chmod +x run-installer.sh
./run-installer.sh
```

## Modes

- `ui`: starts frontend + backend with Docker Compose
- `install`: runs non-interactive installer and exits
- `uninstall`: runs non-interactive uninstaller and exits

The UI stack is started with Docker Compose project name `eclipse-slm-installer`.

Examples:

```bash
./run-installer.sh --mode ui
./run-installer.sh --mode install
./run-installer.sh --mode uninstall
```

## API endpoints (UI mode)

When running in `ui` mode, the backend is exposed through the frontend host under `/api`.

- Swagger UI: `http://<installer-host>:6060/api/docs`
- OpenAPI JSON: `http://<installer-host>:6060/api/openapi.json`
- ReDoc: `http://<installer-host>:6060/api/redoc`

The installer UI includes a `Stop Installer` action in the top-right app bar.
It stops and removes the installer frontend and backend containers.

