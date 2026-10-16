#!/bin/bash
# Script to stop all microservices

for service in auth customer staff driver payment; do
  systemctl --user stop "ralsei-${service}" 2>/dev/null || true
  systemctl --user reset-failed "ralsei-${service}" 2>/dev/null || true
  if [ -f "${service}-service.pid" ]; then
    PID=$(cat "${service}-service.pid")
    echo "Stopping ${service}-service (PID: $PID)..."
    kill $PID 2>/dev/null || true
    rm -f "${service}-service.pid"
  else
    echo "Stopped ${service}-service."
  fi
done

echo "Attempting to kill any lingering spring-boot processes..."
pkill -f "spring-boot:run" 2>/dev/null || true
pkill -f "auth-service.*\.jar|customer-service.*\.jar|staff-service.*\.jar|driver-service.*\.jar|payment-service.*\.jar" 2>/dev/null || true
pkill -f "com.ralsei.auth.application.Application|com.ralsei.customer.application.Application|com.ralsei.staff.application.Application|com.ralsei.driver.application.Application|com.ralsei.payment.PaymentApplication" 2>/dev/null || true

echo "All services stopped."
