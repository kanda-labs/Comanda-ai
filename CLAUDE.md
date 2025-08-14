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
./gradlew run                    # Start server (localhost:8080)
./gradlew test                   # Run tests
docker-compose up -d             # Start with Docker
```

### Mobile Development
```bash
cd Comanda-ai-kmp
./gradlew build                  # Build all targets
./gradlew :app:assembleDebug     # Build Android APK
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
├── core/           # Utilities, error handling
└── designsystem/   # UI components, theming
```

**Key Screens:** TablesScreen, TableDetailsScreen, ItemsScreen

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

## ⚙️ Configuration

### Environment Variables (Backend)
| Variable | Default | Description |
|----------|---------|-------------|
| PORT | 8080 | Server port |
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