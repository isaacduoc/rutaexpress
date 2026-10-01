#!/bin/bash
set -e

# Cargar credenciales desde el .env de RabbitMQ
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/.env"

API="http://localhost:15672/api"
AUTH="${RABBITMQ_USER}:${RABBITMQ_PASSWORD}"
VHOST="%2F"

echo "=== Creando exchanges ==="

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/exchanges/$VHOST/cmd.direct" \
  -d '{"type":"direct","durable":true,"auto_delete":false,"internal":false,"arguments":{}}'

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/exchanges/$VHOST/cmd.topic" \
  -d '{"type":"topic","durable":true,"auto_delete":false,"internal":false,"arguments":{}}'

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/exchanges/$VHOST/cmd.dead.dlx" \
  -d '{"type":"direct","durable":true,"auto_delete":false,"internal":false,"arguments":{}}'

echo "=== Creando DLQ ==="

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/queues/$VHOST/q.cmd.email.dlq" \
  -d '{"durable":true,"auto_delete":false,"arguments":{}}'

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/queues/$VHOST/q.cmd.warehouse.dlq" \
  -d '{"durable":true,"auto_delete":false,"arguments":{}}'

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/queues/$VHOST/q.cmd.label.dlq" \
  -d '{"durable":true,"auto_delete":false,"arguments":{}}'

echo "=== Creando colas principales ==="

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/queues/$VHOST/q.cmd.email" \
  -d '{"durable":true,"auto_delete":false,"arguments":{"x-dead-letter-exchange":"cmd.dead.dlx","x-dead-letter-routing-key":"email.dlq"}}'

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/queues/$VHOST/q.cmd.warehouse" \
  -d '{"durable":true,"auto_delete":false,"arguments":{"x-dead-letter-exchange":"cmd.dead.dlx","x-dead-letter-routing-key":"warehouse.dlq"}}'

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/queues/$VHOST/q.cmd.label" \
  -d '{"durable":true,"auto_delete":false,"arguments":{"x-dead-letter-exchange":"cmd.dead.dlx","x-dead-letter-routing-key":"label.dlq"}}'

echo "=== Bindings DLQ ==="

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.dead.dlx/q/q.cmd.email.dlq" \
  -d '{"routing_key":"email.dlq","arguments":{}}'

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.dead.dlx/q/q.cmd.warehouse.dlq" \
  -d '{"routing_key":"warehouse.dlq","arguments":{}}'

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.dead.dlx/q/q.cmd.label.dlq" \
  -d '{"routing_key":"label.dlq","arguments":{}}'

echo "=== Bindings direct ==="

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.direct/q/q.cmd.email" \
  -d '{"routing_key":"email.send","arguments":{}}'

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.direct/q/q.cmd.warehouse" \
  -d '{"routing_key":"warehouse.ticket","arguments":{}}'

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.direct/q/q.cmd.label" \
  -d '{"routing_key":"label.gen","arguments":{}}'

echo "=== Bindings topic ==="

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.topic/q/q.cmd.email" \
  -d '{"routing_key":"email.*","arguments":{}}'

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.topic/q/q.cmd.warehouse" \
  -d '{"routing_key":"warehouse.#","arguments":{}}'

curl -s -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.topic/q/q.cmd.label" \
  -d '{"routing_key":"label.*","arguments":{}}'

echo
echo "======================================"
echo " Topologia RabbitMQ creada correctamente"
echo "======================================"
