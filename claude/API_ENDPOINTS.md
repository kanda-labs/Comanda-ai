# API Endpoints Documentation - Comanda AI

Este documento documenta os endpoints da API REST do CommanderAPI utilizados pela aplicação móvel.

## Base URL
```
http://192.168.1.4:8081/api/v1
```

## 📋 Tables Endpoints

### GET /tables
Busca todas as mesas

**Response:**
```json
[
  {
    "id": 1,
    "number": 1,
    "status": "OPEN|CLOSED|ON_PAYMENT",
    "createdAt": "2025-01-15T10:30:00",
    "billId": 123,
    "orders": []
  }
]
```

### GET /tables/{id}
Busca uma mesa específica por ID

**Parameters:**
- `id` (path): ID da mesa

**Response:**
```json
{
  "id": 1,
  "number": 1,
  "status": "OPEN",
  "createdAt": "2025-01-15T10:30:00",
  "billId": 123,
  "orders": [
    {
      "id": 1,
      "billId": 123,
      "tableNumber": 1,
      "items": [],
      "status": "OPEN|GRANTED|CANCELED",
      "createdAt": "2025-01-15T10:35:00"
    }
  ]
}
```

### PUT /tables/{id}
Atualiza uma mesa (usado para mudança de status)

**Parameters:**
- `id` (path): ID da mesa

**Request Body:**
```json
{
  "billId": 123,      // opcional
  "status": "ON_PAYMENT"  // opcional: OPEN|CLOSED|ON_PAYMENT
}
```

**Response:**
```json
{
  "id": 1,
  "number": 1,
  "status": "ON_PAYMENT",
  "createdAt": "2025-01-15T10:30:00",
  "billId": 123,
  "orders": []
}
```

**Uso no app:**
- Fechar conta: `status: "ON_PAYMENT"`

## 💰 Bills Endpoints

### POST /bills
Cria uma nova conta (bill) e automaticamente atualiza o status da mesa para OPEN

**Request Body:**
```json
{
  "tableId": 1,
  "tableNumber": 1
}
```

**Response:**
- Status: 201 Created

**Efeitos colaterais:**
- Cria uma nova bill com status OPEN
- Atualiza a mesa para status OPEN
- Associa a bill criada à mesa

**Uso no app:**
- Abrir conta: cria bill e muda mesa de FREE para OCCUPIED

## 🍽️ Items Endpoints

### GET /items
Busca todos os itens do menu

**Response:**
```json
[
  {
    "id": 1,
    "name": "Hambúrguer",
    "description": "Hambúrguer artesanal",
    "price": 25.90,
    "category": "Lanches",
    "status": "AVAILABLE|UNAVAILABLE"
  }
]
```

## 📝 Orders Endpoints

### POST /orders
Cria um novo pedido

**Request Body:**
```json
{
  "tableId": 1,
  "billId": 123,
  "userName": "leonardo-paixao",
  "items": [
    {
      "itemId": 1,
      "name": "Hambúrguer",
      "count": 2,
      "observation": "Sem cebola"
    }
  ]
}
```

**Response:**
```json
{
  "id": 1,
  "billId": 123,
  "tableNumber": 1,
  "userName": "leonardo-paixao",
  "items": [],
  "status": "OPEN",
  "createdAt": "2025-01-15T10:35:00"
}
```

## 🔄 Table Status Flow

### Estados da Mesa
1. **FREE** (CLOSED no backend) - Mesa livre
2. **OCCUPIED** (OPEN no backend) - Mesa ocupada com conta aberta
3. **ON_PAYMENT** - Mesa em processo de pagamento

### Fluxo de Estados
```
FREE → [Abrir conta] → OCCUPIED → [Fechar conta] → ON_PAYMENT
```

### Ações por Estado

| Status | Badge | Botão Primário | Botão Secundário | Endpoint Usado |
|---------|-------|----------------|------------------|----------------|
| FREE | 🟢 Verde "Livre" | "Abrir conta" | "Voltar" | POST /bills |
| OCCUPIED | 🟡 Amarelo "Ocupada" | "Fazer pedido" | "Fechar conta" | PUT /tables/{id} |
| ON_PAYMENT | 🟠 Laranja "Em pagamento" | - | "Voltar" | - |

## 🏗️ Arquitetura Frontend

### Camadas
1. **CommanderApi** - Interface Ktorfit com endpoints
2. **TablesRepository** - Interface do domínio
3. **TablesRepositoryImp** - Implementação que chama a API
4. **TablesDetailsViewModel** - Lógica de negócio e estado
5. **TableDetailsScreen** - UI e navegação

### Request/Response Models
```kotlin
// Requests
data class CreateBillRequest(
    val tableId: Int?,
    val tableNumber: Int?
)

data class UpdateTableRequest(
    val billId: Int? = null,
    val status: TableStatus? = null
)

// Domain Models
enum class TableStatus { 
    OCCUPIED,    // "Ocupada" - equivale a OPEN no backend
    FREE,        // "Livre" - equivale a CLOSED no backend  
    ON_PAYMENT   // "Em pagamento"
}
```

## 🎯 Uso nos Métodos

### Abrir Conta
```kotlin
// Frontend chama
repository.openTable(tableId, tableNumber)
// Que faz
POST /bills { tableId, tableNumber }
// Backend automaticamente
// 1. Cria bill com status OPEN
// 2. Atualiza mesa para status OPEN
// 3. Associa bill à mesa
```

### Fechar Conta  
```kotlin
// Frontend chama
repository.closeTable(tableId)
// Que faz
PUT /tables/{tableId} { status: "ON_PAYMENT" }
// Backend atualiza status da mesa
```

### Fazer Pedido
```kotlin
// Navegação local - não usa API
navigator.push(OrderScreen(tableId, tableNumber, billId))
```

## 🍳 Kitchen Endpoints (NEW)

Nova seção de endpoints específicos para o módulo de cozinha.

### GET /kitchen/orders
Busca todos os pedidos ativos para a cozinha

**Response:**
```json
[
  {
    "id": 1,
    "tableNumber": 5,
    "userName": "leonardo-paixao",
    "items": [
      {
        "itemId": 1,
        "name": "Hambúrguer",
        "category": "SKEWER",
        "count": 2,
        "observation": "Sem cebola",
        "unitStatuses": [
          {
            "unitIndex": 0,
            "status": "PENDING"
          },
          {
            "unitIndex": 1,
            "status": "DELIVERED"
          }
        ]
      }
    ],
    "status": "OPEN",
    "createdAt": "2025-01-15T10:35:00"
  }
]
```

### GET /kitchen/orders/delivered
Busca todos os pedidos entregues para a cozinha

**Response:** Mesmo formato do endpoint `/kitchen/orders`

### PUT /kitchen/orders/{orderId}/items/{itemId}/unit/{unitIndex}
Atualiza o status de uma unidade específica de um item

**Parameters:**
- `orderId` (path): ID do pedido
- `itemId` (path): ID do item  
- `unitIndex` (path): Índice da unidade (0, 1, 2...)

**Request Body:**
```json
{
  "status": "PENDING|DELIVERED|CANCELED"
}
```

**Status Values:**
- `PENDING`: Pendente, aguardando processamento ou em preparo
- `DELIVERED`: Entregue ao cliente
- `CANCELED`: Cancelado

**Response:**
```json
{
  "success": true
}
```

### PUT /kitchen/orders/{orderId}/deliver
Marca um pedido completo como entregue

**Parameters:**
- `orderId` (path): ID do pedido

**Response:**
```json
{
  "success": true
}
```

### PUT /kitchen/orders/{orderId}/items/{itemId}/deliver
Marca todas as unidades de um item como entregues

**Parameters:**
- `orderId` (path): ID do pedido
- `itemId` (path): ID do item

**Response:**
```json
{
  "success": true
}
```

### GET /kitchen/orders/{orderId}/items/{itemId}/statuses
Obtém o detalhamento de status de todas as unidades de um item

**Parameters:**
- `orderId` (path): ID do pedido
- `itemId` (path): ID do item

**Response:**
```json
[
  {
    "unitIndex": 0,
    "status": "PENDING",
    "updatedAt": "2025-01-15T10:40:00",
    "updatedBy": "kitchen-user"
  },
  {
    "unitIndex": 1,
    "status": "DELIVERED",
    "updatedAt": "2025-01-15T10:45:00",
    "updatedBy": "kitchen-user"
  }
]
```

### GET /kitchen/events (SSE)
Endpoint Server-Sent Events para atualizações em tempo real da cozinha

**Response Events:**
- `connection`: Confirmação de conexão estabelecida
- `kitchen_orders`: Atualização da lista de pedidos da cozinha
- `heartbeat`: Sinal de vida da conexão
- `error`: Mensagens de erro

**Event Data:**
```json
{
  "type": "kitchen_orders_update",
  "orders": [...],
  "timestamp": 1642248000000
}
```

### POST /kitchen/events/trigger
Dispara uma atualização manual para a cozinha

**Response:**
```json
{
  "message": "Kitchen update triggered"
}
```

### Server-Sent Events
- `GET /api/v1/orders/sse` - Real-time order updates
- `GET /api/v1/kitchen/events` - Real-time kitchen updates (NEW)

## 🌐 Network Configuration (NEW)

O projeto agora utiliza o módulo `network` para centralizar toda a configuração de rede.

### Configuração Centralizada
- **Módulo network**: Único local para configurar IPs e portas
- **Separação automática**: URLs diferentes para debug/production baseadas no build type
- **Configuração por plataforma**: Android (buildConfig) e iOS (código nativo)

### Mudança de IP
**Para alterar o IP de todo o sistema:**

1. **Android**: Editar `/network/build.gradle.kts`
```kotlin
buildConfigField("String", "BASE_IP", "\"SEU_IP_AQUI\"")
```

2. **iOS**: Editar `/network/src/iosMain/kotlin/.../NetworkConfig.kt`
```kotlin
actual val baseIp: String = "SEU_IP_AQUI"
```

### Ambientes Separados

| Ambiente | Porta | Database | Descrição |
|----------|-------|----------|-----------|
| **PRODUCTION** | 8081 | `data.db` | Ambiente de produção |
| **DEBUG** | 8082 | `data-debug.db` | Ambiente de desenvolvimento |

### Scripts de Gerenciamento
```bash
# Produção
./start-production.sh

# Debug 
./start-debug.sh
./start-debug-with-prod-data.sh

# Gerenciamento de banco
./manage-databases.sh
./copy-prod-to-debug.sh
```

**API Documentation:** Available at `/swagger-ui` when server is running

## 🚦 Status Reference

Para informações detalhadas sobre todos os status do sistema, consulte:
- **`STATUS_DEFINITIONS.md`** - Documentação completa de status para Tables, Bills, Orders e Items
- Inclui fluxos de estado, cores da UI, regras de negócio e guias de migração

---

*Documentação atualizada em 25/08/2025 - Comanda AI v2.0*
*Inclui: Kitchen Module, Network Module, Environment Separation, Status Management*