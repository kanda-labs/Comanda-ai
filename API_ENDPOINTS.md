# API Endpoints Documentation - Comanda AI

Este documento documenta os endpoints da API REST do CommanderAPI utilizados pela aplicação móvel.

## Base URL
```
http://10.0.2.2:8081/api/v1
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

---

*Documentação gerada em 15/08/2025 - Comanda AI v1.0*