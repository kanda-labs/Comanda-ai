# 🏗️ Configuração de Ambientes Separados

Este sistema agora suporta ambientes separados de **PRODUÇÃO** e **DEBUG/DESENVOLVIMENTO**, garantindo que seus dados de produção permaneçam seguros durante o desenvolvimento.

## 📦 Bancos de Dados

| Ambiente | Porta API | Database | Descrição |
|----------|-----------|----------|-----------|
| **PRODUÇÃO** | 8081 | `data.db` | ⚠️ Dados reais em produção |
| **DEBUG** | 8082 | `data-debug.db` | 🔧 Ambiente de desenvolvimento (pode copiar de prod) |

### 📋 **Cópia de Dados de Produção**
Para testar com dados reais sem afetar produção:
```bash
# Copiar banco de produção para debug
./copy-prod-to-debug.sh

# OU iniciar debug e perguntar se quer copiar
./start-debug-with-prod-data.sh
```

## 🚀 Como Usar

### Para Produção (App Release)
```bash
# 1. Inicie o servidor de produção
./start-production.sh

# 2. Build e instale o app de produção
cd Comanda-ai-kmp
./gradlew :app:assembleRelease
```

### Para Desenvolvimento (App Debug)
```bash
# OPÇÃO 1: Debug normal (banco vazio/existente)
./start-debug.sh

# OPÇÃO 2: Debug com dados de produção (recomendado)
./start-debug-with-prod-data.sh

# 2. Build e instale o app de debug
cd Comanda-ai-kmp
./gradlew :app:assembleDebug
```

## 🔄 Configuração Automática

### Backend (CommanderAPI)
- **Produção**: `ENVIRONMENT=production` → `data.db`
- **Debug**: `ENVIRONMENT=debug` → `data-debug.db`

### Mobile App (Comanda-ai-kmp)
- **Release Build**: Conecta em `192.168.0.161:8081` (produção)
- **Debug Build**: Conecta em `192.168.0.161:8082` (debug)

## 📱 Identificação dos Apps

- **App de Produção**: `co.kandalabs.comandaai`
- **App de Debug**: `co.kandalabs.comandaai.debug` (sufixo `.debug`)

Você pode ter ambos os apps instalados simultaneamente no mesmo dispositivo!

## ⚠️ Importante

1. **NUNCA** use o app de debug em produção
2. **SEMPRE** use o app de release para clientes
3. Os bancos são **completamente separados**
4. Cada ambiente roda em uma **porta diferente**

## 🔧 Desenvolvimento Seguro

Agora você pode:
✅ Desenvolver sem medo de afetar a produção  
✅ Testar novas funcionalidades no ambiente debug  
✅ Manter dados de produção intactos  
✅ Rodar ambos os ambientes simultaneamente  

## 🛠️ **Gerenciamento de Bancos**

```bash
# Gerenciar bancos interativamente
./manage-databases.sh

# Copiar produção para debug manualmente  
./copy-prod-to-debug.sh
```

## 🏃‍♂️ Quick Start

```bash
# Terminal 1 - Servidor de produção (para clientes)
./start-production.sh

# Terminal 2 - Servidor de debug com dados de prod (para desenvolvimento)  
./start-debug-with-prod-data.sh

# Agora você tem ambos rodando simultaneamente!
```

## 💾 **Backups Automáticos**

- ✅ **Backup automático** - Antes de copiar, o sistema faz backup do banco debug atual
- ✅ **Histórico de backups** - Mantém histórico de alterações
- ✅ **Restauração fácil** - Use `./manage-databases.sh` para restaurar qualquer backup
- ✅ **Limpeza automática** - Remove backups antigos automaticamente