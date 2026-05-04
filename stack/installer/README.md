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

Examples:

```bash
./run-installer.sh --mode ui
./run-installer.sh --mode install
./run-installer.sh --mode uninstall
```

