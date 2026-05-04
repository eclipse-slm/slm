from __future__ import annotations
import sys
from typing import Any, Dict

from backend.runner import create_runner, parse_args


def main() -> int:
    options = parse_args()
    runner = create_runner()

    def emit(event: Dict[str, Any]) -> None:
        message = event.get("message")
        if message is None:
            return
        print(str(message), flush=True)

    return runner.run(options, emit=emit)


if __name__ == "__main__":
    sys.exit(main())

