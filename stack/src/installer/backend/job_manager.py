from __future__ import annotations

import asyncio
import threading
import uuid
from dataclasses import dataclass, field
from datetime import datetime, timezone
from typing import Any, Dict, Optional

TERMINAL_STATUSES = {"successful", "failed", "canceled"}


@dataclass
class JobRecord:
    job_id: str
    operation: str = "install"
    status: str = "queued"
    start_time: Optional[str] = None
    end_time: Optional[str] = None
    exit_code: Optional[int] = None
    cancel_requested: bool = False
    request: Dict[str, Any] = field(default_factory=dict)
    logs: list[Dict[str, Any]] = field(default_factory=list)

    def to_dict(self) -> Dict[str, Any]:
        return {
            "jobId": self.job_id,
            "operation": self.operation,
            "status": self.status,
            "startTime": self.start_time,
            "endTime": self.end_time,
            "exitCode": self.exit_code,
            "cancelRequested": self.cancel_requested,
            "request": self.request,
            "logs": self.logs,
        }


class JobRepository:
    def create(self, request: Dict[str, Any], operation: str) -> JobRecord:
        raise NotImplementedError

    def get(self, job_id: str) -> Optional[JobRecord]:
        raise NotImplementedError

    def save(self, job: JobRecord) -> None:
        raise NotImplementedError

    def list(self) -> list[JobRecord]:
        raise NotImplementedError


class InMemoryJobRepository(JobRepository):
    def __init__(self) -> None:
        self._jobs: Dict[str, JobRecord] = {}
        self._lock = threading.RLock()

    def create(self, request: Dict[str, Any], operation: str) -> JobRecord:
        with self._lock:
            job_id = str(uuid.uuid4())
            job = JobRecord(job_id=job_id, request=request, operation=operation)
            self._jobs[job_id] = job
            return job

    def get(self, job_id: str) -> Optional[JobRecord]:
        with self._lock:
            return self._jobs.get(job_id)

    def save(self, job: JobRecord) -> None:
        with self._lock:
            self._jobs[job.job_id] = job

    def list(self) -> list[JobRecord]:
        with self._lock:
            return list(self._jobs.values())


class JobManager:
    def __init__(self, repository: Optional[JobRepository] = None) -> None:
        self.repository = repository or InMemoryJobRepository()
        self._subscribers: Dict[str, set[asyncio.Queue]] = {}
        self._subscriber_lock = threading.RLock()
        self._execution_handles: Dict[str, Any] = {}
        self._execution_lock = threading.RLock()
        self._shutdown_lock = threading.RLock()
        self._shutdown = False

    def create_job(self, request: Dict[str, Any], operation: str = "install") -> JobRecord:
        return self.repository.create(request, operation)

    def get_job(self, job_id: str) -> Optional[JobRecord]:
        return self.repository.get(job_id)

    def get_active_job(self) -> Optional[JobRecord]:
        for job in self.repository.list():
            if job.status in {"queued", "running"}:
                return job
        return None

    def get_latest_job(self) -> Optional[JobRecord]:
        jobs = self.repository.list()
        if not jobs:
            return None
        return jobs[-1]

    def has_active_job(self) -> bool:
        for job in self.repository.list():
            if job.status in {"queued", "running"}:
                return True
        return False

    def is_cancel_requested(self, job_id: str) -> bool:
        job = self.repository.get(job_id)
        return bool(job and job.cancel_requested)

    def register_execution_handle(self, job_id: str, handle: Any) -> None:
        with self._execution_lock:
            self._execution_handles[job_id] = handle

    def clear_execution_handle(self, job_id: str) -> None:
        with self._execution_lock:
            self._execution_handles.pop(job_id, None)

    async def subscribe(self, job_id: str) -> asyncio.Queue:
        queue: asyncio.Queue = asyncio.Queue()
        with self._subscriber_lock:
            if self._shutdown:
                await queue.put(None)
                return queue
            self._subscribers.setdefault(job_id, set()).add(queue)
        return queue

    async def unsubscribe(self, job_id: str, queue: asyncio.Queue) -> None:
        with self._subscriber_lock:
            if job_id in self._subscribers:
                self._subscribers[job_id].discard(queue)
                if not self._subscribers[job_id]:
                    del self._subscribers[job_id]

    async def append_event(self, job_id: str, event: Dict[str, Any]) -> None:
        job = self.repository.get(job_id)
        if not job:
            return

        job.logs.append(event)
        self.repository.save(job)

        await self._broadcast(job_id, event)

    async def mark_running(self, job_id: str) -> None:
        await self._set_status(job_id, "running")

    async def mark_finished(self, job_id: str, exit_code: int) -> None:
        status = "successful" if exit_code == 0 else "failed"
        await self._set_status(job_id, status, exit_code)

    async def mark_canceled(self, job_id: str, exit_code: Optional[int] = None) -> None:
        await self._set_status(job_id, "canceled", exit_code)

    async def mark_failed_to_start(self, job_id: str) -> None:
        await self._set_status(job_id, "failed", exit_code=1)

    async def request_cancel(self, job_id: str) -> Optional[JobRecord]:
        job = self.repository.get(job_id)
        if not job:
            return None

        if job.status in TERMINAL_STATUSES:
            return job

        if not job.cancel_requested:
            job.cancel_requested = True
            self.repository.save(job)
            await self._broadcast(
                job_id,
                {
                    "jobId": job_id,
                    "type": "status",
                    "timestamp": datetime.now(timezone.utc).isoformat(),
                    "level": "warning",
                    "message": "Cancellation requested",
                    "raw": {
                        "oldStatus": job.status,
                        "newStatus": job.status,
                        "exitCode": job.exit_code,
                        "cancelRequested": True,
                        "operation": job.operation,
                    },
                },
            )

        with self._execution_lock:
            handle = self._execution_handles.get(job_id)

        if handle is not None:
            await asyncio.to_thread(handle.cancel)

        if job.status == "queued":
            await self.mark_canceled(job_id)

        return self.repository.get(job_id)

    async def shutdown(self) -> None:
        with self._shutdown_lock:
            self._shutdown = True

        with self._subscriber_lock:
            subscribers = [queue for queues in self._subscribers.values() for queue in queues]
            self._subscribers.clear()

        for queue in subscribers:
            await queue.put(None)

        with self._execution_lock:
            handles = list(self._execution_handles.values())
            self._execution_handles.clear()

        for handle in handles:
            await asyncio.to_thread(handle.cancel)

    def submit_coro_from_thread(self, coro: asyncio.Future, loop: asyncio.AbstractEventLoop):
        return asyncio.run_coroutine_threadsafe(coro, loop)

    async def _set_status(self, job_id: str, status: str, exit_code: Optional[int] = None) -> None:
        job = self.repository.get(job_id)
        if not job:
            return

        old_status = job.status
        if not _is_valid_transition(old_status, status):
            raise ValueError(f"Invalid job transition from {old_status} to {status}")

        job.status = status
        now = datetime.now(timezone.utc).isoformat()

        if status == "running" and not job.start_time:
            job.start_time = now

        if status in TERMINAL_STATUSES:
            job.end_time = now
            job.exit_code = exit_code
            self.clear_execution_handle(job_id)

        self.repository.save(job)

        await self._broadcast(
            job_id,
            {
                "jobId": job_id,
                "type": "status",
                "timestamp": now,
                "level": "error" if status == "failed" else ("warning" if status == "canceled" else "info"),
                "message": f"Job status changed to {status}",
                "raw": {
                    "oldStatus": old_status,
                    "newStatus": status,
                    "exitCode": exit_code,
                    "cancelRequested": job.cancel_requested,
                    "operation": job.operation,
                },
            },
        )

    async def _broadcast(self, job_id: str, event: Dict[str, Any]) -> None:
        with self._subscriber_lock:
            subscribers = list(self._subscribers.get(job_id, set()))

        for queue in subscribers:
            await queue.put(event)


def _is_valid_transition(old: str, new: str) -> bool:
    transitions = {
        "queued": {"running", "failed", "canceled"},
        "running": {"successful", "failed", "canceled"},
        "successful": set(),
        "failed": set(),
        "canceled": set(),
    }
    return new in transitions.get(old, set())

