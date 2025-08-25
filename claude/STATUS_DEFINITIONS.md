# 📊 Status Definitions - Comanda AI

Este documento define todos os status utilizados no sistema Comanda-ai para mesas, bills, orders e items.

## 🍽️ Table Status (Mesa)

### Backend: `TableStatus`
```kotlin
enum class TableStatus { OPEN, CLOSED, ON_PAYMENT }
```

### Frontend: `TableStatus` 
```kotlin
enum class TableStatus(val presentationName: String) {
    @SerialName("OPEN")
    OCCUPIED("Ocupada"),        // Mesa com conta ativa
    @SerialName("CLOSED") 
    FREE("Livre"),              // Mesa disponível
    ON_PAYMENT("Em pagamento")   // Mesa em processo de pagamento
}
```

### Status Mapping (Backend ↔ Frontend)
| Backend Status | Frontend Status | Display Name | Descrição |
|---------------|-----------------|--------------|-----------|
| `CLOSED` | `FREE` | 🟢 "Livre" | Mesa disponível para ocupação |
| `OPEN` | `OCCUPIED` | 🟡 "Ocupada" | Mesa com conta ativa e pedidos |
| `ON_PAYMENT` | `ON_PAYMENT` | 🟠 "Em pagamento" | Mesa em processo de fechamento |

### Status Flow
```
FREE → [Abrir conta] → OCCUPIED → [Fechar conta] → ON_PAYMENT
  ↑                                                    ↓
  ← ← ← ← ← ← [Finalizar pagamento] ← ← ← ← ← ← ← ← ← ←
```

### Actions by Status
| Status | Primary Action | Secondary Action | Endpoint |
|--------|---------------|------------------|----------|
| **FREE** | "Abrir conta" | "Voltar" | `POST /bills` |
| **OCCUPIED** | "Fazer pedido" | "Fechar conta" | `PUT /tables/{id}` |
| **ON_PAYMENT** | - | "Voltar" | - |

---

## 💰 Bill Status (Conta)

### Definition: `BillStatus`
```kotlin
enum class BillStatus { PAID, SCAM, OPEN, CANCELED }
```

| Status | Descrição | Quando Usar |
|--------|-----------|-------------|
| `OPEN` | Conta ativa, aceitando pedidos | Conta aberta e operacional |
| `PAID` | Conta paga e finalizada | Pagamento confirmado |
| `CANCELED` | Conta cancelada | Conta cancelada antes do pagamento |
| `SCAM` | Conta com problemas/fraude | Situações excepcionais |

### Status Flow
```
OPEN → [Pagar conta] → PAID
  ↓
  → [Cancelar conta] → CANCELED
  ↓
  → [Problema detectado] → SCAM
```

---

## 📝 Order Status (Pedido)

### Definition: `OrderStatus`
```kotlin
enum class OrderStatus { PENDING, DELIVERED, CANCELED }
```

| Status | Descrição | Quando Usar |
|--------|-----------|-------------|
| `PENDING` | Pedido criado, aguardando processamento | Pedido recém-criado |
| `DELIVERED` | Pedido entregue/finalizado | Pedido completamente entregue |
| `CANCELED` | Pedido cancelado | Pedido cancelado antes da entrega |

### Status Flow
```
PENDING → [Entregar pedido] → DELIVERED
  ↓
  → [Cancelar pedido] → CANCELED
```

---

## 🍳 Item Status (Item de Pedido)

### Definition: `ItemStatus`
```kotlin
enum class ItemStatus { 
    PENDING,       // Pendente - item aguardando processamento
    DELIVERED,     // Entregue - item finalizado e entregue ao cliente
    CANCELED       // Cancelado - item cancelado
}
```

### Simplified Status System
| Status | Display Name | Descrição | Cor | Quando Usar |
|--------|-------------|-----------|-----|-------------|
| `PENDING` | Pendente | Item aguardando processamento/entrega | 🔴 Vermelho | Item recém-criado ou em preparo |
| `DELIVERED` | Entregue | Item entregue ao cliente | 🟢 Verde | Item servido na mesa |
| `CANCELED` | Cancelado | Item cancelado | ⚫ Cinza | Item cancelado pelo cliente/cozinha |

### Simplified Status Flow
```
PENDING → [Entregar] → DELIVERED
  ↓
  → [Cancelar] → CANCELED
```

### Unit Status Tracking
O sistema suporta controle granular de status por unidade individual:

```json
{
  "itemId": 1,
  "name": "Hambúrguer",
  "totalCount": 3,
  "unitStatuses": [
    {
      "unitIndex": 0,
      "status": "DELIVERED",
      "updatedAt": 1642248000000,
      "updatedBy": "kitchen-user"
    },
    {
      "unitIndex": 1,
      "status": "DELIVERED",
      "updatedAt": 1642248060000,
      "updatedBy": "kitchen-user"
    },
    {
      "unitIndex": 2,
      "status": "PENDING",
      "updatedAt": 1642248120000,
      "updatedBy": "kitchen-user"
    }
  ]
}
```

---

## 🎨 UI Status Colors

### Kitchen Screen Colors
```kotlin
val statusColors = mapOf(
    ItemStatus.PENDING to Color.Red,        // 🔴 Pendente
    ItemStatus.DELIVERED to Color.Green,    // 🟢 Entregue
    ItemStatus.CANCELED to Color.Gray       // ⚫ Cancelado
)
```

### Table Status Colors
```kotlin
val tableStatusColors = mapOf(
    TableStatus.FREE to Color.Green,        // 🟢 Livre
    TableStatus.OCCUPIED to Color.Yellow,   // 🟡 Ocupada
    TableStatus.ON_PAYMENT to Color.Orange  // 🟠 Em Pagamento
)
```

---

## 🔄 Status Integration Points

### API Endpoints Status Usage

#### Kitchen Endpoints
- `PUT /kitchen/orders/{orderId}/items/{itemId}/unit/{unitIndex}` - Atualiza status individual de unidade
- `PUT /kitchen/orders/{orderId}/deliver` - Marca pedido como entregue (todos os items → DELIVERED)
- `PUT /kitchen/orders/{orderId}/items/{itemId}/deliver` - Marca item como entregue (todas unidades → DELIVERED)

#### Table Endpoints  
- `PUT /tables/{id}` - Atualiza status da mesa
- `POST /bills` - Cria conta e automaticamente atualiza mesa para OPEN

#### Order Endpoints
- `POST /orders` - Cria pedido com status OPEN
- Status do pedido é atualizado automaticamente baseado no status dos items

### Database Schema Status
```sql
-- Tables
CREATE TABLE tables (
    status VARCHAR(32)  -- OPEN, CLOSED, ON_PAYMENT
);

-- Bills  
CREATE TABLE bills (
    status VARCHAR(32)  -- PAID, SCAM, OPEN, CANCELED
);

-- Orders
CREATE TABLE orders (
    status VARCHAR(32)  -- GRANTED, OPEN, CANCELED
);

-- Order Items (Legacy)
CREATE TABLE order_items (
    status VARCHAR(32)  -- GRANTED, OPEN, CANCELED, IN_PRODUCTION, COMPLETED, DELIVERED
);

-- Order Item Statuses (New - Unit Tracking)
CREATE TABLE order_item_statuses (
    unit_index INTEGER,
    status VARCHAR(32),  -- Individual unit status
    updated_at BIGINT,
    updated_by VARCHAR(255)
);
```

---

## 🧭 Status Migration Guide

### From Legacy to New System

#### Item Status Migration
```kotlin
// Legacy → New Status Mapping
val statusMigration = mapOf(
    "OPEN" to ItemStatus.OPEN,           // Pendente
    "GRANTED" to ItemStatus.COMPLETED,   // Pronto/Finalizado  
    "CANCELED" to ItemStatus.CANCELED    // Cancelado
)

// New statuses for enhanced workflow
// IN_PRODUCTION - Para items em preparo
// DELIVERED - Para items entregues
```

### Kitchen Integration
- **Kitchen Screen**: Utiliza todos os novos status para controle granular
- **Unit Tracking**: Cada unidade de item tem status independente
- **Real-time Updates**: SSE envia atualizações em tempo real dos status
- **Filter System**: Filtra pedidos por status (Ativos vs Entregues)

---

## 📋 Status Business Rules

### Table Status Rules
1. Mesa só pode ser aberta se estiver `FREE`
2. Mesa só pode ser fechada se estiver `OCCUPIED`  
3. Mesa em `ON_PAYMENT` não aceita novos pedidos
4. Transição de `ON_PAYMENT` para `FREE` finaliza o ciclo

### Item Status Rules
1. Item começa sempre como `OPEN`
2. `CANCELED` é terminal - não pode ser revertido
3. `DELIVERED` é terminal - representa conclusão do ciclo
4. Unidades individuais podem ter status diferentes
5. Status geral do item é calculado com base nas unidades

### Order Status Rules
1. Pedido começa como `OPEN`
2. Status do pedido é derivado dos status dos items
3. Pedido `GRANTED` quando todos os items estão processados
4. Pedido `CANCELED` quando cancelado explicitamente

### Bill Status Rules
1. Bill começa como `OPEN`
2. Bill `PAID` finaliza e libera a mesa
3. Bill `CANCELED` também libera a mesa
4. Bill `SCAM` requer intervenção manual

---

*Documentação criada em 25/08/2025 - Comanda AI v2.0*
*Status definitions para todos os entities do sistema*