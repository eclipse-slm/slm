# Installer

## Download and run the script

```bash
curl -fsSL -o slm-installer.sh https://raw.githubusercontent.com/eclipse-slm/slm/develop/stack/installer/slm-installer.sh
chmod +x slm-installer.sh
./slm-installer.sh
```

Alternative with `wget`:

```bash
wget -O slm-installer.sh https://raw.githubusercontent.com/eclipse-slm/slm/develop/stack/installer/slm-installer.sh
chmod +x slm-installer.sh
./slm-installer.sh
```

## Modes

- `ui`: starts frontend + backend with Docker Compose
- `install`: runs non-interactive installer and exits
- `uninstall`: runs non-interactive uninstaller and exits

The UI stack is started with Docker Compose project name `eclipse-slm-installer`.

Examples:

```bash
./slm-installer.sh --mode ui
./slm-installer.sh --mode install
./slm-installer.sh --mode uninstall
```

## API endpoints (UI mode)

When running in `ui` mode, the backend is exposed through the frontend host under `/api`.

- Swagger UI: `http://<installer-host>:6060/api/docs`
- OpenAPI JSON: `http://<installer-host>:6060/api/openapi.json`
- ReDoc: `http://<installer-host>:6060/api/redoc`

The installer UI includes a `Stop Installer` action in the top-right app bar.
It stops and removes the installer frontend and backend containers.

