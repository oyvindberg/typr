#!/bin/bash

# Start SQL Server in the background
/opt/mssql/bin/sqlservr &

# Wait for SQL Server to start
echo "Waiting for SQL Server to start..."
for i in {1..60}; do
    /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -C -Q "SELECT 1" > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "SQL Server is ready"
        break
    fi
    echo "Waiting... ($i/60)"
    sleep 2
done

# Run init scripts
echo "Running init scripts..."
for f in /docker-entrypoint-initdb.d/*.sql; do
    if [ -f "$f" ]; then
        echo "Running $f"
        /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -C -i "$f"
    fi
done

echo "Initialization complete"

# Wait for SQL Server process
wait
