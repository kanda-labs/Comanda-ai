# 🌐 Network Module

Módulo centralizado de configuração de rede para o projeto Comanda-ai.

## 🎯 **ÚNICO LOCAL PARA ALTERAR IP**

Para alterar o IP de todos os ambientes, modifique **apenas**:

### Android: `/network/build.gradle.kts`
```kotlin
buildConfigField("String", "BASE_IP", "\"127.0.0.1\"")  // ← MUDE AQUI
```

### iOS: `/network/src/iosMain/kotlin/.../NetworkConfig.kt`
```kotlin
actual val baseIp: String = "127.0.0.1"  // ← MUDE AQUI
```

## ⚙️ **Configuração Automática**

O módulo configura automaticamente:

| Build Type | Ambiente | Porta | Base URL |
|------------|----------|--------|----------|
| **Debug** | DEBUG | 8082 | `http://127.0.0.1:8082/` |
| **Release** | PRODUCTION | 8081 | `http://127.0.0.1:8081/` |

## 📋 **Como Usar nos Módulos**

### 1. Adicionar dependência
```kotlin
// build.gradle.kts
implementation(projects.network)
```

### 2. Usar configuração
```kotlin
import co.kandalabs.comandaai.network.NetworkConfig

// URL atual (baseado no build type)
val baseUrl = NetworkConfig.currentBaseUrl

// URLs específicas
val prodUrl = NetworkConfig.productionBaseUrl    // http://127.0.0.1:8081/
val debugUrl = NetworkConfig.debugBaseUrl       // http://127.0.0.1:8082/
```

### 3. Construir endpoints
```kotlin
import co.kandalabs.comandaai.network.NetworkUtils
import co.kandalabs.comandaai.network.NetworkEnvironment

// API endpoint
val loginUrl = NetworkUtils.buildApiUrl(
    environment = NetworkEnvironment.PRODUCTION,
    endpoint = "auth/login"
)
// Resultado: http://127.0.0.1:8081/api/v1/auth/login

// SSE endpoint  
val sseUrl = NetworkUtils.buildSseUrl(
    environment = NetworkEnvironment.DEBUG,
    endpoint = "orders/sse"
)
// Resultado: http://127.0.0.1:8082/api/v1/orders/sse
```

## 🔄 **Migração Completa**

### ✅ **Módulos Atualizados:**
- ✅ `app` - Usa NetworkConfig.currentBaseUrl
- ✅ `auth` - Usa NetworkUtils para construir URLs
- ✅ `kitchen` - Usa NetworkConfig.currentBaseUrl
- ✅ `network` - Centraliza toda configuração

### ❌ **Removido:**
- ❌ IPs hardcoded em build.gradle.kts
- ❌ URLs duplicadas em múltiplos arquivos
- ❌ AppConfig específico por plataforma

## 🎯 **Benefícios**

- ✅ **IP em 1 só lugar** - mudança simples
- ✅ **Módulo independente** - sem dependências externas  
- ✅ **Separação automática** - debug vs production
- ✅ **Reutilização** - todos os módulos usam a mesma fonte
- ✅ **Type-safe** - configuração tipada e segura

## 🔧 **Desenvolvimento**

```bash
# Testar módulo network
./gradlew :network:build

# Testar compilação completa
./gradlew :app:compileCommonMainKotlinMetadata
```

---

**🎉 Agora você pode alterar o IP do sistema inteiro modificando apenas 2 linhas de código!**