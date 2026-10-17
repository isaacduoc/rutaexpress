
#!/bin/bash
set -euo pipefail

# ==========================================
# CARGAR CREDENCIALES
# ==========================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/.env"

API="http://localhost:15672/api"
AUTH="${RABBITMQ_USER}:${RABBITMQ_PASSWORD}"
VHOST="%2F"

echo "======================================"
echo " CONFIGURANDO RABBITMQ RUTAEXPRESS"
echo "======================================"

# ==========================================
# CREAR EXCHANGES
# ==========================================

echo "=== Creando exchanges ==="

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/exchanges/$VHOST/cmd.direct" \
  -d '{"type":"direct","durable":true,"auto_delete":false,"internal":false,"arguments":{}}'

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/exchanges/$VHOST/cmd.topic" \
  -d '{"type":"topic","durable":true,"auto_delete":false,"internal":false,"arguments":{}}'

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/exchanges/$VHOST/cmd.dead.dlx" \
  -d '{"type":"direct","durable":true,"auto_delete":false,"internal":false,"arguments":{}}'

# ==========================================
# CREAR DEAD LETTER QUEUES
# ==========================================

echo "=== Creando DLQ ==="

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/queues/$VHOST/q.cmd.email.dlq" \
  -d '{"durable":true,"auto_delete":false,"arguments":{}}'

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/queues/$VHOST/q.cmd.warehouse.dlq" \
  -d '{"durable":true,"auto_delete":false,"arguments":{}}'

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/queues/$VHOST/q.cmd.label.dlq" \
  -d '{"durable":true,"auto_delete":false,"arguments":{}}'

# ==========================================
# CREAR COLAS PRINCIPALES
# ==========================================

echo "=== Creando colas principales ==="

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/queues/$VHOST/q.cmd.email" \
  -d '{"durable":true,"auto_delete":false,"arguments":{"x-dead-letter-exchange":"cmd.dead.dlx","x-dead-letter-routing-key":"email.dlq"}}'

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/queues/$VHOST/q.cmd.warehouse" \
  -d '{"durable":true,"auto_delete":false,"arguments":{"x-dead-letter-exchange":"cmd.dead.dlx","x-dead-letter-routing-key":"warehouse.dlq"}}'

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/queues/$VHOST/q.cmd.label" \
  -d '{"durable":true,"auto_delete":false,"arguments":{"x-dead-letter-exchange":"cmd.dead.dlx","x-dead-letter-routing-key":"label.dlq"}}'

# ==========================================
# POLITICA TTL Y LIMITE DE MENSAJES
# ==========================================

echo "=== Configurando politica TTL y limite ==="

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X PUT "$API/policies/$VHOST/rutaexpress-ttl-max" \
  -d '{"pattern":"^q[.]cmd[.](email|warehouse|label)$","definition":{"message-ttl":86400000,"max-length":1000},"priority":0,"apply-to":"queues"}'

# ==========================================
# BINDINGS DLQ
# ==========================================

echo "=== Creando bindings DLQ ==="

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.dead.dlx/q/q.cmd.email.dlq" \
  -d '{"routing_key":"email.dlq","arguments":{}}'

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.dead.dlx/q/q.cmd.warehouse.dlq" \
  -d '{"routing_key":"warehouse.dlq","arguments":{}}'

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.dead.dlx/q/q.cmd.label.dlq" \
  -d '{"routing_key":"label.dlq","arguments":{}}'

# ==========================================
# BINDINGS DIRECT
# ==========================================

echo "=== Creando bindings direct ==="

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.direct/q/q.cmd.email" \
  -d '{"routing_key":"email.send","arguments":{}}'

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.direct/q/q.cmd.warehouse" \
  -d '{"routing_key":"warehouse.ticket","arguments":{}}'

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.direct/q/q.cmd.label" \
  -d '{"routing_key":"label.gen","arguments":{}}'

# ==========================================
# BINDINGS TOPIC
# ==========================================

echo "=== Creando bindings topic ==="

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.topic/q/q.cmd.email" \
  -d '{"routing_key":"email.*","arguments":{}}'

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.topic/q/q.cmd.warehouse" \
  -d '{"routing_key":"warehouse.#","arguments":{}}'

curl -fSs -u "$AUTH" \
  -H "content-type: application/json" \
  -X POST "$API/bindings/$VHOST/e/cmd.topic/q/q.cmd.label" \
  -d '{"routing_key":"label.*","arguments":{}}'

# ==========================================
# FINALIZACION
# ==========================================

echo ""
echo "======================================"
echo " TOPOLOGIA RABBITMQ CREADA CORRECTAMENTE"
echo "======================================"
echo " Exchanges: 3"
echo " Colas principales: 3"
echo " Dead Letter Queues: 3"
echo " Bindings: 9"
echo " TTL: 24 horas"
echo " Limite: 1000 mensajes por cola"
echo "======================================"
