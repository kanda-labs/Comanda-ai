# Plano de Execução para LLM - Tela de Fazer Pedido
## Instruções para Implementação Assistida por IA

### 📌 Contexto do Projeto
**Projeto**: Comanda-ai - Sistema de gestão de comandas de restaurante  
**Módulo**: Tela de criação de pedidos para mesa  
**Stack**: Kotlin Multiplatform, Compose Multiplatform, Voyager, Kodein DI  
**Localização**: `Comanda-ai-kmp/app/src/commonMain/kotlin/`

---

## 🎯 Objetivo da Implementação
Criar uma tela que permita adicionar itens a um pedido de mesa, com navegação por categorias e controle de quantidade para cada item.

---

## 📋 TAREFAS DE IMPLEMENTAÇÃO

### TAREFA 1: Criar Estrutura Base da Tela
**Arquivo**: `co/touchlab/dogify/presentation/screens/order/OrderScreen.kt`

**Instruções específicas**:
1. Criar o pacote `order` dentro de `presentation/screens/`
2. Implementar a classe `OrderScreen` que estende `Screen` do Voyager
3. Adicionar os parâmetros: `tableId: Int`, `tableNumber: String`, `billId: Int`
4. No método `Content()`, instanciar o `OrderScreenModel` usando `getScreenModel`
5. Chamar o composable `OrderScreenContent` passando os parâmetros necessários

**Código base para começar**:
```kotlin
package co.touchlab.dogify.presentation.screens.order

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class OrderScreen(
    private val tableId: Int,
    private val tableNumber: String,
    private val billId: Int
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<OrderScreenModel>()
        
        OrderScreenContent(
            tableNumber = tableNumber,
            screenModel = screenModel,
            onBackClick = { navigator.pop() },
            onSubmitOrder = { 
                screenModel.submitOrder(tableId, billId)
            }
        )
    }
}
```

---

### TAREFA 2: Implementar o ScreenModel
**Arquivo**: `co/touchlab/dogify/presentation/screens/order/OrderScreenModel.kt`

**Instruções específicas**:
1. Criar classe `OrderScreenModel` que estende `ScreenModel` do Voyager
2. Injetar via construtor: `ItemRepository` e `OrderRepository`
3. Criar os StateFlows necessários:
   - `_allItems`: Lista de todos os itens carregados
   - `_selectedCategory`: Categoria atualmente selecionada
   - `_selectedItems`: Map de itemId para quantidade
   - `_isLoading`: Estado de carregamento
4. Implementar os métodos:
   - `loadItems()`: Buscar itens da API
   - `selectCategory(category: ItemCategory)`: Mudar categoria
   - `incrementItem(itemId: Int)`: Aumentar quantidade
   - `decrementItem(itemId: Int)`: Diminuir quantidade
   - `submitOrder(tableId: Int, billId: Int)`: Enviar pedido

**Pontos de atenção**:
- Usar `combine` para criar flows derivados (`filteredItems`, `itemsWithCount`)
- Implementar `canSubmit` baseado na soma das quantidades
- No `init`, chamar `loadItems()`
- Usar `viewModelScope` para coroutines

---

### TAREFA 3: Criar Modelo de Dados Auxiliar
**Arquivo**: `co/touchlab/dogify/domain/model/ItemWithCount.kt`

**Instruções específicas**:
1. Criar data class `ItemWithCount` com:
   - `item: Item` (usar modelo existente)
   - `count: Int` com valor padrão 0
2. Esta classe será usada para associar quantidade a cada item na UI

**Código exato**:
```kotlin
package co.touchlab.dogify.domain.model

data class ItemWithCount(
    val item: Item,
    val count: Int = 0
)
```

---

### TAREFA 4: Implementar Composable Principal
**Arquivo**: `co/touchlab/dogify/presentation/screens/order/OrderScreenContent.kt`

**Instruções específicas**:
1. Criar função `@Composable OrderScreenContent` com parâmetros:
   - `tableNumber: String`
   - `screenModel: OrderScreenModel`
   - `onBackClick: () -> Unit`
   - `onSubmitOrder: () -> Unit`
2. Estrutura da tela:
   - **Top**: `DogifyTopAppBar` com título "Mesa $tableNumber" e navegação
   - **Abaixo do header**: `CategoryTabs` para seleção de categoria
   - **Centro**: `LazyColumn` com lista de `OrderItemCard`
   - **Bottom**: Botão "Fazer pedido (X itens)" fixo
3. Coletar estados usando `collectAsState()`:
   - `categories`, `selectedCategory`, `itemsWithCount`, `canSubmit`, `totalItems`, `isLoading`
4. Mostrar `CircularProgressIndicator` quando `isLoading = true`

**Estrutura do Scaffold**:
```kotlin
Scaffold(
    topBar = { /* DogifyTopAppBar */ },
    bottomBar = { /* Botão Fazer Pedido */ }
) { paddingValues ->
    Column(modifier = Modifier.padding(paddingValues)) {
        CategoryTabs(...)
        LazyColumn { ... }
    }
}
```

---

### TAREFA 5: Implementar Component CategoryTabs
**Arquivo**: `co/touchlab/dogify/presentation/screens/order/components/CategoryTabs.kt`

**Instruções específicas**:
1. Criar `@Composable CategoryTabs` com parâmetros:
   - `categories: List<ItemCategory>`
   - `selectedCategory: ItemCategory`
   - `onCategorySelected: (ItemCategory) -> Unit`
2. Usar `LazyRow` com `FilterChip` do Material3
3. Mapear enums para nomes em português:
   - SKEWER → "Espetinhos"
   - DRINK → "Bebidas"
   - NON_ALCOHOLIC_DRINKS → "Sem Álcool"
   - CHOPP → "Chopp"
4. Aplicar padding horizontal de 16.dp
5. Espaçamento entre chips de 8.dp

---

### TAREFA 6: Implementar Component OrderItemCard
**Arquivo**: `co/touchlab/dogify/presentation/screens/order/components/OrderItemCard.kt`

**Instruções específicas**:
1. Criar `@Composable OrderItemCard` com parâmetros:
   - `itemWithCount: ItemWithCount`
   - `onIncrement: () -> Unit`
   - `onDecrement: () -> Unit`
2. Usar `Card` do Material3 com elevation de 2.dp
3. Layout horizontal com:
   - **Esquerda**: Nome, preço (formato R$ XX,XX) e descrição
   - **Direita**: `QuantitySelector`
4. Converter preço: `item.value / 100f` e formatar com vírgula
5. Padding interno de 16.dp

---

### TAREFA 7: Implementar Component QuantitySelector
**Arquivo**: `co/touchlab/dogify/presentation/screens/order/components/QuantitySelector.kt`

**Instruções específicas**:
1. Criar `@Composable QuantitySelector` com:
   - `count: Int`
   - `onIncrement: () -> Unit`
   - `onDecrement: () -> Unit`
2. Layout horizontal com:
   - `IconButton` com `Icons.Default.Remove` (desabilitado se count = 0)
   - `Text` mostrando count (largura mínima 24.dp, centralizado)
   - `IconButton` com `Icons.Default.Add`
3. Espaçamento entre elementos de 8.dp
4. Adicionar `contentDescription` para acessibilidade

---

### TAREFA 8: Criar Request DTO
**Arquivo**: `co/touchlab/dogify/data/api/dto/CreateOrderRequest.kt`

**Instruções específicas**:
1. Criar data classes com `@Serializable`:
```kotlin
@Serializable
data class CreateOrderRequest(
    val tableId: Int,
    val billId: Int,
    val items: List<CreateOrderItemRequest>
)

@Serializable
data class CreateOrderItemRequest(
    val itemId: Int,
    val count: Int,
    val observation: String? = null
)
```
2. Verificar se já existem DTOs similares no projeto e reutilizar se possível

---

### TAREFA 9: Configurar Injeção de Dependências
**Arquivo**: Localizar módulo DI existente (provavelmente em `di/AppModule.kt`)

**Instruções específicas**:
1. Adicionar factory para `OrderScreenModel`:
```kotlin
bindSingleton<OrderScreenModel> {
    OrderScreenModel(
        itemRepository = instance(),
        orderRepository = instance()
    )
}
```
2. Verificar se `ItemRepository` e `OrderRepository` já estão configurados
3. Se não existirem, criar as implementações necessárias

---

### TAREFA 10: Integrar Navegação
**Arquivo**: `co/touchlab/dogify/presentation/screens/tables/TableDetailsScreen.kt`

**Instruções específicas**:
1. Localizar o botão "Fazer pedido" na `TableDetailsScreen`
2. Adicionar navegação para `OrderScreen`:
```kotlin
Button(
    onClick = {
        navigator.push(
            OrderScreen(
                tableId = table.id,
                tableNumber = table.number.toString(),
                billId = currentBill.id // Obter bill ativa
            )
        )
    }
) {
    Text("Fazer pedido")
}
```
3. Verificar como obter o `billId` da bill ativa da mesa

---

### TAREFA 11: Implementar Método submitOrder
**Arquivo**: `co/touchlab/dogify/presentation/screens/order/OrderScreenModel.kt`

**Instruções específicas**:
1. No método `submitOrder`:
   - Converter `_selectedItems` para `CreateOrderRequest`
   - Fazer chamada POST para `/api/v1/orders`
   - Em sucesso: Limpar seleções e retornar true
   - Em erro: Logar erro e retornar false
2. Na `OrderScreen`, após sucesso do submit:
   - Mostrar feedback (Toast ou SnackBar)
   - Navegar de volta com `navigator.pop()`
3. Implementar tratamento de erros com try-catch
4. Usar `_isLoading` para mostrar progress durante request

---

### TAREFA 12: Adicionar Testes Unitários
**Arquivo**: `app/src/commonTest/kotlin/.../OrderScreenModelTest.kt`

**Instruções específicas**:
1. Testar `incrementItem` e `decrementItem`
2. Verificar que `canSubmit` fica true quando há itens
3. Testar filtro por categoria
4. Mockar repositories usando MockK
5. Verificar chamada correta da API em `submitOrder`

---

## 🔍 CHECKLIST DE VALIDAÇÃO

Após implementar cada tarefa, validar:

### ✅ Funcionalidades
- [ ] Itens carregam da API corretamente
- [ ] Categorias filtram os itens adequadamente
- [ ] Incrementar/decrementar altera quantidade corretamente
- [ ] Botão só habilita quando há itens selecionados
- [ ] Pedido é enviado com sucesso para API
- [ ] Navegação de volta funciona após submit

### ✅ UI/UX
- [ ] Layout segue o design do protótipo
- [ ] Estados de loading são visíveis
- [ ] Erros são tratados e exibidos ao usuário
- [ ] Responsividade em diferentes tamanhos de tela
- [ ] Animações e feedback visual funcionam

### ✅ Código
- [ ] Segue arquitetura MVVM do projeto
- [ ] Usa componentes do design system existente
- [ ] DI configurado corretamente
- [ ] Sem warnings de compilação
- [ ] Imports organizados e sem unused

---

## 🚨 PONTOS DE ATENÇÃO

1. **API Endpoints**: Verificar URLs exatas no `CommanderAPI`
2. **Modelos**: Reutilizar `Item`, `Order`, `Bill` existentes
3. **Design System**: Usar `ComandaAiTheme` e componentes existentes
4. **Navigation**: Integrar com fluxo existente do Voyager
5. **Error Handling**: Seguir padrão do projeto para tratamento de erros
6. **Estado da Mesa**: Validar se mesa está aberta antes de permitir pedido

---

## 📝 COMANDO PARA COMEÇAR

Para uma LLM executar este plano, use o seguinte prompt:

```
"Vou implementar a tela de fazer pedido do projeto Comanda-ai seguindo o plano de execução. 
Começarei pela TAREFA 1 criando a OrderScreen. 
O projeto usa Kotlin Multiplatform com Compose e Voyager para navegação.
Por favor, gere o código completo para o arquivo OrderScreen.kt seguindo as especificações da TAREFA 1."
```

Após cada tarefa concluída, prossiga com:
```
"TAREFA X concluída. Agora vou implementar a TAREFA Y. 
Por favor, gere o código para [nome do arquivo] seguindo as especificações."
```