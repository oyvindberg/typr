#!/bin/bash
# Oracle init script - runs SQL files in order

for f in /container-entrypoint-initdb.d/*.sql; do
    if [ -f "$f" ]; then
        echo "Running $f..."
        sqlplus -s typr/typr_password@localhost/FREEPDB1 @"$f"
    fi
done
