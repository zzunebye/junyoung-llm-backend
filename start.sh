#!/usr/bin/env bash
set -euo pipefail

# run this shell file will spin up the server, DB.
if [ -f .env.local ]; then
    docker compose --env-file .env.local up --build -d
else
    docker compose up --build -d
fi

echo
echo "Services started."
echo "  API:  http://localhost:8080"
echo "  Logs: docker compose logs -f app"
echo "  Stop: docker compose down"
