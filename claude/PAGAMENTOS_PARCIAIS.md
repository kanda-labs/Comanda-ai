# Funcionalidade de Pagamentos Parciais

## Descrição

Sistema que permite que múltiplas pessoas em uma mesa possam fazer pagamentos parciais antes de sair, facilitando a divisão da conta em restaurantes e estabelecimentos similares.

## Caso de Uso

**Cenário**: Mesa com 4 pessoas, onde uma pessoa precisa sair mais cedo e quer pagar apenas sua parte da conta, sem esperar que todos terminem.

**Solução**: Sistema de "pagamentos avulsos" que permite registrar pagamentos parciais independentes, mantendo histórico e calculando saldo restante automaticamente.

## Arquitetura Implementada

### 1. Model - PartialPayment
```kotlin
@Serializable
data class PartialPayment(
    val id: Int? = null,
    val billId: Int,
    val tableId: Int,
    val paidBy: String,              // Nome da pessoa que pagou
    val amountInCentavos: Long,
    val amountFormatted: String,     // Formatado como "R$ X,XX"
    val description: String? = null, // Ex: "João pagou sua parte"
    val paymentMethod: String? = null, // PIX, Cartão, Dinheiro, etc.
    val createdAt: LocalDateTime
)
```

### 2. Database Schema
Tabela `partial_payments` criada via migração:
```sql
CREATE TABLE partial_payments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    bill_id INTEGER NOT NULL REFERENCES bills(id),
    table_id INTEGER NOT NULL REFERENCES tables(id),
    paid_by VARCHAR(255) NOT NULL,
    amount_in_centavos BIGINT NOT NULL,
    description VARCHAR(500),
    payment_method VARCHAR(50),
    created_at BIGINT NOT NULL
);
```

### 3. API Endpoints

#### Criar Pagamento Parcial
```http
POST /api/v1/bills/table/{tableId}/partial-payment
Content-Type: application/json

{
    "paidBy": "João",
    "amountInCentavos": 5000,
    "description": "Pagamento parcial - João saiu mais cedo",
    "paymentMethod": "PIX"
}
```

**Response:**
```json
{
    "id": 1,
    "billId": 73,
    "tableId": 2,
    "paidBy": "João",
    "amountInCentavos": 5000,
    "amountFormatted": "R$ 50,00",
    "description": "Pagamento parcial - João saiu mais cedo",
    "paymentMethod": "PIX",
    "createdAt": "2025-08-27T12:15:56.516"
}
```

#### Resumo de Pagamentos
```http
GET /api/v1/bills/table/{tableId}/payment-summary
```

**Response:**
```json
{
    "tableNumber": "02",
    "totalAmountInCentavos": 1900,
    "totalAmountFormatted": "R$ 19,00",
    "totalPaidInCentavos": 1400,
    "totalPaidFormatted": "R$ 14,00",
    "remainingAmountInCentavos": 500,
    "remainingAmountFormatted": "R$ 5,00",
    "orders": [
        {
            "id": "Pedido Nº 217",
            "items": [
                {
                    "name": "Água",
                    "quantity": 3,
                    "priceInCentavos": 300,
                    "priceFormatted": "R$ 3,00",
                    "totalInCentavos": 900,
                    "totalFormatted": "R$ 9,00",
                    "observation": null
                },
                {
                    "name": "Refrigerante",
                    "quantity": 2,
                    "priceInCentavos": 500,
                    "priceFormatted": "R$ 5,00",
                    "totalInCentavos": 1000,
                    "totalFormatted": "R$ 10,00",
                    "observation": null
                }
            ],
            "orderTotalInCentavos": 1900,
            "orderTotalFormatted": "R$ 19,00",
            "status": {
                "text": "Pendente",
                "colorHex": "#2196F3"
            }
        }
    ],
    "partialPayments": [
        {
            "id": 6,
            "billId": 74,
            "tableId": 3,
            "paidBy": "Maria",
            "amountInCentavos": 900,
            "amountFormatted": "R$ 9,00",
            "description": "Maria pagou as 3 águas",
            "paymentMethod": "Cartão",
            "createdAt": "2025-08-27T15:45:53.851"
        },
        {
            "id": 5,
            "billId": 74,
            "tableId": 3,
            "paidBy": "Pedro",
            "amountInCentavos": 500,
            "amountFormatted": "R$ 5,00",
            "description": "Pedro pagou 1 refrigerante",
            "paymentMethod": "PIX",
            "createdAt": "2025-08-27T15:45:15.170"
        }
    ]
}
```

#### Listar Pagamentos Parciais
```http
GET /api/v1/bills/table/{tableId}/partial-payments
```

### 4. Gerenciamento de Status da Conta

Sistema automaticamente atualiza status da conta:
- **OPEN**: Conta aberta, sem pagamentos
- **PARTIALLY_PAID**: Há pagamentos parciais, mas ainda resta saldo
- **PAID**: Total pago >= total da conta

```kotlin
val newBillStatus = when {
    totalPaidAmount >= totalBillAmount -> "PAID"
    totalPaidAmount > 0 -> "PARTIALLY_PAID"
    else -> "OPEN"
}
```

Quando conta é marcada como PAID:
- Todos os pedidos são marcados como DELIVERED
- Mesa é liberada (status CLOSED, billId = null)

## Implementação de Frontend

### PaymentSummaryScreen.kt
Tela redesenhada para mostrar:
- Resumo financeiro da mesa
- Lista de pedidos com detalhes dos itens
- Histórico de pagamentos parciais
- Botão para adicionar novo pagamento
- Cálculo automático do saldo restante

### PartialPaymentDialog
Modal para criar novo pagamento parcial:
- Campo para nome de quem está pagando
- Campo para valor em reais
- Campo para descrição (opcional)
- Seleção do método de pagamento

## Exemplo de Fluxo Completo

### Cenário Testado:
1. **Mesa 3**: Pedido de R$ 19,00 (3 águas + 2 refrigerantes)
2. **Pedro paga**: R$ 5,00 via PIX (1 refrigerante)
   - Status: PARTIALLY_PAID
   - Restante: R$ 14,00
3. **Maria paga**: R$ 9,00 via Cartão (3 águas)
   - Status: PARTIALLY_PAID
   - Restante: R$ 5,00
4. **Último pagamento**: R$ 5,00 (1 refrigerante restante)
   - Status: PAID
   - Mesa liberada

## Correções Técnicas Implementadas

### Problema: Endpoints 404 para contas PARTIALLY_PAID
**Causa**: Métodos `getBillByTableId()` e `getBillPaymentSummary()` buscavam apenas contas OPEN.

**Solução**: Atualizado filtro para incluir contas PARTIALLY_PAID:
```kotlin
.where { 
    (billTable.tableId eq tableId) and 
    (billTable.status inList listOf(BillStatus.OPEN.name, BillStatus.PARTIALLY_PAID.name))
}
```

### Problema: Erro de coluna amount_formatted
**Causa**: Tentativa de inserir campo calculado no banco.

**Solução**: Campo `amountFormatted` calculado dinamicamente no método `toPartialPayment()`.

## Arquivos Modificados

### Backend (CommanderAPI):
- `domain/model/PartialPayment.kt` - Model criado
- `data/model/sqlModels/SQLTableObjects.kt` - Tabela PartialPaymentTable
- `data/repository/BillRepositoryImpl.kt` - Implementação dos métodos
- `presentation/routes/BillRoutes.kt` - Endpoints REST
- `presentation/models/request/CreatePartialPaymentRequest.kt` - Request model
- `application/config/DatabaseMigrations.kt` - Migração da tabela

### Frontend (Comanda-ai-kmp):
- `presentation/screens/payment/PaymentSummaryScreen.kt` - Tela principal
- `presentation/screens/payment/PaymentSummaryAction.kt` - Actions
- `presentation/screens/payment/PaymentSummaryScreenState.kt` - State
- `presentation/screens/payment/PaymentSummaryViewModel.kt` - ViewModel
- `domain/models/model/PartialPayment.kt` - Model no frontend
- `domain/models/request/CreatePartialPaymentRequest.kt` - Request model

## Status da Implementação

✅ **Funcional:**
- Criação de pagamentos parciais
- Consulta de resumo de pagamentos  
- Listagem de histórico
- Cálculos de saldo
- Transição de status da conta
- Interface de usuário completa

⚠️ **Bug Identificado:**
Algoritmo de cálculo do total da conta (`getBillTotalAmount()`) tem erro que faz contas serem marcadas como PAID prematuramente quando ainda há saldo restante.

## Data da Implementação
27 de Agosto de 2025

## Desenvolvido por
Claude Code Assistant