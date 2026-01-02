#!/bin/bash
# DB2 init script - runs SQL files from sql/ subdirectory
# Must run as db2inst1 user

su - db2inst1 -c "
db2 connect to typr
for f in /var/custom/sql/*.sql; do
    if [ -f \"\$f\" ]; then
        echo \"Running \$f...\"
        db2 -tvf \"\$f\"
    fi
done
db2 connect reset
"
