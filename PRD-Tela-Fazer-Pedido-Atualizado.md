# PRD - Tela de Fazer Pedido (ATUALIZADO)
## Product Requirements Document

### 📋 Visão Geral
Desenvolver uma tela intuitiva para adicionar itens a um pedido de mesa, permitindo seleção rápida de produtos com controle de quantidade e navegação por categorias.

**IMPORTANTE**: Este PRD foi atualizado para refletir a arquitetura e APIs reais do projeto Comanda-ai.

---

## 🎯 Objetivos
- **Principal**: Permitir que garçons adicionem itens rapidamente a um pedido
- **Secundários**: 
  - Minimizar toques para adicionar produtos
  - Facilitar navegação entre categorias
  - Prevenir erros de quantidade

---

## 📱 Especificações da Tela

### Header
- **Título**: "Mesa {número}" com botão voltar (<)
- **Subtítulo**: "Fazer pedido"
- **Badge "aberta"**: Indicador visual do status da comanda (canto superior direito)

### Navegação por Categorias
- **Tabs horizontais** baseadas no enum `ItemCategory`:
  - "SKEWER" (Espetinhos)
  - "DRINK" (Bebidas)
  - "CHOPP" (Chopp)

### Lista de Produtos
- **Layout**: Lista vertical com cards para cada item
- **Cada card contém**:
  - Nome do produto (item.name)
  - Preço (item.value - formato R$ XX,XX)
  - Descrição (item.description) - se disponível
  - Controle de quantidade:
    - Botão decrementar (-)
    - Display count (default: 0)
    - Botão incrementar (+)

### Footer
- **Botão "Fazer pedido"**:
  - Cor verde (usar ComandaAiTheme)
  - Largura total
  - Habilitado apenas quando há pelo menos 1 item selecionado
  - Mostrar total de itens: "Fazer pedido (X itens)"

---

## 🔧 Requisitos Técnicos

### Componentes a Desenvolver

#### 1. OrderScreen
```kotlin
// Path: app/src/commonMain/kotlin/co/touchlab/dogify/presentation/screens/order/OrderScreen.kt
class OrderScreen(
    private val tableId: Int,
    private val tableNumber: String,
    private val billId: Int
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel { OrderScreenModel() }
        OrderScreenContent(
            tableNumber = tableNumber,
            screenModel = screenModel,
            onBackClick = { /* navigator.pop() */ },
            onSubmitOrder = { screenModel.submitOrder(tableId, billId) }
        )
    }
}
```

#### 2. OrderScreenModel
```kotlin
// Path: app/src/commonMain/kotlin/co/touchlab/dogify/presentation/screens/order/OrderScreenModel.kt
class OrderScreenModel(
    private val itemRepository: ItemRepository,
    private val orderRepository: OrderRepository
) : ScreenModel {
    
    private val _allItems = MutableStateFlow<List<Item>>(emptyList())
    private val _selectedCategory = MutableStateFlow(ItemCategory.DRINK)
    private val _selectedItems = MutableStateFlow<Map<Int, Int>>(emptyMap()) // itemId -> count
    private val _isLoading = MutableStateFlow(false)
    
    val categories = ItemCategory.values().toList()
    val selectedCategory = _selectedCategory.asStateFlow()
    val isLoading = _isLoading.asStateFlow()
    
    val filteredItems = combine(_allItems, _selectedCategory) { items, category ->
        items.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    val itemsWithCount = combine(filteredItems, _selectedItems) { items, selected ->
        items.map { item ->
            ItemWithCount(item, selected[item.id] ?: 0)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    val canSubmit = _selectedItems.map { it.values.sum() > 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    
    val totalItems = _selectedItems.map { it.values.sum() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    
    init {
        loadItems()
    }
    
    fun selectCategory(category: ItemCategory) {
        _selectedCategory.value = category
    }
    
    fun incrementItem(itemId: Int) {
        val current = _selectedItems.value.toMutableMap()
        current[itemId] = (current[itemId] ?: 0) + 1
        _selectedItems.value = current
    }
    
    fun decrementItem(itemId: Int) {
        val current = _selectedItems.value.toMutableMap()
        val newCount = (current[itemId] ?: 0) - 1
        if (newCount <= 0) {
            current.remove(itemId)
        } else {
            current[itemId] = newCount
        }
        _selectedItems.value = current
    }
    
    fun submitOrder(tableId: Int, billId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val orderItems = _selectedItems.value.map { (itemId, count) ->
                    val item = _allItems.value.first { it.id == itemId }
                    CreateOrderItemRequest(
                        itemId = itemId,
                        count = count,
                        observation = null
                    )
                }
                
                val request = CreateOrderRequest(
                    tableId = tableId,
                    billId = billId,
                    items = orderItems
                )
                
                orderRepository.createOrder(request)
                // Navegar de volta ou mostrar sucesso
            } catch (e: Exception) {
                // Tratar erro
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun loadItems() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _allItems.value = itemRepository.getAllItems()
            } catch (e: Exception) {
                // Tratar erro
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

#### 3. Modelos de Dados
```kotlin
// Usar modelos existentes + helper
data class ItemWithCount(
    val item: Item, // Modelo existente
    val count: Int = 0
)

// Para request da API
data class CreateOrderItemRequest(
    val itemId: Int,
    val count: Int,
    val observation: String? = null
)
```

#### 4. Componentes UI
```kotlin
// Path: app/src/commonMain/kotlin/co/touchlab/dogify/presentation/screens/order/components/

@Composable
fun CategoryTabs(
    categories: List<ItemCategory>,
    selectedCategory: ItemCategory,
    onCategorySelected: (ItemCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { category ->
            CategoryTab(
                category = category,
                isSelected = category == selectedCategory,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun CategoryTab(
    category: ItemCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val displayName = when (category) {
        ItemCategory.SKEWER -> "Espetinhos"
        ItemCategory.DRINK -> "Bebidas"
        ItemCategory.NON_ALCOHOLIC_DRINKS -> "Sem Álcool"
        ItemCategory.CHOPP -> "Chopp"
    }
    
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(displayName) }
    )
}

@Composable
fun OrderItemCard(
    itemWithCount: ItemWithCount,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = itemWithCount.item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "R$ ${itemWithCount.item.value / 100f}".replace(".", ","),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                itemWithCount.item.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            QuantitySelector(
                count = itemWithCount.count,
                onIncrement = onIncrement,
                onDecrement = onDecrement
            )
        }
    }
}

@Composable
fun QuantitySelector(
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onDecrement,
            enabled = count > 0
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Diminuir")
        }
        
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.widthIn(min = 24.dp),
            textAlign = TextAlign.Center
        )
        
        IconButton(onClick = onIncrement) {
            Icon(Icons.Default.Add, contentDescription = "Aumentar")
        }
    }
}
```

### Integração com API

#### Endpoints Utilizados
1. **GET /api/v1/items** - Buscar todos os itens (filtro por categoria no frontend)
2. **POST /api/v1/orders** - Criar pedido

#### Request Body (POST /api/v1/orders)
```json
{
  "tableId": 1,
  "billId": 1,
  "items": [
    {
      "itemId": 1,
      "count": 2,
      "observation": null
    },
    {
      "itemId": 3,
      "count": 1,
      "observation": "Sem cebola"
    }
  ]
}
```

---

## 🎨 Especificações de Design

### Tema
- **Usar ComandaAiTheme**: Tema já configurado no projeto
- **Cores**: Material Design 3 com cores personalizadas do tema
- **Tipografia**: ComandaAiTypography já definida

### Layout
- **Padding da tela**: 16.dp
- **Espaçamento entre cards**: 8.dp
- **Altura mínima do card**: 80.dp
- **Padding interno do card**: 16.dp

### Estados Visuais
- **Categoria selecionada**: FilterChip selected
- **Botão desabilitado**: Quando count = 0 no decrementar
- **Loading**: CircularProgressIndicator durante requisições

---

## 📊 Fluxo de Dados

### Estado Inicial
1. Receber `tableId`, `tableNumber` e `billId` como parâmetros
2. Carregar todos os itens via `GET /api/v1/items`
3. Selecionar categoria "DRINK" por padrão
4. Filtrar itens da categoria selecionada
5. Inicializar todos os counts em 0

### Interações
1. **Trocar categoria**: 
   - Atualizar `selectedCategory`
   - Manter counts já selecionados (estado global)
   - Recompor lista filtrada

2. **Incrementar/Decrementar**:
   - Atualizar Map<itemId, count>
   - Recompor estado `canSubmit`
   - Atualizar texto do botão com total

3. **Submeter pedido**:
   - Validar se há itens selecionados
   - Converter para `CreateOrderRequest`
   - Fazer POST para `/api/v1/orders`
   - Em sucesso: voltar para tela anterior
   - Em erro: mostrar mensagem de erro

---

## 🔍 Validações

### Frontend
- Count mínimo: 0
- Count máximo: 99 por item
- Pelo menos 1 item para submeter
- Debounce nos botões de quantidade (300ms)

### Backend
- Verificar se mesa e bill existem
- Validar se itens existem no banco
- Verificar se mesa está aberta

---

## 🚀 Cronograma Atualizado

### Sprint 1 - Foundation (2-3 dias)
**Dia 1:**
- [ ] Criar estrutura de pastas `presentation/screens/order/`
- [ ] Implementar `OrderScreen` básica com navegação
- [ ] Configurar `OrderScreenModel` com estados básicos
- [ ] Mockear dados para desenvolvimento inicial

**Dia 2:**
- [ ] Implementar `CategoryTabs` component
- [ ] Criar layout básico da tela com header e tabs
- [ ] Implementar filtro de categorias (local)
- [ ] Adicionar `DogifyTopAppBar` do design system

**Dia 3:**
- [ ] Implementar `OrderItemCard` component
- [ ] Criar `QuantitySelector` component
- [ ] Layout da lista de itens
- [ ] Testar navegação entre categorias

### Sprint 2 - Funcionalidades Core (2-3 dias)
**Dia 4:**
- [ ] Integrar com API real `GET /api/v1/items`
- [ ] Implementar repository pattern para items
- [ ] Tratar estados de loading e erro
- [ ] Configurar DI para OrderScreenModel

**Dia 5:**
- [ ] Implementar lógica de increment/decrement
- [ ] Adicionar validações de count (0-99)
- [ ] Implementar estado `canSubmit`
- [ ] Criar footer com botão de submissão

**Dia 6:**
- [ ] Implementar `submitOrder` com API POST
- [ ] Tratar resposta de sucesso/erro
- [ ] Adicionar loading states
- [ ] Implementar navegação de retorno

### Sprint 3 - Polish & Testing (1-2 dias)
**Dia 7:**
- [ ] Adicionar animações (ripple effects)
- [ ] Implementar feedback visual
- [ ] Melhorar tratamento de erros
- [ ] Adicionar content descriptions (acessibilidade)

**Dia 8:**
- [ ] Testes unitários do ScreenModel
- [ ] Testes de UI (Compose Testing)
- [ ] Code review e refatoração
- [ ] Documentação dos componentes

---

## ⚠️ Considerações Técnicas

### Dependências Necessárias
- **Voyager**: Já configurado (navigation)
- **Compose**: Já configurado (UI)
- **Koin/Kodein**: Para DI (verificar qual está sendo usado)
- **Ktor Client**: Para API calls (já configurado)

### Navegação
```kotlin
// Na TableDetailsScreen, navegar para OrderScreen
navigator.push(
    OrderScreen(
        tableId = table.id,
        tableNumber = table.number.toString(),
        billId = currentBill.id // Buscar bill ativa da mesa
    )
)
```

### Performance
- Usar `LazyColumn` para lista de itens
- `StateFlow` com `stateIn` para otimizar recomposições
- Debounce nos botões de quantidade
- Cache de itens carregados

### Tratamento de Erros
- Conexão perdida durante carregamento
- Erro 404/500 na API
- Mesa fechada durante criação do pedido
- Validação de billId inválido

### Edge Cases
- Lista de itens vazia
- Categoria sem itens
- Mesa sem bill ativa
- Navegação para trás com itens selecionados (confirmar descarte)
- Limit de count por item (99)

---

## 📈 Definição de Pronto (DoD)

### Funcional
- [ ] Carrega itens da API corretamente
- [ ] Filtra por categoria adequadamente
- [ ] Incrementa/decrementa counts corretamente
- [ ] Submete pedido com sucesso
- [ ] Trata erros adequadamente
- [ ] Navega corretamente

### Qualidade
- [ ] Código segue padrões do projeto
- [ ] Testes unitários >80% cobertura
- [ ] Sem warnings de lint
- [ ] Performance adequada (< 16ms por frame)
- [ ] Acessibilidade implementada

### UX
- [ ] Interface intuitiva e responsiva
- [ ] Feedback visual adequado
- [ ] Estados de loading claros
- [ ] Mensagens de erro informativas

---

## 🔮 Próximos Passos (Futuras Melhorias)

1. **Observações por item**: Campo de texto para observações
2. **Busca de itens**: SearchBar para encontrar produtos
3. **Favoritos**: Marcar itens mais pedidos
4. **Histórico**: Sugestões baseadas em pedidos anteriores
5. **Offline mode**: Cache local para itens
6. **Modificadores**: Opções como "sem cebola", "mal passado", etc.
7. **Imagens**: Fotos dos produtos
8. **Promocões**: Destacar itens em promoção