from __future__ import annotations

import json
import sys
from typing import Any, Dict

from backend.runner import create_runner, parse_args


def main() -> int:
    options = parse_args()
    runner = create_runner()

    def emit(event: Dict[str, Any]) -> None:
        print(json.dumps(event), flush=True)

    return runner.run(options, emit=emit)


if __name__ == "__main__":
    sys.exit(main())

