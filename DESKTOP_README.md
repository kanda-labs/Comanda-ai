# ComandaAI - App Desktop

Este documento descreve como utilizar a versão desktop do ComandaAI criada com Compose Multiplatform.

## 🏗️ Estrutura Implementada

O suporte desktop foi adicionado ao projeto ComandaAI KMP com as seguintes funcionalidades:

### ✅ Configuração Completa
- **Target Desktop JVM** configurado em todos os módulos (app, auth, core, designsystem, domain, kitchen, network)
- **Dependências específicas** para desktop (Compose Desktop, Ktor OkHttp, SQLite driver)
- **Configuração de DI** (Dependency Injection) para desktop
- **NetworkConfig** para desktop com configuração de IP/porta específica
- **Gerenciador de sessão** para desktop
- **Configuração do banco SQLite** para desktop

### 📁 Estrutura de Arquivos Criados
```
Comanda-ai-kmp/app/src/desktopMain/
├── kotlin/co/kandalabs/comandaai/
│   ├── Main.kt                              # Ponto de entrada da aplicação desktop
│   ├── config/
│   │   ├── PlatformDI.kt                   # Configuração de DI específica
│   │   └── sqldelight/DriverFactory.kt     # Factory do SQLite driver
│   └── platform/SessionManagerProvider.kt  # Provedor de sessão
└── resources/                               # Recursos (ícones, etc)
```

## 🚀 Como Executar

### Pré-requisitos
- Java 17 ou superior
- Gradle configurado

### Comandos de Build

#### 1. Compilar JAR Desktop
```bash
cd Comanda-ai-kmp
./gradlew :app:desktopJar
```

#### 2. Executar Diretamente
```bash
cd Comanda-ai-kmp
./gradlew :app:runDistributable
```

#### 3. Criar Distribuição Nativa
```bash
# Para criar pacotes nativos (DMG no macOS, MSI no Windows, DEB no Linux)
./gradlew :app:createDistributable
```

### Executar o JAR
Após compilar com sucesso:
```bash
java -jar app/build/compose/jars/ComandaAi-desktop-1.0.0.jar
```

## ⚙️ Configuração

### Configuração de Rede
O app desktop está configurado para usar:
- **IP Base**: 192.168.2.200
- **Porta Produção**: 8081
- **Porta Debug**: 8082
- **URL Atual**: Debug (8082) por padrão

Para alterar, edite: `network/src/desktopMain/kotlin/co/kandalabs/comandaai/network/NetworkConfig.kt`

### Banco de Dados
- Usa SQLite local
- Arquivo: `ComandaAiDatabase.db` no diretório de execução
- Driver: SQLite JDBC para desktop (diferente do native driver usado no iOS)

## 🎯 Funcionalidades

O app desktop herda todas as funcionalidades do app móvel:
- Sistema de autenticação
- Gerenciamento de mesas
- Pedidos e comandas
- Cozinha
- Interface responsiva adaptada para desktop

## 📦 Distribuição

### Formatos Suportados
- **macOS**: .dmg
- **Windows**: .msi
- **Linux**: .deb

### Configuração de Ícones
Para adicionar ícones customizados:
1. Adicione os arquivos:
   - `src/desktopMain/resources/icon.icns` (macOS)
   - `src/desktopMain/resources/icon.ico` (Windows)  
   - `src/desktopMain/resources/icon.png` (Linux)

## ⚠️ Status Atual

### ✅ Implementado
- Configuração completa de build para desktop
- Estrutura de DI e configuração
- Interface de usuário (herda do código comum)
- Configuração de rede
- Banco de dados SQLite

### 🔧 Requer Correção
- Alguns erros de compilação no código comum (não específicos do desktop):
  - Uso de `String.format` não compatível com KMP
  - Propriedades não resolvidas em alguns UseCase

### 📋 Próximos Passos
1. Corrigir erros de compilação no código comum
2. Testar funcionalidades específicas do desktop
3. Otimizar interface para uso desktop (layouts maiores, atalhos de teclado)
4. Configurar distribuição automática

## 🛠️ Troubleshooting

### Problema: Erro de compilação com String.format
**Solução**: Substituir `String.format` por alternativas KMP compatíveis.

### Problema: SQLite não encontrado
**Solução**: Verificar se o driver SQLite está incluído nas dependências.

### Problema: Erro de DI
**Solução**: Verificar se o módulo platformDI está configurado corretamente.

## 📞 Suporte

Para problemas específicos do desktop, verificar:
1. Logs do Gradle durante o build
2. Configuração dos targets desktop nos build.gradle.kts
3. Implementações actual/expect específicas para desktop