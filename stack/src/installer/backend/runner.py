from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import threading
from dataclasses import dataclass, field
from datetime import datetime, timezone
from typing import Any, Callable, Dict, Iterable, Optional

EventCallback = Callable[[Dict[str, Any]], None]
ProcessStartedCallback = Callable[["SubprocessExecutionHandle"], None]


@dataclass
class PlaybookRunOptions:
    job_id: str
    playbook_path: str = "main.yml"
    inventory: Optional[str] = None
    limit: Optional[str] = None
    verbosity: int = 1
    tags: list[str] = field(default_factory=list)
    skip_tags: list[str] = field(default_factory=list)
    extra_vars: Dict[str, Any] = field(default_factory=dict)
    working_dir: str = "/ansible/installer/src"
    env_file: Optional[str] = None
    execution_env: Dict[str, str] = field(default_factory=dict)


class BasePlaybookRunner:
    def run(
        self,
        options: PlaybookRunOptions,
        emit: EventCallback,
        on_process_started: Optional[ProcessStartedCallback] = None,
    ) -> int:
        raise NotImplementedError


class SubprocessExecutionHandle:
    def __init__(self, process: subprocess.Popen[str]) -> None:
        self._process = process
        self._lock = threading.Lock()

    def cancel(self) -> None:
        with self._lock:
            if self._process.poll() is not None:
                return

            self._process.terminate()
            try:
                self._process.wait(timeout=10)
            except subprocess.TimeoutExpired:
                self._process.kill()


class SubprocessPlaybookRunner(BasePlaybookRunner):
    def __init__(self, ansible_binary: str = "ansible-playbook") -> None:
        self.ansible_binary = ansible_binary

    def run(
        self,
        options: PlaybookRunOptions,
        emit: EventCallback,
        on_process_started: Optional[ProcessStartedCallback] = None,
    ) -> int:
        command = build_ansible_command(self.ansible_binary, options)

        return _run_subprocess_command(
            options.job_id,
            command,
            emit,
            cwd=options.working_dir,
            env=_merged_env(options.env_file, options.execution_env),
            start_message="Playbook process started",
            finish_message="Playbook process finished",
            on_process_started=on_process_started,
            line_mapper=_line_to_event,
        )


class AnsibleRunnerIntegration(BasePlaybookRunner):
    """Optional adapter that uses ansible-runner if available.

    This adapter is intentionally optional and can be enabled via INSTALLER_RUNNER_MODE=ansible-runner.
    """

    def run(
        self,
        options: PlaybookRunOptions,
        emit: EventCallback,
        on_process_started: Optional[ProcessStartedCallback] = None,
    ) -> int:
        try:
            import ansible_runner  # type: ignore
        except ImportError as exc:
            raise RuntimeError("ansible-runner is not installed. Use subprocess mode or install ansible-runner.") from exc

        tags = ",".join(options.tags) if options.tags else None
        skip_tags = ",".join(options.skip_tags) if options.skip_tags else None
        verbosity = _normalized_verbosity(options.verbosity)

        emit(_event(options.job_id, "status", "info", "ansible-runner started"))

        def event_handler(event: Dict[str, Any]) -> None:
            task_name = event.get("event_data", {}).get("task")
            host_name = event.get("event_data", {}).get("host")
            event_type = "task" if task_name else ("host" if host_name else "log")
            message = event.get("stdout") or event.get("event") or "ansible-runner event"
            emit(
                _event(
                    options.job_id,
                    event_type,
                    "info",
                    str(message),
                    task=task_name,
                    host=host_name,
                    raw=event,
                )
            )

        def status_handler(status_data: Dict[str, Any], _runner_config: Any) -> None:
            status = status_data.get("status") if isinstance(status_data, dict) else str(status_data)
            emit(_event(options.job_id, "status", "info", f"ansible-runner status: {status}", raw={"status": status}))

        runner_result = ansible_runner.run(
            private_data_dir=options.working_dir,
            playbook=options.playbook_path,
            inventory=options.inventory,
            limit=options.limit,
            verbosity=verbosity,
            tags=tags,
            skip_tags=skip_tags,
            extravars=options.extra_vars if options.extra_vars else None,
            event_handler=event_handler,
            status_handler=status_handler,
            json_mode=True,
            quiet=True,
            envvars=_merged_env(options.env_file, options.execution_env),
        )

        exit_code = int(getattr(runner_result, "rc", 1))
        emit(
            _event(
                options.job_id,
                "summary",
                "info" if exit_code == 0 else "error",
                "ansible-runner finished",
                raw={"exitCode": exit_code},
            )
        )
        return exit_code


def create_runner() -> BasePlaybookRunner:
    mode = os.getenv("INSTALLER_RUNNER_MODE", "subprocess").strip().lower()
    if mode == "ansible-runner":
        return AnsibleRunnerIntegration()
    return SubprocessPlaybookRunner(ansible_binary=os.getenv("ANSIBLE_PLAYBOOK_BIN", "ansible-playbook"))


def build_ansible_command(ansible_binary: str, options: PlaybookRunOptions) -> list[str]:
    command = [ansible_binary, options.playbook_path]

    if options.inventory:
        command.extend(["-i", options.inventory])
    if options.limit:
        command.extend(["--limit", options.limit])
    if options.tags:
        command.extend(["--tags", ",".join(options.tags)])
    if options.skip_tags:
        command.extend(["--skip-tags", ",".join(options.skip_tags)])
    if options.extra_vars:
        command.extend(["--extra-vars", json.dumps(options.extra_vars)])

    verbosity = _normalized_verbosity(options.verbosity)
    if verbosity > 0:
        command.append("-" + ("v" * verbosity))

    return command


def parse_csv_arg(value: Optional[str]) -> list[str]:
    if not value:
        return []
    return [item.strip() for item in value.split(",") if item.strip()]


def parse_json_arg(value: Optional[str]) -> Dict[str, Any]:
    if not value:
        return {}
    parsed = json.loads(value)
    if not isinstance(parsed, dict):
        raise ValueError("JSON arguments must be objects")
    return parsed


def parse_args(args: Optional[Iterable[str]] = None) -> PlaybookRunOptions:
    parser = argparse.ArgumentParser(description="Run installer playbook with shared runner core")
    parser.add_argument("--job-id", default="cli-job")
    parser.add_argument("--playbook", default=os.getenv("INSTALLER_PLAYBOOK", "main.yml"))
    parser.add_argument("--inventory", default=os.getenv("INSTALLER_INVENTORY"))
    parser.add_argument("--limit")
    parser.add_argument("--tags")
    parser.add_argument("--skip-tags")
    parser.add_argument("--verbosity", type=int, default=1)
    parser.add_argument("--extra-vars", dest="extra_vars")
    parser.add_argument("--execution-env", dest="execution_env")
    parser.add_argument("--working-dir", default=os.getenv("INSTALLER_WORKING_DIR", "/ansible/installer/src"))
    parser.add_argument("--env-file", default=os.getenv("INSTALLER_ENV_FILE", "/env/env.yml"))

    parsed = parser.parse_args(args=args)
    return PlaybookRunOptions(
        job_id=parsed.job_id,
        playbook_path=parsed.playbook,
        inventory=parsed.inventory,
        limit=parsed.limit,
        verbosity=parsed.verbosity,
        tags=parse_csv_arg(parsed.tags),
        skip_tags=parse_csv_arg(parsed.skip_tags),
        extra_vars=parse_json_arg(parsed.extra_vars),
        working_dir=parsed.working_dir,
        env_file=parsed.env_file,
        execution_env=parse_json_arg(parsed.execution_env),
    )


def _normalized_verbosity(value: int) -> int:
    return max(0, min(int(value), 4))


def _line_to_event(job_id: str, stream_name: str, line: str) -> Dict[str, Any]:
    task_match = re.match(r"^TASK \[(.+)]", line)
    if task_match:
        return _event(job_id, "task", "info", line, task=task_match.group(1), raw={"stream": stream_name})

    host_match = re.match(r"^(\S+)\s*:\s*(ok|changed|failed|unreachable)=", line)
    if host_match:
        level = "error" if host_match.group(2) in {"failed", "unreachable"} else "info"
        return _event(job_id, "host", level, line, host=host_match.group(1), raw={"stream": stream_name})

    if line.startswith("PLAY RECAP"):
        return _event(job_id, "summary", "info", line, raw={"stream": stream_name})

    level = "error" if stream_name == "stderr" else "info"
    return _event(job_id, "log", level, line, raw={"stream": stream_name})


def _run_subprocess_command(
    job_id: str,
    command: list[str],
    emit: EventCallback,
    cwd: Optional[str],
    env: Dict[str, str],
    start_message: str,
    finish_message: str,
    on_process_started: Optional[ProcessStartedCallback],
    line_mapper: Callable[[str, str, str], Dict[str, Any]],
) -> int:
    emit(_event(job_id, "status", "info", start_message, raw={"command": command}))

    process = subprocess.Popen(
        command,
        cwd=cwd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        bufsize=1,
        env=env,
    )

    if on_process_started is not None:
        on_process_started(SubprocessExecutionHandle(process))

    def consume(stream_name: str, pipe: Any) -> None:
        for line in iter(pipe.readline, ""):
            stripped = line.rstrip("\n")
            if not stripped:
                continue
            emit(line_mapper(job_id, stream_name, stripped))
        pipe.close()

    stdout_thread = threading.Thread(target=consume, args=("stdout", process.stdout), daemon=True)
    stderr_thread = threading.Thread(target=consume, args=("stderr", process.stderr), daemon=True)
    stdout_thread.start()
    stderr_thread.start()

    exit_code = process.wait()
    stdout_thread.join()
    stderr_thread.join()

    emit(
        _event(
            job_id,
            "summary",
            "info" if exit_code == 0 else "error",
            finish_message,
            raw={"exitCode": exit_code, "command": command},
        )
    )
    return exit_code


def _event(
    job_id: str,
    event_type: str,
    level: str,
    message: str,
    task: Optional[str] = None,
    host: Optional[str] = None,
    raw: Optional[Dict[str, Any]] = None,
) -> Dict[str, Any]:
    payload: Dict[str, Any] = {
        "jobId": job_id,
        "type": event_type,
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "level": level,
        "message": message,
        "raw": raw or {},
    }
    if task:
        payload["task"] = task
    if host:
        payload["host"] = host
    return payload


def _merged_env(env_file: Optional[str], overrides: Optional[Dict[str, str]] = None) -> Dict[str, str]:
    merged = dict(os.environ)
    if not env_file or not os.path.exists(env_file):
        return merged

    with open(env_file, "r", encoding="utf-8") as handle:
        for line in handle:
            entry = line.strip()
            if not entry or entry.startswith("#") or "=" not in entry:
                continue
            key, value = entry.split("=", 1)
            merged.setdefault(key.strip(), value.strip())

    if overrides:
        for key, value in overrides.items():
            if value is None:
                continue
            merged[str(key)] = str(value)

    return merged

