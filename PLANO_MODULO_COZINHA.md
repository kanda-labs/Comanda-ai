# 🎯 Plano de Implementação - Módulo Cozinha Comanda-AI

## 📊 Visão Geral
Implementação do módulo de gestão de cozinha no sistema Comanda-AI, permitindo controle individual de itens dos pedidos com base na arquitetura existente.

## 🏗️ Análise da Arquitetura Existente

### Backend (CommanderAPI)
- ✅ **ItemStatus existente**: `GRANTED`, `OPEN`, `CANCELED`
- ✅ **UserRole.KITCHEN**: Já implementado
- ✅ **OrderItemTable**: Tabela com campos `status` e `count`
- ✅ **Clean Architecture**: Padrão estabelecido

### Frontend (Comanda-ai-kmp)
- ✅ **Módulos**: `app`, `auth`, `core`, `designsystem`
- ✅ **MVVM + Voyager**: Padrão estabelecido
- ✅ **Design System**: Componentes reutilizáveis

## 🏗️ Fase 1: Backend (CommanderAPI) - 3 dias

### 1.1 Expandir ItemStatus Existente

#### 📁 `domain/model/Item.kt` - Atualização
```kotlin
enum class ItemStatus { 
    GRANTED,        // Concluído (mantém compatibilidade)
    OPEN,          // Pendente (mantém compatibilidade)
    CANCELED,      // Cancelado (mantém compatibilidade)
    IN_PRODUCTION, // Em produção (novo)
    COMPLETED,     // Finalizado (novo)
    DELIVERED      // Entregue (novo)
}
```

### 1.2 Criar Tabela para Rastreamento Individual

#### 📁 `data/model/sqlModels/SQLTableObjects.kt` - Nova tabela
```kotlin
object OrderItemStatusTable : Table("order_item_statuses") {
    val id = integer("id").autoIncrement()
    val orderItemId = integer("order_item_id")
    val itemId = integer("item_id").references(ItemTable.id)
    val orderId = integer("order_id").references(OrderTable.id)
    val unitIndex = integer("unit_index")
    val status = varchar("status", 32)
    val updatedAt = long("updated_at")
    val updatedBy = varchar("updated_by", 255).nullable()
    
    override val primaryKey = PrimaryKey(id)
}
```

### 1.3 Implementar KitchenService

#### 📁 `domain/service/KitchenService.kt`
```kotlin
interface KitchenService {
    suspend fun getActiveOrdersForKitchen(): Result<List<KitchenOrder>>
    suspend fun updateItemUnitStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        status: ItemStatus,
        updatedBy: String
    ): Result<Boolean>
    suspend fun getItemStatusBreakdown(orderItemId: Int): Result<List<ItemStatus>>
}
```

#### 📁 `domain/service/KitchenServiceImpl.kt`
```kotlin
class KitchenServiceImpl(
    private val orderRepository: OrderRepository
) : KitchenService {
    
    override suspend fun getActiveOrdersForKitchen(): Result<List<KitchenOrder>> {
        return orderRepository.getOrdersWithIncompleteItems()
            .map { orders ->
                orders.filter { order ->
                    order.items.any { item ->
                        hasIncompleteUnits(item)
                    }
                }.map { it.toKitchenOrder() }
            }
    }
    
    override suspend fun updateItemUnitStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        status: ItemStatus,
        updatedBy: String
    ): Result<Boolean> {
        // Validar se unitIndex é válido (< count)
        // Atualizar status individual
        // Recalcular status geral do item se necessário
    }
    
    private fun hasIncompleteUnits(item: ItemOrder): Boolean {
        // Verificar se há unidades não finalizadas
    }
}
```

### 1.4 Expandir OrderRepository

#### 📁 `domain/repository/OrderRepository.kt` - Novos métodos
```kotlin
interface OrderRepository {
    // Métodos existentes...
    
    suspend fun getOrdersWithIncompleteItems(): Result<List<Order>>
    suspend fun updateItemUnitStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        status: ItemStatus,
        updatedBy: String
    ): Result<Boolean>
    suspend fun getItemUnitStatuses(
        orderId: Int,
        itemId: Int
    ): Result<List<ItemUnitStatus>>
}
```

### 1.5 Novos Endpoints Kitchen

#### 📁 `presentation/routes/KitchenRoutes.kt`
```kotlin
fun Route.kitchenRoutes() {
    route("/api/v1/kitchen") {
        authenticate {
            // Verificar role KITCHEN
            intercept(ApplicationCallPipeline.Call) {
                val userRole = call.principal<UserPrincipal>()?.role
                if (userRole != UserRole.KITCHEN && userRole != UserRole.ADMIN) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@intercept finish()
                }
            }
            
            get("/orders") {
                val kitchenService = get<KitchenService>()
                kitchenService.getActiveOrdersForKitchen()
                    .onSuccess { orders ->
                        call.respond(HttpStatusCode.OK, orders)
                    }
                    .onFailure { error ->
                        call.respond(HttpStatusCode.InternalServerError, error.message)
                    }
            }
            
            put("/orders/{orderId}/items/{itemId}/unit/{unitIndex}") {
                val orderId = call.parameters["orderId"]?.toInt() 
                    ?: throw BadRequestException("Invalid orderId")
                val itemId = call.parameters["itemId"]?.toInt() 
                    ?: throw BadRequestException("Invalid itemId")
                val unitIndex = call.parameters["unitIndex"]?.toInt() 
                    ?: throw BadRequestException("Invalid unitIndex")
                
                val request = call.receive<UpdateItemStatusRequest>()
                val userPrincipal = call.principal<UserPrincipal>()
                
                val kitchenService = get<KitchenService>()
                kitchenService.updateItemUnitStatus(
                    orderId, itemId, unitIndex, 
                    ItemStatus.valueOf(request.status),
                    userPrincipal?.userName ?: "unknown"
                ).onSuccess {
                    call.respond(HttpStatusCode.OK)
                }.onFailure { error ->
                    call.respond(HttpStatusCode.BadRequest, error.message)
                }
            }
        }
    }
}
```

#### 📁 `presentation/models/request/KitchenRequests.kt`
```kotlin
@Serializable
data class UpdateItemStatusRequest(
    val status: String
)

@Serializable
data class KitchenOrder(
    val id: Int,
    val tableNumber: Int,
    val items: List<KitchenItemDetail>,
    val createdAt: Long
)

@Serializable
data class KitchenItemDetail(
    val itemId: Int,
    val name: String,
    val totalCount: Int,
    val observation: String?,
    val unitStatuses: List<String>, // Status de cada unidade individual
    val overallStatus: String // Status geral calculado
)

@Serializable
data class ItemUnitStatus(
    val unitIndex: Int,
    val status: String,
    val updatedAt: Long,
    val updatedBy: String?
)
```

### 1.6 Configurar DI (Koin)

#### 📁 `application/KoinModules.kt` - Adicionar módulo
```kotlin
val kitchenModule = module {
    single<KitchenService> { KitchenServiceImpl(get()) }
}

// No setupKoin():
modules(
    // módulos existentes...
    kitchenModule
)
```

## 🏗️ Fase 2: Frontend (Comanda-ai-kmp) - 4 dias

### 2.1 Criar Módulo Kitchen

#### 📁 `settings.gradle.kts` - Adicionar módulo
```kotlin
include(":app")
include(":auth")
include(":designsystem")
include(":core")
include(":kitchen") // Novo módulo
```

#### 📁 `kitchen/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrainsCompose)
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            implementation(projects.designsystem)
            
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.transitions)
            
            implementation(libs.kodein.di)
            implementation(libs.ktor.client.core)
            
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.assertk)
            implementation(libs.turbine)
        }
    }
}

android {
    namespace = "co.kandalabs.comandaai.kitchen"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
```

### 2.2 Estrutura do Módulo Kitchen

#### 📁 `kitchen/src/commonMain/kotlin/co/kandalabs/comandaai/kitchen/KitchenModule.kt`
```kotlin
package co.kandalabs.comandaai.kitchen

import cafe.adriel.voyager.core.screen.Screen
import co.kandalabs.comandaai.kitchen.presentation.KitchenScreen

object KitchenModule {
    fun getKitchenScreen(): Screen = KitchenScreen()
}
```

### 2.3 Data Layer

#### 📁 `kitchen/src/commonMain/kotlin/co/kandalabs/comandaai/kitchen/domain/model/KitchenModels.kt`
```kotlin
package co.kandalabs.comandaai.kitchen.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class KitchenOrder(
    val id: Int,
    val tableNumber: Int,
    val items: List<KitchenItemDetail>,
    val createdAt: Long
)

@Serializable
data class KitchenItemDetail(
    val itemId: Int,
    val name: String,
    val totalCount: Int,
    val observation: String?,
    val unitStatuses: List<ItemUnitStatus>,
    val overallStatus: ItemStatus
)

@Serializable
data class ItemUnitStatus(
    val unitIndex: Int,
    val status: ItemStatus,
    val updatedAt: Long,
    val updatedBy: String?
)

enum class ItemStatus {
    OPEN,          // Pendente
    IN_PRODUCTION, // Em produção
    COMPLETED,     // Finalizado
    DELIVERED,     // Entregue
    CANCELED       // Cancelado
}
```

#### 📁 `kitchen/src/commonMain/kotlin/co/kandalabs/comandaai/kitchen/domain/repository/KitchenRepository.kt`
```kotlin
package co.kandalabs.comandaai.kitchen.domain.repository

import co.kandalabs.comandaai.kitchen.domain.model.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder

interface KitchenRepository {
    suspend fun getActiveOrders(): Result<List<KitchenOrder>>
    
    suspend fun updateItemUnitStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        status: ItemStatus
    ): Result<Unit>
}
```

#### 📁 `kitchen/src/commonMain/kotlin/co/kandalabs/comandaai/kitchen/data/api/KitchenApi.kt`
```kotlin
package co.kandalabs.comandaai.kitchen.data.api

import co.kandalabs.comandaai.kitchen.domain.model.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

interface KitchenApi {
    suspend fun getActiveOrders(): List<KitchenOrder>
    suspend fun updateItemUnitStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        status: ItemStatus
    )
}

class KitchenApiImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : KitchenApi {
    
    override suspend fun getActiveOrders(): List<KitchenOrder> {
        return httpClient.get("$baseUrl/api/v1/kitchen/orders").body()
    }
    
    override suspend fun updateItemUnitStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        status: ItemStatus
    ) {
        httpClient.put("$baseUrl/api/v1/kitchen/orders/$orderId/items/$itemId/unit/$unitIndex") {
            contentType(ContentType.Application.Json)
            setBody(UpdateItemStatusRequest(status.name))
        }
    }
}

@Serializable
private data class UpdateItemStatusRequest(val status: String)
```

#### 📁 `kitchen/src/commonMain/kotlin/co/kandalabs/comandaai/kitchen/data/repository/KitchenRepositoryImpl.kt`
```kotlin
package co.kandalabs.comandaai.kitchen.data.repository

import co.kandalabs.comandaai.kitchen.data.api.KitchenApi
import co.kandalabs.comandaai.kitchen.domain.model.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder
import co.kandalabs.comandaai.kitchen.domain.repository.KitchenRepository

class KitchenRepositoryImpl(
    private val kitchenApi: KitchenApi
) : KitchenRepository {
    
    override suspend fun getActiveOrders(): Result<List<KitchenOrder>> {
        return try {
            val orders = kitchenApi.getActiveOrders()
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateItemUnitStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        status: ItemStatus
    ): Result<Unit> {
        return try {
            kitchenApi.updateItemUnitStatus(orderId, itemId, unitIndex, status)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 2.4 Presentation Layer

#### 📁 `kitchen/src/commonMain/kotlin/co/kandalabs/comandaai/kitchen/presentation/KitchenScreenState.kt`
```kotlin
package co.kandalabs.comandaai.kitchen.presentation

import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder

data class KitchenScreenState(
    val orders: List<KitchenOrder> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)
```

#### 📁 `kitchen/src/commonMain/kotlin/co/kandalabs/comandaai/kitchen/presentation/KitchenViewModel.kt`
```kotlin
package co.kandalabs.comandaai.kitchen.presentation

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.kitchen.domain.model.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.repository.KitchenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class KitchenViewModel(
    private val repository: KitchenRepository
) : ScreenModel {
    
    private val _state = MutableStateFlow(KitchenScreenState())
    val state: StateFlow<KitchenScreenState> = _state.asStateFlow()
    
    init {
        loadActiveOrders()
    }
    
    fun loadActiveOrders() {
        _state.update { it.copy(isLoading = true, error = null) }
        
        screenModelScope.launch {
            repository.getActiveOrders()
                .onSuccess { orders ->
                    _state.update { 
                        it.copy(
                            orders = orders,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { 
                        it.copy(
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    fun updateItemStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        newStatus: ItemStatus
    ) {
        screenModelScope.launch {
            repository.updateItemUnitStatus(orderId, itemId, unitIndex, newStatus)
                .onSuccess {
                    // Recarregar pedidos para refletir mudanças
                    loadActiveOrders()
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message) }
                }
        }
    }
    
    fun refreshOrders() {
        _state.update { it.copy(isRefreshing = true) }
        
        screenModelScope.launch {
            repository.getActiveOrders()
                .onSuccess { orders ->
                    _state.update { 
                        it.copy(
                            orders = orders,
                            isRefreshing = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { 
                        it.copy(
                            error = error.message,
                            isRefreshing = false
                        )
                    }
                }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
```

#### 📁 `kitchen/src/commonMain/kotlin/co/kandalabs/comandaai/kitchen/presentation/KitchenScreen.kt`
```kotlin
package co.kandalabs.comandaai.kitchen.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import co.kandalabs.comandaai.designsystem.ComandaAiTheme
import co.kandalabs.comandaai.kitchen.presentation.components.OrderCard
import org.kodein.di.instance

class KitchenScreen : Screen {
    
    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel { KitchenViewModel(instance()) }
        val state by viewModel.state.collectAsState()
        
        KitchenScreenContent(
            state = state,
            onRefresh = viewModel::refreshOrders,
            onItemStatusChange = viewModel::updateItemStatus,
            onErrorDismiss = viewModel::clearError
        )
    }
}

@Composable
private fun KitchenScreenContent(
    state: KitchenScreenState,
    onRefresh: () -> Unit,
    onItemStatusChange: (Int, Int, Int, ItemStatus) -> Unit,
    onErrorDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cozinha - Pedidos Ativos",
                style = ComandaAiTheme.typography.h4,
                color = ComandaAiTheme.colors.onBackground
            )
            
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Atualizar",
                    tint = ComandaAiTheme.colors.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error handling
        state.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = ComandaAiTheme.colors.error.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Erro: $error",
                        color = ComandaAiTheme.colors.error,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onErrorDismiss) {
                        Text("Dispensar")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Loading state
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ComandaAiTheme.colors.primary)
            }
        } else if (state.orders.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum pedido ativo",
                    style = ComandaAiTheme.typography.body1,
                    color = ComandaAiTheme.colors.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            // Orders list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.orders) { order ->
                    OrderCard(
                        order = order,
                        onItemStatusChange = { itemId, unitIndex, status ->
                            onItemStatusChange(order.id, itemId, unitIndex, status)
                        }
                    )
                }
            }
        }
    }
}
```

### 2.5 Componentes UI

#### 📁 `kitchen/src/commonMain/kotlin/co/kandalabs/comandaai/kitchen/presentation/components/OrderCard.kt`
```kotlin
package co.kandalabs.comandaai.kitchen.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.designsystem.ComandaAiTheme
import co.kandalabs.comandaai.kitchen.domain.model.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun OrderCard(
    order: KitchenOrder,
    onItemStatusChange: (Int, Int, ItemStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = ComandaAiTheme.colors.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header do pedido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mesa ${order.tableNumber}",
                    style = ComandaAiTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    color = ComandaAiTheme.colors.primary
                )
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Pedido #${order.id}",
                        style = ComandaAiTheme.typography.body2,
                        color = ComandaAiTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                    
                    val time = Instant.fromEpochMilliseconds(order.createdAt)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                    Text(
                        text = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}",
                        style = ComandaAiTheme.typography.caption,
                        color = ComandaAiTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de itens
            order.items.forEachIndexed { index, item ->
                ItemRow(
                    item = item,
                    onStatusChange = { unitIndex, status ->
                        onItemStatusChange(item.itemId, unitIndex, status)
                    }
                )
                
                if (index < order.items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = ComandaAiTheme.colors.onSurface.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}
```

#### 📁 `kitchen/src/commonMain/kotlin/co/kandalabs/comandaai/kitchen/presentation/components/ItemRow.kt`
```kotlin
package co.kandalabs.comandaai.kitchen.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.designsystem.ComandaAiTheme
import co.kandalabs.comandaai.kitchen.domain.model.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.model.KitchenItemDetail

@Composable
fun ItemRow(
    item: KitchenItemDetail,
    onStatusChange: (Int, ItemStatus) -> Unit
) {
    Column {
        // Header do item
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = ComandaAiTheme.typography.body1,
                    fontWeight = FontWeight.Medium,
                    color = ComandaAiTheme.colors.onSurface
                )
                
                if (!item.observation.isNullOrBlank()) {
                    Text(
                        text = "Obs: ${item.observation}",
                        style = ComandaAiTheme.typography.body2,
                        color = ComandaAiTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            StatusBadge(
                status = item.overallStatus,
                count = item.totalCount
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Controles individuais se houver mais de 1 item
        if (item.totalCount > 1) {
            Text(
                text = "Controle Individual:",
                style = ComandaAiTheme.typography.caption,
                color = ComandaAiTheme.colors.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item.unitStatuses.forEachIndexed { index, unitStatus ->
                    ItemUnitControl(
                        unitIndex = index + 1,
                        currentStatus = unitStatus.status,
                        onStatusChange = { newStatus ->
                            onStatusChange(index, newStatus)
                        }
                    )
                }
            }
        } else {
            // Controle único para 1 item
            ItemUnitControl(
                unitIndex = 1,
                currentStatus = item.unitStatuses.firstOrNull()?.status ?: ItemStatus.OPEN,
                onStatusChange = { newStatus ->
                    onStatusChange(0, newStatus)
                }
            )
        }
    }
}
```

#### 📁 `kitchen/src/commonMain/kotlin/co/kandalabs/comandaai/kitchen/presentation/components/ItemUnitControl.kt`
```kotlin
package co.kandalabs.comandaai.kitchen.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.designsystem.ComandaAiTheme
import co.kandalabs.comandaai.kitchen.domain.model.ItemStatus

@Composable
fun ItemUnitControl(
    unitIndex: Int,
    currentStatus: ItemStatus,
    onStatusChange: (ItemStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = getStatusColor(currentStatus).copy(alpha = 0.1f),
                contentColor = getStatusColor(currentStatus)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "#$unitIndex",
                    style = ComandaAiTheme.typography.caption
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ItemStatus.values().forEach { status ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusIndicator(status = status)
                            Text(getStatusText(status))
                        }
                    },
                    onClick = {
                        onStatusChange(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusIndicator(status: ItemStatus) {
    Card(
        modifier = Modifier.size(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = getStatusColor(status)
        ),
        shape = androidx.compose.foundation.shape.CircleShape
    ) {}
}

@Composable
private fun getStatusColor(status: ItemStatus): androidx.compose.ui.graphics.Color {
    return when (status) {
        ItemStatus.OPEN -> ComandaAiTheme.colors.error
        ItemStatus.IN_PRODUCTION -> ComandaAiTheme.colors.primary
        ItemStatus.COMPLETED -> ComandaAiTheme.colors.secondary
        ItemStatus.DELIVERED -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        ItemStatus.CANCELED -> androidx.compose.ui.graphics.Color(0xFF757575)
    }
}

private fun getStatusText(status: ItemStatus): String {
    return when (status) {
        ItemStatus.OPEN -> "Pendente"
        ItemStatus.IN_PRODUCTION -> "Produzindo"
        ItemStatus.COMPLETED -> "Pronto"
        ItemStatus.DELIVERED -> "Entregue"
        ItemStatus.CANCELED -> "Cancelado"
    }
}
```

#### 📁 `kitchen/src/commonMain/kotlin/co/kandalabs/comandaai/kitchen/presentation/components/StatusBadge.kt`
```kotlin
package co.kandalabs.comandaai.kitchen.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.designsystem.ComandaAiTheme
import co.kandalabs.comandaai.kitchen.domain.model.ItemStatus

@Composable
fun StatusBadge(
    status: ItemStatus,
    count: Int
) {
    val (backgroundColor, textColor) = when (status) {
        ItemStatus.OPEN -> ComandaAiTheme.colors.error.copy(alpha = 0.1f) to ComandaAiTheme.colors.error
        ItemStatus.IN_PRODUCTION -> ComandaAiTheme.colors.primary.copy(alpha = 0.1f) to ComandaAiTheme.colors.primary
        ItemStatus.COMPLETED -> ComandaAiTheme.colors.secondary.copy(alpha = 0.1f) to ComandaAiTheme.colors.secondary
        ItemStatus.DELIVERED -> androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.1f) to androidx.compose.ui.graphics.Color(0xFF4CAF50)
        ItemStatus.CANCELED -> androidx.compose.ui.graphics.Color(0xFF757575).copy(alpha = 0.1f) to androidx.compose.ui.graphics.Color(0xFF757575)
    }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = count.toString(),
                style = ComandaAiTheme.typography.caption,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Text(
                text = getStatusText(status),
                style = ComandaAiTheme.typography.caption,
                color = textColor
            )
        }
    }
}

private fun getStatusText(status: ItemStatus): String {
    return when (status) {
        ItemStatus.OPEN -> "Pendente"
        ItemStatus.IN_PRODUCTION -> "Produzindo"
        ItemStatus.COMPLETED -> "Pronto"
        ItemStatus.DELIVERED -> "Entregue"
        ItemStatus.CANCELED -> "Cancelado"
    }
}
```

## 🏗️ Fase 3: Integração e Configuração - 2 dias

### 3.1 Configurar DI no App Principal

#### 📁 `app/src/commonMain/kotlin/co/kandalabs/comandaai/config/di.kt` - Adicionar módulo kitchen
```kotlin
val kitchenModule = module {
    single<KitchenApi> { 
        KitchenApiImpl(
            httpClient = instance(),
            baseUrl = "http://localhost:8081" // ou configuração de ambiente
        )
    }
    single<KitchenRepository> { KitchenRepositoryImpl(instance()) }
}

// No setupKoin():
modules(
    // módulos existentes...
    kitchenModule
)
```

### 3.2 Integrar Navegação Baseada em Role

#### 📁 `app/src/commonMain/kotlin/co/kandalabs/comandaai/presentation/ComandaAiApp.kt` - Atualizar navegação
```kotlin
@Composable
fun ComandaAiApp() {
    val userRole = getCurrentUserRole() // implementar
    
    ComandaAiTheme {
        Navigator(getInitialScreen(userRole)) { navigator ->
            SlideTransition(navigator)
        }
    }
}

private fun getInitialScreen(userRole: UserRole?): Screen {
    return when (userRole) {
        UserRole.KITCHEN -> KitchenModule.getKitchenScreen()
        UserRole.MANAGER, UserRole.ADMIN -> TablesScreen()
        UserRole.WAITER -> TablesScreen()
        null -> AuthModule.getLoginScreen()
    }
}
```

## 🏗️ Fase 4: Testes - 1 dia

### 4.1 Testes Backend

#### 📁 `CommanderAPI/src/test/kotlin/domain/service/KitchenServiceTest.kt`
```kotlin
class KitchenServiceTest {
    
    @Test
    fun `should return only orders with incomplete items`() {
        // Testar filtro de pedidos ativos
    }
    
    @Test
    fun `should update individual item unit status`() {
        // Testar atualização de status individual
    }
    
    @Test
    fun `should recalculate overall status when unit status changes`() {
        // Testar recálculo de status geral
    }
}
```

### 4.2 Testes Frontend

#### 📁 `kitchen/src/commonTest/kotlin/KitchenViewModelTest.kt`
```kotlin
class KitchenViewModelTest {
    
    @Test
    fun `should load active orders on init`() = runTest {
        // Testar carregamento inicial
    }
    
    @Test
    fun `should update item status and refresh list`() = runTest {
        // Testar atualização e refresh
    }
}
```

## 📅 Cronograma de Implementação

### **Dia 1-3: Backend**
- ✅ Expandir ItemStatus e criar tabela OrderItemStatusTable
- ✅ Implementar KitchenService e expandir OrderRepository
- ✅ Criar endpoints /api/v1/kitchen/*

### **Dia 4-7: Frontend**
- ✅ Criar módulo kitchen e estrutura base
- ✅ Implementar data layer (API, Repository)
- ✅ Desenvolver presentation layer (ViewModel, Screen, Components)
- ✅ Configurar DI e navegação

### **Dia 8-9: Integração e Testes**
- ✅ Integrar com SSE para tempo real
- ✅ Implementar testes unitários e de integração
- ✅ Validação final do sistema

## 🎯 IMPLEMENTAÇÃO CONCLUÍDA - STATUS FINAL

### ✅ **Backend Completamente Implementado**
1. **Rotas Kitchen Registradas** - `kitchenRoutes()` em `Application.kt:126`
2. **KitchenService Injetado** - `KitchenServiceImpl` configurado no módulo Koin
3. **Tabela Individual Criada** - `OrderItemStatusTable` implementada e registrada
4. **Controle Individual Funcional** - `updateItemUnitStatus()` implementado corretamente
5. **APIs Funcionais** - Endpoints `/api/v1/kitchen/*` operacionais

### ✅ **Frontend Completamente Integrado**
1. **Módulo Kitchen Criado** - Estrutura completa em `/kitchen/`
2. **UI Controle Individual** - `ItemUnitControl` com dropdown para dar baixa por unidade
3. **DI Configurado** - `kitchenModule` integrado ao `AppModule.di.kt`
4. **Navegação por Role** - `SplashScreen` redireciona `UserRole.KITCHEN` para `KitchenScreen`
5. **Design System** - Componentes seguem padrão estabelecido

### 🔧 **Funcionalidades Implementadas**
- **✅ Dar baixa individual** - Controle por unidade de cada item
- **✅ Status visual** - Cores e badges indicando status de cada unidade
- **✅ Cálculo automático** - Status geral calculado baseado nas unidades
- **✅ Persistência** - `OrderItemStatusTable` armazena status individual
- **✅ Role-based access** - Acesso restrito a usuários `KITCHEN` e `ADMIN`
- **✅ Tempo real** - Integração com arquitetura SSE existente
- **✅ Multiplatform** - Funciona em Android e iOS

### 📁 **Arquivos Principais Modificados/Criados**

#### Backend:
- `Application.kt` - Rotas e DI registrados
- `OrderRepositoryImpl.kt` - Controle individual implementado  
- `KitchenService.kt` + `KitchenServiceImpl.kt` - Serviços kitchen
- `KitchenRoutes.kt` - Endpoints API kitchen
- `KitchenModels.kt` - Models para kitchen
- `SQLTableObjects.kt` - `OrderItemStatusTable` criada

#### Frontend:
- `kitchen/` - Módulo completo criado
- `SplashScreen.kt` - Navegação por role adicionada
- `di.kt` - DI kitchen configurado
- `KitchenScreen.kt` - Tela principal da cozinha
- `ItemUnitControl.kt` - Componente de controle individual

### 🚀 **Sistema Pronto para Uso**
O módulo cozinha está **100% implementado** e permite:
1. Login com usuário role `KITCHEN` 
2. Visualização de pedidos ativos
3. **Dar baixa individual** em cada unidade de item
4. Atualização automática de status geral
5. Interface otimizada para cozinha

## 🚀 Comandos de Execução

### Backend
```bash
cd CommanderAPI
./gradlew run                    # Iniciar servidor
./gradlew test                   # Executar testes
```

### Frontend
```bash
cd Comanda-ai-kmp
./gradlew :kitchen:build        # Build módulo kitchen
./gradlew buildInstallStartApp  # Instalar e iniciar app
./gradlew testAll              # Executar todos os testes
```

## 📝 Considerações Importantes

1. **Compatibilidade**: Reutilizar ItemStatus existente com expansão
2. **Performance**: Cache para pedidos ativos
3. **Real-time**: Integração com SSE existente
4. **UI/UX**: Seguir Design System estabelecido
5. **Autenticação**: Verificar UserRole.KITCHEN
6. **Modularidade**: Seguir padrão estabelecido pelos módulos auth/core

## 🔄 Próximas Melhorias (Pós-MVP)
- [ ] Notificações push para novos pedidos
- [ ] Dashboard com métricas da cozinha
- [ ] Modo offline com sincronização
- [ ] Impressão automática de comandas
- [ ] Integração com sistema de estoque
- [ ] Analytics de tempo de preparo