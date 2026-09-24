#!/bin/bash
set -e

# Start SQL Server in the background
/opt/mssql/bin/sqlservr &
pid=$!

SQLCMD="/opt/mssql-tools18/bin/sqlcmd -C"
if [ ! -f /opt/mssql-tools18/bin/sqlcmd ]; then
    SQLCMD="/opt/mssql-tools/bin/sqlcmd"
fi

SA_PASS="${MSSQL_SA_PASSWORD:-01102006Duc.}"

echo "Waiting for SQL Server to boot..."
for i in {1..60}; do
    if $SQLCMD -S localhost -U sa -P "$SA_PASS" -Q "SELECT 1" > /dev/null 2>&1; then
        echo "SQL Server is ready."
        break
    fi
    sleep 2
done

# Check if database VeXeDB exists
if ! $SQLCMD -S localhost -U sa -P "$SA_PASS" -Q "SELECT name FROM sys.databases WHERE name = 'VeXeDB'" 2>/dev/null | grep -q "VeXeDB"; then
    echo "Initializing VeXeDB database..."
    if [ -f /usr/src/app/ddl.sql ]; then
        echo "Running ddl.sql..."
        $SQLCMD -S localhost -U sa -P "$SA_PASS" -i /usr/src/app/ddl.sql
    fi
    if [ -f /usr/src/app/Procedure.sql ]; then
        echo "Running Procedure.sql..."
        $SQLCMD -S localhost -U sa -P "$SA_PASS" -i /usr/src/app/Procedure.sql
    fi
    if [ -f /usr/src/app/fakedata1.sql ]; then
        echo "Running fakedata1.sql..."
        $SQLCMD -S localhost -U sa -P "$SA_PASS" -i /usr/src/app/fakedata1.sql
    fi
    echo "VeXeDB database initialized successfully."
else
    echo "VeXeDB database already exists."
fi

# Wait for background SQL Server process
wait $pid
