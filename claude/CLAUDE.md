# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with the **Comanda-ai** restaurant order management system.

## 🏗️ Project Structure

```
comanda-ai/
├── CommanderAPI/        # Backend REST API (Kotlin/Ktor)
├── Comanda-ai-kmp/     # Mobile app (Kotlin Multiplatform)
│   ├── app/            # Main mobile application
│   ├── core/           # Core modules (organized namespace)
│   │   ├── auth/       # Authentication module
│   │   ├── network/    # Network configuration module  
│   │   └── sdk/        # Shared SDK utilities (renamed from core)
│   ├── features/       # Feature modules (organized namespace)
│   │   ├── attendance/ # Restaurant attendance features
│   │   ├── domain/     # Domain models (moved to features)
│   │   └── kitchen/    # Kitchen management module
│   └── designsystem/   # UI components and theming
└── claude/             # Documentation and guidance files
```

## 🚀 Quick Start

### Backend Development
```bash
cd CommanderAPI
./gradlew run                    # Start server (localhost:8081) - Production
./gradlew runDebug               # Start server (localhost:8082) - Debug mode
./gradlew test                   # Run tests
docker-compose up -d             # Start with Docker
```

### Mobile Development
```bash
cd Comanda-ai-kmp
./gradlew build                  # Build all targets
./gradlew :app:assembleDebug     # Build Android APK
./gradlew :auth:build            # Build auth module
./gradlew test                   # Run tests
```

### Root Project Tasks
```bash
./gradlew buildInstallStartApp   # Build, install and start Android app
./gradlew buildAll               # Build all sub-projects
./gradlew testAll                # Test all sub-projects
./gradlew cleanAll               # Clean all sub-projects
```

### Environment Management (NEW)
```bash
# Production environment (port 8081)
./start-production.sh            # Start production server

# Debug environment (port 8082)
./start-debug.sh                 # Start debug server
./start-debug-with-prod-data.sh  # Start debug with production data copy

# Database management
./manage-databases.sh            # Interactive database management
./copy-prod-to-debug.sh         # Copy production data to debug environment
```

## 🏛️ Architecture

### CommanderAPI (Backend)
- **Stack:** Kotlin 2.0.0, Ktor 2.3.8, SQLite + Exposed ORM, Koin DI
- **Pattern:** Clean Architecture with Repository pattern
- **API:** REST API at `/api/v1` with Swagger docs at `/swagger-ui`

**Package Structure:**
```
kandalabs.commander/
├── application/     # DI setup, configuration
├── core/           # Utilities, extensions
├── data/           # Repositories, database
├── domain/         # Entities, interfaces, services
└── presentation/   # REST routes, DTOs
```

**Core Entities:** User, Table, Item, Order, Bill

### Comanda-ai-kmp (Mobile)
- **Stack:** Kotlin 2.1.10, Compose Multiplatform, Voyager, Kodein DI, Ktor Client
- **Pattern:** MVVM + Clean Architecture
- **Platforms:** Android (SDK 25-35), iOS

**Module Structure:**
```
├── app/                    # Main app (MVVM implementation)
├── core/                   # Core modules namespace
│   ├── auth/              # Authentication module (Login, Registration)
│   ├── network/           # Centralized network configuration
│   └── sdk/               # Shared utilities, error handling (renamed from core)
├── features/               # Feature modules namespace
│   ├── attendance/        # Restaurant attendance features (Tables, Orders, Items)
│   ├── domain/            # Shared domain models
│   └── kitchen/           # Kitchen management module (Order control, Real-time updates)
└── designsystem/          # UI components, theming
```

**Key Screens:** LoginScreen, TablesScreen, TableDetailsScreen, ItemsScreen, OrderScreen, KitchenScreen

## 🔄 Recent Architecture Changes (NEW)

### Module Reorganization (2024)
The project underwent a major reorganization to improve modularity and maintainability:

#### Core Modules Namespace
- **`core:auth`** - Authentication module (formerly `auth`)
- **`core:network`** - Network configuration with auto IP management
- **`core:sdk`** - Shared utilities and SDK components (renamed from `core`)

#### Features Modules Namespace  
- **`features:attendance`** - Restaurant table and order management
- **`features:domain`** - Shared domain models (moved from root)
- **`features:kitchen`** - Kitchen operations and real-time updates

#### Benefits of New Architecture
- ✅ **Clear Separation**: Core infrastructure vs business features
- ✅ **Scalability**: Easy to add new features under `features:*`
- ✅ **Dependency Management**: Better module boundaries
- ✅ **Auto-Generated Config**: Network settings from single source
- ✅ **Type-Safe Accessors**: Gradle projects.core.* and projects.features.*

### Network Configuration Revolution
- **Before**: Manual IP updates in multiple files across platforms
- **After**: Single `local.properties` edit → automatic regeneration everywhere
- **Platforms Supported**: Android (BuildConfig), iOS (Generated Kotlin), Desktop (Generated Kotlin)

### Migration Status
- ✅ All 62 Kotlin files updated with new package structure
- ✅ Gradle configurations updated with new module references  
- ✅ DI modules reconfigured for new architecture
- ✅ Build system generates network config automatically
- ✅ Cross-platform builds working (Android, iOS, Desktop)

## 🛠️ Development Guidelines

### Backend
- Use Clean Architecture with clear layer separation
- Koin for dependency injection
- Repository interfaces in domain layer
- Consistent error handling with proper HTTP status codes
- Health checks at `/health` endpoint

### Mobile
- MVVM with Voyager ScreenModel ViewModels
- Compose UI with existing design system components
- Voyager for navigation
- Ktor Client + Ktorfit for API communication
- SQLDelight for local persistence
- Kodein for dependency injection
- **Core Modules**: Organized core functionality (auth, network, sdk)
- **Features Modules**: Business logic separated by domain (attendance, kitchen, domain)
- **Network Module**: Automatic IP configuration from local.properties

### Testing
- **Backend:** JUnit 5 + MockK, separate test config
- **Mobile:** Kotlin Test + AssertK + Turbine, Compose testing

## 🔑 Demo Credentials

For testing and development, use these demo user credentials:

| Username | Password | Role | Description |
|----------|----------|------|-------------|
| `leonardo-paixao` | `123456` | MANAGER | Manager access |
| `lueny-paixao` | `142824` | USER | Regular user |
| `admin` | `122825` | ADMIN | Administrator |
| `test` | `1234` | USER | Test user |
| `rennan-viana` | `123456` | USER | Regular user |

**Note:** These are demo credentials for development only. In production, proper password hashing and secure authentication should be implemented.

## 🍽️ Menu Items

The system comes with these pre-configured menu items:

### Espetinhos (SKEWER)
- **Espetinho de Alcatra** - R$ 8,00 (ID: 1)
- **Filé com Alho** - R$ 9,00 (ID: 2)
- **Medalhão de Frango** - R$ 10,00 (ID: 3)
- **Batata Frita** - R$ 15,00 (ID: 7)

### Chopp (CHOPP)
- **Chopp** - R$ 10,00 (ID: 4)

### Bebidas sem Álcool (NON_ALCOHOLIC_DRINKS)
- **Água** - R$ 3,00 (ID: 5)
- **Refrigerante** - R$ 5,00 (ID: 6)

### Item Categories
- `SKEWER`: Food items and appetizers
- `DRINK`: Alcoholic and non alcoholic drinks

## 📋 API Endpoints

### Authentication
- `POST /api/v1/auth/login` - User authentication

### Items Management
- `GET /api/v1/items` - List all items
- `GET /api/v1/items/{id}` - Get item by ID
- `POST /api/v1/items` - Create new item
- `PUT /api/v1/items/{id}` - Update item
- `DELETE /api/v1/items/{id}` - Delete item

### Tables Management
- `GET /api/v1/tables` - List all tables
- `GET /api/v1/tables/{id}` - Get table details
- `PUT /api/v1/tables/{id}` - Update table status

### Orders & Bills
- `POST /api/v1/bills` - Create bill (opens table)
- `GET /api/v1/bills/{tableId}` - Get bill for table
- `POST /api/v1/orders` - Create order
- `GET /api/v1/orders` - List orders

### Server-Sent Events
- `GET /api/v1/orders/sse` - Real-time order updates

**API Documentation:** Available at `/swagger-ui` when server is running

## ⚙️ Configuration

### Environment Variables (Backend)
| Variable | Default | Description |
|----------|---------|-------------|
| PORT | 8081/8082 | Server port (prod/debug) |
| HOST | 0.0.0.0 | Server host |
| DATABASE_URL | data.db/data-debug.db | Database connection |
| ENVIRONMENT | production/debug | Environment type |
| LOG_LEVEL | INFO | Logging level |

### Network Configuration (Mobile)
**Automatic configuration from `local.properties`:**
- **Current IP**: `10.0.2.2` (from local.properties)
- **Production**: `10.0.2.2:8081` (Release builds)
- **Debug**: `10.0.2.2:8082` (Debug builds)  
- **Change IP**: Update only `local.properties` → `base.ip=YOUR_IP` (auto-regenerates for all platforms)

### Build Variants
| Build Type | App ID | Server Port | Database |
|------------|--------|-------------|----------|
| **Release** | `co.kandalabs.comandaai` | 8081 | `data.db` |
| **Debug** | `co.kandalabs.comandaai.debug` | 8082 | `data-debug.db` |

### Requirements
- **Backend:** JDK 22+
- **Mobile:** Android SDK 35 (min 25), iOS support
- **Build:** Gradle with Kotlin DSL

## 📊 Database Schema
SQLite with tables: Users, Tables, Items, Orders, Bills (see `SQLTableObjects.kt`)

## 🚦 Status Management System (NEW)

O sistema possui controle abrangente de status para todos os entities principais:

### Status Types Overview
- **🍽️ Table Status**: `FREE` → `OCCUPIED` → `ON_PAYMENT` (3 states)
- **💰 Bill Status**: `OPEN` → `PAID/CANCELED/SCAM` (4 states)  
- **📝 Order Status**: `PENDING` → `DELIVERED/CANCELED` (3 states)
- **🍳 Item Status**: `PENDING` → `DELIVERED/CANCELED` (3 states)

### Key Features
- ✅ **Granular Control**: Status individual por unidade de item
- ✅ **Real-time Updates**: Atualizações automáticas via SSE
- ✅ **Visual Indicators**: Cores específicas por status na UI
- ✅ **Legacy Compatibility**: Suporte a status antigos durante migração
- ✅ **Business Rules**: Regras de transição de estado bem definidas

### Status Documentation
Consulte `STATUS_DEFINITIONS.md` para documentação completa de:
- Definições detalhadas de cada status
- Fluxos de transição de estado
- Mapping entre frontend/backend  
- Cores e indicadores visuais
- Regras de negócio por entity
- Guia de migração legacy → novo sistema

## 🔗 Key Files
- **Backend API:** `CommanderApi.kt`
- **Database:** `DatabaseConfig.kt`, `SQLTableObjects.kt`
- **Mobile DI:** Check DI modules for base URL configuration
- **Platform Code:** `androidMain/`, `iosMain/`, `commonMain/`
- **API Documentation:** `API_ENDPOINTS.md`
- **Status Definitions:** `STATUS_DEFINITIONS.md` (NEW)

## 🍽️ Table Status Management

### Table Status Flow
The app implements a complete table status management system:

```
FREE (Livre) → [Abrir conta] → OCCUPIED (Ocupada) → [Fechar conta] → ON_PAYMENT (Em pagamento)
```

### Status Mapping (Frontend ↔ Backend)
| Frontend Status | Backend Status | Description |
|-----------------|----------------|-------------|
| `FREE` | `CLOSED` | Mesa livre, sem conta ativa |
| `OCCUPIED` | `OPEN` | Mesa ocupada, conta ativa |
| `ON_PAYMENT` | `ON_PAYMENT` | Mesa em processo de pagamento |

### UI Behavior by Status

| Status | Badge Color | Primary Button | Secondary Button |
|---------|-------------|----------------|------------------|
| **FREE** | 🟢 Verde "Livre" | "Abrir conta" | "Voltar" |
| **OCCUPIED** | 🟡 Amarelo "Ocupada" | "Fazer pedido" | "Fechar conta" |
| **ON_PAYMENT** | 🟠 Laranja "Em pagamento" | - | "Voltar" |

### Key Implementation Points
- **Opening account**: Creates a bill via `POST /bills` and auto-updates table status
- **Closing account**: Updates table status via `PUT /tables/{id}` to `ON_PAYMENT`
- **Making orders**: Navigates to OrderScreen with `billId` from active table
- **Auto-refresh**: UI automatically updates after status changes by fetching fresh data
- **Repository methods**: `openTable()`, `closeTable()`, `getTableById()` for status management

### Important Files for Table Status
- **TablesDetailsViewModel.kt**: Status update logic with auto-refresh
- **TableDetailsScreenState.kt**: UI state based on table status
- **TablesRepository.kt**: Interface with status management methods
- **TablesRepositoryImp.kt**: API integration for status updates

## 🔐 Authentication Module

The app uses a modularized authentication system with the `auth` module providing login functionality.

### Auth Module Structure
```
auth/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/co/kandalabs/comandaai/auth/
    │   ├── AuthModule.kt                    # Public API
    │   └── presentation/login/
    │       ├── LoginScreen.kt              # Login UI (Compose)
    │       ├── LoginViewModel.kt           # MVVM ViewModel
    │       └── LoginScreenState.kt         # UI State
    └── androidMain/
        └── AndroidManifest.xml
```

### Using Auth Module
```kotlin
// Import auth functionality
import co.kandalabs.comandaai.auth.AuthModule

// Get login screen
val loginScreen = AuthModule.getLoginScreen()

// Example integration in ComandaAiApp
@Composable
fun ComandaAiApp() {
    ComandaAiTheme {
        Navigator(AuthModule.getLoginScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}
```

### Login Screen Features
- ✅ **Form Validation**: Username (min 3 chars) and password (min 4 chars)
- ✅ **Real-time Validation**: Immediate feedback on input errors
- ✅ **Loading States**: Button disabled during authentication
- ✅ **Error Handling**: Display authentication errors with styling
- ✅ **Keyboard Navigation**: Tab between fields, submit on Done
- ✅ **Design System**: Uses ComandaAi colors, spacing, and components
- ✅ **Multiplatform**: Works on Android and iOS

### Auth Module Dependencies
```kotlin
core:auth {
    - core:sdk (error handling, utilities)
    - designsystem (UI components, theming)
    - voyager (navigation framework)
    - kodein (dependency injection)
    - compose (UI framework)
}
```

### Future Auth Features (Planned)
- 📝 User registration screen
- 🔐 Password recovery flow
- 👥 Social login (Google, Apple)
- 🔒 2FA authentication
- 💾 Token persistence
- 🔄 Automatic token refresh

### Key Auth Files
- **core/auth/AuthModule.kt**: Public API for auth integration
- **core/auth/presentation/login/LoginScreen.kt**: Main login interface
- **core/auth/presentation/login/LoginViewModel.kt**: Login business logic
- **core/auth/presentation/login/LoginScreenState.kt**: UI state management

## 🍳 Kitchen Module (NEW)

The app now includes a dedicated kitchen module for order management and real-time kitchen operations.

### Kitchen Module Structure
```
features/kitchen/
├── build.gradle.kts
└── src/
    └── commonMain/kotlin/co/kandalabs/comandaai/kitchen/
        ├── KitchenModule.kt              # Public API
        ├── data/                         # Data layer
        │   └── api/KitchenSSEClient.kt   # Real-time order updates
        ├── di/KitchenModule.kt           # Dependency injection
        ├── domain/                       # Domain models
        └── presentation/
            ├── KitchenScreen.kt          # Main kitchen interface
            ├── KitchenViewModel.kt       # Business logic
            ├── KitchenScreenState.kt     # UI state
            └── components/               # Kitchen-specific components
```

### Kitchen Screen Features
- ✅ **Real-time Order Updates**: SSE connection for live order status
- ✅ **Order Control**: View and manage active orders
- ✅ **Item Status Management**: Mark items as prepared, delivered, etc.
- ✅ **Order Filtering**: Toggle between active and delivered orders
- ✅ **Connection Status**: Visual indicator for SSE connection
- ✅ **User Profile**: Avatar with logout functionality
- ✅ **Order Overview**: Summary view with category filtering
- ✅ **Delivered Order Tracking**: Complete order lifecycle management

### Kitchen Module Integration
```kotlin
// Access kitchen functionality
import co.kandalabs.comandaai.kitchen.KitchenModule

// Get kitchen screen
val kitchenScreen = KitchenModule.getKitchenScreen()

// DI module
val kitchenDI = KitchenModule.kitchenDI
```

### Kitchen API Endpoints
- `GET /api/v1/kitchen/orders` - Get all kitchen orders with real-time updates
- `PUT /api/v1/kitchen/orders/{id}/items/{itemId}` - Update item status
- `PUT /api/v1/kitchen/orders/{id}/delivered` - Mark order as delivered
- `GET /api/v1/kitchen/events` - SSE endpoint for real-time updates

### Key Kitchen Files
- **features/kitchen/KitchenModule.kt**: Public API for kitchen integration
- **features/kitchen/presentation/KitchenScreen.kt**: Main kitchen management interface
- **features/kitchen/presentation/KitchenViewModel.kt**: Kitchen business logic
- **features/kitchen/data/api/KitchenSSEClient.kt**: Real-time order updates via SSE

## 📱 Order Details Modal

The table details screen includes an interactive order details modal for viewing order items and their status.

### Modal Features
- ✅ **Click to Open**: Click any order in the table details to view details
- ✅ **90% Height**: Modal occupies 90% of screen height
- ✅ **Bottom Alignment**: Slides up from bottom of screen
- ✅ **Drag to Dismiss**: Drag downward to close (150px threshold)
- ✅ **Order Number**: Title shows "Pedido Nº X"
- ✅ **Item List**: Shows all order items with individual status
- ✅ **Status Colors**: Visual indicators (Atendido/Pendente/Cancelado)
- ✅ **Quantity Display**: Shows item count and observations
- ✅ **Bottom Button**: "Voltar" button fixed at bottom

### Implementation Files
- **OrderDetailsModal.kt**: Modal component with drag-to-dismiss
- **TableDetailsAction.kt**: SHOW_ORDER_DETAILS action
- **TableDetailsScreenState.kt**: selectedOrderForDetails state
- **TablesDetailsViewModel.kt**: showOrderDetails() / hideOrderDetails() methods
- **TableDetailsOrders.kt**: Order click handling

### Usage
```kotlin
// Orders list with click handler
TableDetailsOrders(
    orders = state.orders.ordersPresentation,
    onOrderClick = { order -> viewModel.showOrderDetails(order) }
)

// Modal integration
state.selectedOrderForDetails?.let { order ->
    OrderDetailsModal(
        isVisible = true,
        order = order,
        onDismiss = { viewModel.hideOrderDetails() }
    )
}
```

## 🌐 Network Module (NEW)

Centralized network configuration module that manages all API endpoints and environment settings.

### Network Configuration Features
- ✅ **Single IP Configuration**: Change IP in one place for entire app
- ✅ **Environment Separation**: Automatic production/debug URL selection
- ✅ **Build-Type Aware**: Different URLs for release vs debug builds
- ✅ **Type-Safe URLs**: Utility functions for building endpoints
- ✅ **Modular**: Independent module used by all other modules

### Network Module Structure
```
core/network/
├── build.gradle.kts                    # Auto-reads local.properties for IP config
└── src/
    ├── commonMain/kotlin/.../network/
    │   ├── NetworkConfig.kt            # Common network interface
    │   └── generated/
    │       └── GeneratedNetworkConfig.kt # Auto-generated from local.properties
    ├── androidMain/kotlin/.../network/
    │   └── NetworkConfig.kt            # Android implementation (BuildConfig)
    └── iosMain/kotlin/.../network/
        └── NetworkConfig.kt            # iOS implementation (generated config)
```

### How to Change IP Address (NEW)
**All Platforms**: Edit `/local.properties` (one place for everything!)
```properties
base.ip=YOUR_IP_HERE
production.port=8081
debug.port=8082
```
The build system automatically regenerates configuration for Android, iOS, and Desktop on any compilation.

### Network Usage in Modules
```kotlin
import co.kandalabs.comandaai.network.NetworkConfig
import co.kandalabs.comandaai.network.NetworkUtils
import co.kandalabs.comandaai.network.NetworkEnvironment

// Current environment URL (based on build type)
val currentUrl = NetworkConfig.currentBaseUrl

// Build API endpoints
val loginUrl = NetworkUtils.buildApiUrl(
    environment = NetworkEnvironment.PRODUCTION,
    endpoint = "auth/login"
)

// Build SSE endpoints
val sseUrl = NetworkUtils.buildSseUrl(
    environment = NetworkEnvironment.DEBUG,
    endpoint = "orders/sse"
)
```

### Migrated Modules
- ✅ **app**: Uses NetworkConfig.currentBaseUrl
- ✅ **core:auth**: Uses NetworkUtils for URL building  
- ✅ **features:kitchen**: Uses NetworkConfig for all connections
- ✅ **core:network**: Centralizes all configuration with auto-generation
- ✅ **features:attendance**: Uses generated network configuration

## 🔄 Environment Separation (NEW)

The project now supports complete environment separation for safe development.

### Environment Overview
| Environment | Server Port | Database | App ID Suffix | Use Case |
|-------------|-------------|----------|---------------|----------|
| **PRODUCTION** | 8081 | `data.db` | none | Live restaurant operations |
| **DEBUG** | 8082 | `data-debug.db` | `.debug` | Development & testing |

### Benefits
- ✅ **Safe Development**: Debug environment completely isolated from production
- ✅ **Dual Installation**: Both apps can run simultaneously on same device
- ✅ **Data Protection**: Production data remains untouched during development
- ✅ **Flexible Testing**: Copy production data to debug for realistic testing
- ✅ **Automatic Configuration**: Build system handles environment selection

### Quick Environment Setup
```bash
# Start production server
./start-production.sh

# Start debug server (separate terminal)
./start-debug.sh

# Or start debug with production data copy
./start-debug-with-prod-data.sh

# Manage databases interactively
./manage-databases.sh
```