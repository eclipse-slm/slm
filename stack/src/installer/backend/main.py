from __future__ import annotations

import asyncio
import ipaddress
import os
import subprocess
import threading
from contextlib import suppress
from datetime import datetime
from enum import StrEnum
from typing import Any, Dict, Optional

from fastapi import APIRouter, FastAPI, HTTPException, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, ConfigDict, Field
from starlette.websockets import WebSocketState

from backend.job_manager import JobManager
from backend.runner import PlaybookRunOptions, create_runner

API_PREFIX = "/api"

app = FastAPI(
    title="SLM Installer API",
    version="1.0.0",
    docs_url=f"{API_PREFIX}/docs",
    redoc_url=f"{API_PREFIX}/redoc",
    openapi_url=f"{API_PREFIX}/openapi.json",
)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

job_manager = JobManager()
FIXED_SLM_VERSION = "1.5.0-SNAPSHOT"
INSTALLER_COMPOSE_PROJECT_NAME = "eclipse-slm-installer"
api_router = APIRouter(prefix=API_PREFIX)


class LogLevelName(StrEnum):
    STANDARD = "standard"
    DETAILED = "detailed"
    DEBUG = "debug"
    TRACE = "trace"


LOG_LEVEL_TO_VERBOSITY = {
    LogLevelName.STANDARD: 1,
    LogLevelName.DETAILED: 2,
    LogLevelName.DEBUG: 3,
    LogLevelName.TRACE: 4,
}
STREAM_POLL_INTERVAL_SECONDS = 1.0
app.state.shutdown_requested = False


class JobOptionsRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    logLevel: LogLevelName = LogLevelName.STANDARD


class JobCreateRequest(JobOptionsRequest):
    slmHostname: str = Field(min_length=1)
    slmIp: str = Field(min_length=1)


class UninstallRequest(JobOptionsRequest):
    pass


class JobResponse(BaseModel):

    operation: str
    status: str
    startTime: Optional[str]
    endTime: Optional[str]
    exitCode: Optional[int]
    cancelRequested: bool


class JobLogsResponse(BaseModel):
    logs: list[Dict[str, Any]]


class InstallerStopResponse(BaseModel):
    status: str
    message: str


def _record_to_response(record: Dict[str, Any]) -> JobResponse:
    return JobResponse(
        operation=record["operation"],
        status=record["status"],
        startTime=record["startTime"],
        endTime=record["endTime"],
        exitCode=record["exitCode"],
        cancelRequested=record["cancelRequested"],
    )


def _idle_response() -> JobResponse:
    return JobResponse(
        operation="none",
        status="idle",
        startTime=None,
        endTime=None,
        exitCode=None,
        cancelRequested=False,
    )

def _get_active_job_record() -> Optional[Any]:
    return job_manager.get_active_job()


def _verbosity_for_log_level(log_level: LogLevelName) -> int:
    return LOG_LEVEL_TO_VERBOSITY[log_level]


def _raise_if_active_job_exists() -> None:
    if job_manager.get_active_job() is not None:
        raise HTTPException(status_code=409, detail="Another install or uninstall job is already running")


@app.on_event("startup")
async def on_startup() -> None:
    app.state.shutdown_requested = False


@app.on_event("shutdown")
async def on_shutdown() -> None:
    app.state.shutdown_requested = True
    await job_manager.shutdown()


async def _append_queued_event(job_id: str, operation: str) -> None:
    action = "installation" if operation == "install" else "uninstallation"
    await job_manager.append_event(
        job_id,
        {
            "jobId": job_id,
            "type": "status",
            "timestamp": datetime.utcnow().isoformat() + "Z",
            "level": "info",
            "message": f"Job queued for {action}",
            "raw": {"oldStatus": None, "newStatus": "queued", "exitCode": None, "operation": operation},
        },
    )


def _start_background_job(record: Any, runner: Any, options: Any, failure_message: str) -> None:
    event_loop = asyncio.get_running_loop()

    def log_event_to_container(event: Dict[str, Any]) -> None:
        message = str(event.get("message", "")).strip()
        if not message:
            return
        print(message, flush=True)

    def submit(coro: Any, wait: bool = False) -> None:
        if event_loop.is_closed() or app.state.shutdown_requested:
            return

        try:
            future = job_manager.submit_coro_from_thread(coro, event_loop)
        except RuntimeError:
            return

        if wait:
            with suppress(Exception):
                future.result(timeout=5)

    def emit(event: Dict[str, Any]) -> None:
        log_event_to_container(event)
        submit(job_manager.append_event(record.job_id, event))

    def run_job() -> None:
        try:
            current = job_manager.get_job(record.job_id)
            if current and current.status == "canceled":
                return

            if job_manager.is_cancel_requested(record.job_id):
                submit(job_manager.mark_canceled(record.job_id), wait=True)
                return

            submit(job_manager.mark_running(record.job_id), wait=True)
            exit_code = runner.run(
                options,
                emit=emit,
                on_process_started=lambda handle: job_manager.register_execution_handle(record.job_id, handle),
            )

            if job_manager.is_cancel_requested(record.job_id):
                submit(job_manager.mark_canceled(record.job_id, exit_code))
            else:
                submit(job_manager.mark_finished(record.job_id, exit_code))
        except Exception as exc:
            emit(
                {
                    "jobId": record.job_id,
                    "type": "error",
                    "timestamp": datetime.utcnow().isoformat() + "Z",
                    "level": "error",
                    "message": f"{failure_message}: {exc}",
                    "raw": {"exception": str(exc), "operation": record.operation},
                }
            )
            if job_manager.is_cancel_requested(record.job_id):
                submit(job_manager.mark_canceled(record.job_id, exit_code=1))
            else:
                submit(job_manager.mark_failed_to_start(record.job_id))
        finally:
            job_manager.clear_execution_handle(record.job_id)

    threading.Thread(target=run_job, daemon=True).start()


def _stop_installer_stack(project_name: str) -> None:
    """Stop and remove all containers that belong to the installer compose project."""
    ids_cmd = [
        "docker",
        "ps",
        "-aq",
        "--filter",
        f"label=com.docker.compose.project={project_name}",
    ]
    ids_result = subprocess.run(ids_cmd, capture_output=True, text=True, check=False)
    ids = [container_id.strip() for container_id in ids_result.stdout.splitlines() if container_id.strip()]
    if not ids:
        return

    subprocess.run(["docker", "rm", "-f", *ids], capture_output=True, text=True, check=False)


def _docker_host_gateway_ip() -> str:
    """Return the Docker host gateway IP as seen from inside this container."""
    route_path = "/proc/net/route"
    try:
        with open(route_path, "r", encoding="utf-8") as handle:
            lines = handle.readlines()
    except OSError as exc:
        raise RuntimeError(f"Could not read {route_path}: {exc}") from exc

    for line in lines[1:]:
        parts = line.split()
        if len(parts) < 3:
            continue
        destination = parts[1]
        gateway_hex = parts[2]
        if destination != "00000000":
            continue

        try:
            gateway_int = int(gateway_hex, 16)
            gateway_bytes = gateway_int.to_bytes(4, byteorder="little", signed=False)
            return str(ipaddress.IPv4Address(gateway_bytes))
        except (ValueError, OverflowError):
            continue

    raise RuntimeError("Could not determine Docker host gateway IP from /proc/net/route")


def _ensure_container_hostname_mapping(hostname: str) -> None:
    hostname = hostname.strip()
    if not hostname:
        return

    ip_address = _docker_host_gateway_ip()

    hosts_path = "/etc/hosts"
    try:
        with open(hosts_path, "r", encoding="utf-8") as handle:
            lines = handle.readlines()
    except OSError as exc:
        raise RuntimeError(f"Could not read {hosts_path}: {exc}") from exc

    rewritten: list[str] = []
    for line in lines:
        stripped = line.strip()
        if not stripped or stripped.startswith("#"):
            rewritten.append(line)
            continue

        parts = stripped.split()
        aliases = parts[1:]
        if hostname in aliases:
            continue
        rewritten.append(line)

    rewritten.append(f"{ip_address}\t{hostname}\n")

    try:
        with open(hosts_path, "w", encoding="utf-8") as handle:
            handle.writelines(rewritten)
    except OSError as exc:
        raise RuntimeError(f"Could not write {hosts_path}: {exc}") from exc


@api_router.post("/install", response_model=JobResponse, status_code=201)
async def start_install(request: JobCreateRequest) -> JobResponse:
    _raise_if_active_job_exists()

    try:
        _ensure_container_hostname_mapping(request.slmHostname)
    except RuntimeError as exc:
        raise HTTPException(status_code=500, detail=str(exc)) from exc

    request_data = request.model_dump()
    record = job_manager.create_job(request_data, operation="install")
    await _append_queued_event(record.job_id, operation="install")

    runner = create_runner()
    options = PlaybookRunOptions(
        job_id=record.job_id,
        playbook_path=os.getenv("INSTALLER_PLAYBOOK", "main.yml"),
        working_dir=os.getenv("INSTALLER_WORKING_DIR", "/ansible/installer/src"),
        env_file=os.getenv("INSTALLER_ENV_FILE", "/env/env.yml"),
        verbosity=_verbosity_for_log_level(request.logLevel),
        execution_env={
            "SLM_HOSTNAME": request.slmHostname,
            "SLM_IP": request.slmIp,
            "SLM_VERSION": FIXED_SLM_VERSION,
        },
    )
    _start_background_job(record, runner, options, failure_message="Installer execution failed")

    latest = job_manager.get_job(record.job_id)
    return _record_to_response(latest.to_dict())


@api_router.post("/uninstall", response_model=JobResponse, status_code=201)
async def start_uninstall(request: UninstallRequest) -> JobResponse:
    _raise_if_active_job_exists()

    request_data = request.model_dump()
    record = job_manager.create_job(request_data, operation="uninstall")
    await _append_queued_event(record.job_id, operation="uninstall")

    runner = create_runner()
    options = PlaybookRunOptions(
        job_id=record.job_id,
        playbook_path=os.getenv("INSTALLER_UNINSTALL_PLAYBOOK", "main.yml"),
        working_dir=os.getenv("INSTALLER_UNINSTALL_WORKING_DIR", "/ansible/uninstaller/src"),
        env_file=os.getenv("INSTALLER_ENV_FILE", "/env/env.yml"),
        verbosity=_verbosity_for_log_level(request.logLevel),
        execution_env={
            "SLM_VERSION": FIXED_SLM_VERSION,
        },
    )
    _start_background_job(record, runner, options, failure_message="Uninstaller playbook execution failed")

    latest = job_manager.get_job(record.job_id)
    return _record_to_response(latest.to_dict())


@api_router.get("/job", response_model=JobResponse)
async def get_job() -> JobResponse:
    record = _get_active_job_record()
    if not record:
        return _idle_response()
    return _record_to_response(record.to_dict())


@api_router.get("/logs", response_model=JobLogsResponse)
async def get_job_logs() -> JobLogsResponse:
    record = _get_active_job_record()
    if not record:
        return JobLogsResponse(logs=[])
    return JobLogsResponse(logs=record.logs)


@api_router.post("/cancel", response_model=JobResponse)
async def cancel_job() -> JobResponse:
    record = job_manager.get_active_job()
    if not record:
        raise HTTPException(status_code=409, detail="No active job to cancel")

    updated = await job_manager.request_cancel(record.job_id)
    if not updated:
        raise HTTPException(status_code=409, detail="No active job to cancel")

    return _record_to_response(updated.to_dict())


@api_router.post("/installer/stop", response_model=InstallerStopResponse, status_code=202)
async def stop_installer() -> InstallerStopResponse:
    project_name = os.getenv("INSTALLER_COMPOSE_PROJECT_NAME", INSTALLER_COMPOSE_PROJECT_NAME)
    threading.Thread(target=_stop_installer_stack, args=(project_name,), daemon=True).start()
    return InstallerStopResponse(
        status="stopping",
        message=f"Stopping installer stack '{project_name}'.",
    )


@api_router.websocket("/stream")
async def stream_job(websocket: WebSocket) -> None:
    record = _get_active_job_record()
    if not record:
        await websocket.close(code=4404, reason="Job not found")
        return

    job_id = record.job_id

    await websocket.accept()

    for event in record.logs:
        await websocket.send_json(event)

    queue = await job_manager.subscribe(job_id)
    try:
        while True:
            if app.state.shutdown_requested:
                break

            try:
                event = await asyncio.wait_for(queue.get(), timeout=STREAM_POLL_INTERVAL_SECONDS)
            except asyncio.TimeoutError:
                continue

            if event is None:
                break

            await websocket.send_json(event)
            if (
                event.get("type") == "status"
                and event.get("raw", {}).get("newStatus") in {"successful", "failed", "canceled"}
            ):
                break
    except WebSocketDisconnect:
        await job_manager.unsubscribe(job_id, queue)
    except asyncio.CancelledError:
        await job_manager.unsubscribe(job_id, queue)
        raise
    except Exception:
        await job_manager.unsubscribe(job_id, queue)
        if websocket.application_state == WebSocketState.CONNECTED:
            with suppress(RuntimeError):
                await websocket.close(code=1011, reason="Stream failed")
    else:
        await job_manager.unsubscribe(job_id, queue)
        if websocket.application_state == WebSocketState.CONNECTED:
            with suppress(RuntimeError):
                await websocket.close(code=1000)


app.include_router(api_router)


