# ComandaAI - Builds Desktop

Este documento descreve como gerar executáveis desktop para diferentes ambientes da aplicação ComandaAI.

## 🚀 Comandos Rápidos

### Gerar Executáveis por Build Variant

| Build Variant | Comando | Porta | Arquivos Gerados |
|---------------|---------|-------|------------------|
| **DEBUG** | `./gradlew :app:createDistributableDebug -DbuildVariant=debug` | 8082 | ComandaAi-debug.dmg/msi/deb |
| **SANDBOX** | `./gradlew :app:createDistributableSandbox -DbuildVariant=sandbox` | 8081 | ComandaAi-sandbox.dmg/msi/deb |
| **RELEASE** | `./gradlew :app:createDistributableRelease -DbuildVariant=release` | 8081 | ComandaAi.dmg/msi/deb |
| **TODOS** | `./gradlew :app:createDistributableAll` | - | Gera todos os variants |

### Executar para Desenvolvimento

| Build Variant | Comando |
|---------------|---------|
| **DEBUG** | `./gradlew :app:run -DbuildVariant=debug` |
| **SANDBOX** | `./gradlew :app:run -DbuildVariant=sandbox` |
| **RELEASE** | `./gradlew :app:run -DbuildVariant=release` |

## 📦 Formatos de Saída

### Cada comando gera automaticamente 3 formatos:

- 🍎 **macOS**: `.dmg` (instalador nativo)
- 🖥️ **Windows**: `.msi` (instalador Windows)
- 🐧 **Linux**: `.deb` (pacote Debian/Ubuntu)

### ⚡ **Importante**: 
- **Um comando = 3 arquivos**: Não há comandos separados por OS
- **Cross-platform**: Execute no seu Mac e gere para todos os sistemas
- **Nomes únicos**: Cada variant tem nomes distintos para evitar conflitos

## 📁 Estrutura de Arquivos Gerados

```
app/build/compose/binaries/main/
├── debug/                    # BUILD VARIANT: DEBUG (8082)
│   ├── dmg/
│   │   └── ComandaAi-debug.dmg
│   ├── msi/  
│   │   └── ComandaAi-debug.msi
│   └── deb/
│       └── comandaai-debug.deb
├── sandbox/                  # BUILD VARIANT: SANDBOX (8081)
│   ├── dmg/
│   │   └── ComandaAi-sandbox.dmg
│   ├── msi/
│   │   └── ComandaAi-sandbox.msi  
│   └── deb/
│       └── comandaai-sandbox.deb
└── release/                  # BUILD VARIANT: RELEASE (8081)
    ├── dmg/
    │   └── ComandaAi.dmg
    ├── msi/
    │   └── ComandaAi.msi
    └── deb/
        └── comandaai.deb
```

## ⚙️ Configuração de Rede

### Portas por Ambiente

| Ambiente | Porta | URL Base | Uso |
|----------|-------|----------|-----|
| **DEBUG** | 8082 | http://192.168.2.200:8082 | Desenvolvimento |
| **SANDBOX** | 8081 | http://192.168.2.200:8081 | Teste/Homologação |
| **RELEASE** | 8081 | http://192.168.2.200:8081 | Produção |

**📝 Nota**: SANDBOX e RELEASE compartilham a porta 8081, mas geram executáveis com identificação visual diferente.

## 🎯 Cenários de Uso Comum

### 🔧 Desenvolvimento
```bash
# Executar para testar localmente
./gradlew :app:run -DbuildVariant=debug
```

### 🧪 Teste/QA  
```bash
# Gerar executável sandbox para equipe de teste
./gradlew :app:createDistributableSandbox -DbuildVariant=sandbox
```

### 🚀 Produção
```bash
# Gerar executável final para distribuição
./gradlew :app:createDistributableRelease -DbuildVariant=release
```

### 📦 Distribuição Completa
```bash
# Gerar todos os executáveis de uma vez
./gradlew :app:createDistributableAll
```

## 🔧 Configuração Técnica

### Arquivos de Configuração

#### NetworkConfig Desktop
```kotlin
// network/src/desktopMain/kotlin/co/kandalabs/comandaai/network/NetworkConfig.kt
actual val currentBaseUrl: String = when (getBuildVariant()) {
    "debug" -> debugBaseUrl        // 8082
    "sandbox" -> productionBaseUrl  // 8081  
    "release" -> productionBaseUrl  // 8081
    else -> debugBaseUrl
}
```

#### Build Configuration
```kotlin
// app/build.gradle.kts
compose.desktop {
    application {
        // Detecta variant automaticamente via -DbuildVariant
        val currentVariant = System.getProperty("buildVariant") ?: "debug"
        
        // Configura nome e Bundle ID baseado no variant
        packageName = "ComandaAi$variantSuffix"
        bundleID = "co.kandalabs.comandaai.desktop$bundleSuffix"
    }
}
```

## ⚠️ Requisitos do Sistema

### Para Gerar Executáveis
- ☕ **Java 17+**
- 🐘 **Gradle** configurado
- 💻 **Qualquer OS** (Mac, Windows, Linux)

### Para Executar Aplicação
- 🖥️ **macOS 10.15+** (para .dmg)
- 🖥️ **Windows 10+** (para .msi)
- 🐧 **Ubuntu/Debian** (para .deb)

## 🆔 Identificação dos Executáveis

### Bundle IDs únicos por ambiente:
- **DEBUG**: `co.kandalabs.comandaai.desktop.debug`
- **SANDBOX**: `co.kandalabs.comandaai.desktop.sandbox`  
- **RELEASE**: `co.kandalabs.comandaai.desktop`

### Nomes de aplicação:
- **DEBUG**: "ComandaAI Debug"
- **SANDBOX**: "ComandaAI Sandbox"
- **RELEASE**: "ComandaAI"

## 🔄 Workflow Sugerido

### Para Desenvolvedores
1. **Desenvolvimento**: `./gradlew :app:run -DbuildVariant=debug`
2. **Teste local**: `./gradlew :app:createDistributableDebug -DbuildVariant=debug`

### Para QA/Teste  
1. **Gerar sandbox**: `./gradlew :app:createDistributableSandbox -DbuildVariant=sandbox`
2. **Distribuir**: Enviar arquivos da pasta `app/build/compose/binaries/main/sandbox/`

### Para Release
1. **Gerar produção**: `./gradlew :app:createDistributableRelease -DbuildVariant=release`
2. **Distribuir**: Enviar arquivos da pasta `app/build/compose/binaries/main/release/`

## 📋 Lista de Verificação

Antes de gerar executáveis:
- [ ] Servidor rodando na porta correta (8081 ou 8082)
- [ ] Código commitado e testado
- [ ] Build variant correto especificado
- [ ] Espaço suficiente em disco (~500MB por build)

## 🐛 Troubleshooting

### Problema: Erro "buildVariant not found"
**Solução**: Sempre incluir `-DbuildVariant=<variant>` no comando

### Problema: Executável não conecta
**Solução**: Verificar se o servidor está rodando na porta configurada

### Problema: Arquivos não encontrados
**Solução**: Executáveis ficam em `app/build/compose/binaries/main/`

## 📞 Suporte

Para modificar configurações de rede, edite:
```
network/src/desktopMain/kotlin/co/kandalabs/comandaai/network/NetworkConfig.kt
```

---

**🎉 Pronto para distribuir!** Use os comandos acima para gerar executáveis nativos do ComandaAI para todos os sistemas operacionais.