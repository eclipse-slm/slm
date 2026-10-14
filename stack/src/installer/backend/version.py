from __future__ import annotations

import os

# Single source of truth for the SLM version inside the installer container.
# Overridden at build time by the SLM_VERSION build arg (see backend/Dockerfile)
# and at runtime by INSTALLER_FIXED_SLM_VERSION, so the version used by the
# playbooks always matches the image tag the installer was launched with.
DEFAULT_SLM_VERSION = "1.5.0-SNAPSHOT"


def slm_version() -> str:
    return os.getenv("INSTALLER_FIXED_SLM_VERSION") or DEFAULT_SLM_VERSION
