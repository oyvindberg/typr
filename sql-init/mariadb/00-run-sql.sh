#!/bin/bash
# MariaDB init script - runs SQL files in order

for f in /docker-entrypoint-initdb.d/*.sql; do
    if [ -f "$f" ]; then
        echo "Running $f..."
        mariadb -u root -p"$MARIADB_ROOT_PASSWORD" -D typr < "$f"
    fi
done
