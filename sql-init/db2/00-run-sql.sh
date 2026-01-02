#!/bin/bash
# DB2 init script - runs SQL files in order

for f in /var/custom/*.sql; do
    if [ -f "$f" ]; then
        echo "Running $f..."
        db2 -tvf "$f"
    fi
done
