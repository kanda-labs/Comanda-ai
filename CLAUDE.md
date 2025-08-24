# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with the **Comanda-ai** restaurant order management system.

## 🏗️ Project Structure

```
comanda-ai/
├── CommanderAPI/        # Backend REST API (Kotlin/Ktor)
└── Comanda-ai-kmp/     # Mobile app (Kotlin Multiplatform)
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
├── app/            # Main app (MVVM implementation)
├── auth/           # Authentication module (Login, Registration)
├── core/           # Utilities, error handling
└── designsystem/   # UI components, theming
```

**Key Screens:** LoginScreen, TablesScreen, TableDetailsScreen, ItemsScreen, OrderScreen

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
| PORT | 8081 | Server port |
| HOST | 0.0.0.0 | Server host |
| DATABASE_URL | jdbc:sqlite:data.db | Database connection |
| LOG_LEVEL | INFO | Logging level |

### Requirements
- **Backend:** JDK 22+
- **Mobile:** Android SDK 35 (min 25), iOS support
- **Build:** Gradle with Kotlin DSL

## 📊 Database Schema
SQLite with tables: Users, Tables, Items, Orders, Bills (see `SQLTableObjects.kt`)

## 🔗 Key Files
- **Backend API:** `CommanderApi.kt`
- **Database:** `DatabaseConfig.kt`, `SQLTableObjects.kt`
- **Mobile DI:** Check DI modules for base URL configuration
- **Platform Code:** `androidMain/`, `iosMain/`, `commonMain/`
- **API Documentation:** `API_ENDPOINTS.md`

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
auth {
    - core (error handling, utilities)
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
- **auth/AuthModule.kt**: Public API for auth integration
- **auth/presentation/login/LoginScreen.kt**: Main login interface
- **auth/presentation/login/LoginViewModel.kt**: Login business logic
- **auth/presentation/login/LoginScreenState.kt**: UI state management

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