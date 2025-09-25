# 🍔 Cardápio Web - Trailer Uçá

Sistema web moderno para exibição de cardápio digital integrado com a CommanderAPI.

## 📋 Características

- ✅ Interface responsiva e moderna
- ✅ Integração com API Kotlin existente
- ✅ Filtro automático de items com valor 0
- ✅ Separação especial para Chopps
- ✅ Categorização inteligente (Espetinhos, Porções, Bebidas, Promoções)
- ✅ Design baseado no cardápio original
- ✅ Preparado para expansão futura (sistema de pedidos)

## 🚀 Como executar

### Pré-requisitos

1. **CommanderAPI rodando**
   ```bash
   cd ../CommanderAPI
   ./gradlew run
   ```
   A API deve estar rodando em `http://192.168.2.218:8081`

2. **Node.js instalado** (versão 16 ou superior)

### Execução rápida com Python

```bash
python3 server.py
```

Escolha uma das opções:
- **1**: Servidor de desenvolvimento (com hot reload)
- **2**: Servidor de produção (build otimizado)
- **3**: Apenas compilar o projeto
- **4**: Sair

### Execução manual

#### Modo desenvolvimento
```bash
npm install
npm run dev
```
Acesse: http://localhost:8003/cardapio

#### Modo produção
```bash
npm install
npm run build
npm run preview
```

## 🏗️ Arquitetura

```
cardapio-web/
├── src/
│   ├── components/      # Componentes React
│   ├── services/        # Integração com API
│   ├── hooks/          # Custom hooks
│   ├── types/          # TypeScript types
│   ├── utils/          # Funções utilitárias
│   └── styles/         # Estilos globais e tema
├── public/             # Arquivos estáticos
├── dist/              # Build de produção
└── server.py          # Servidor Python
```

## 🔧 Configuração

### Variáveis de ambiente (.env)

```env
VITE_API_URL=http://192.168.2.218:8081/api/v1
VITE_BASE_PATH=/cardapio
```

### Configuração do Vite (vite.config.ts)

- Porta: 8003
- Base path: /cardapio
- Proxy para API configurado

## 📱 Categorias do Cardápio

1. **Espetinhos** (SKEWER)
2. **Porções** (SNACK)
3. **Chopps** (Filtrados de DRINK)
4. **Bebidas** (DRINK sem chopps)
5. **Promoções** (PROMOTIONAL)

## 🎨 Tecnologias utilizadas

- **Frontend**: React 19, TypeScript, Vite
- **Estilização**: Styled Components
- **Estado**: React Query
- **API**: Integração com CommanderAPI (Kotlin)
- **Servidor**: Python HTTP Server com proxy

## 🛠️ Desenvolvimento

### Adicionar novo componente

1. Crie o componente em `src/components/NomeComponente/`
2. Adicione estilos em `NomeComponente.styles.ts`
3. Importe e use no `App.tsx`

### Modificar filtros

Os filtros estão em `src/hooks/useItems.ts`:
- Items com valor 0 são filtrados automaticamente
- Chopps são identificados pela função `isChopp()` em `utils/formatters.ts`

## 📝 Notas importantes

- A API deve estar rodando antes de iniciar o frontend
- O servidor Python inclui proxy reverso para a API
- Build de produção é servido em `/cardapio`
- Todas as requisições para `/api` são redirecionadas para a CommanderAPI

## 🐛 Troubleshooting

### API não está respondendo
```bash
# Verifique se a CommanderAPI está rodando
cd ../CommanderAPI
./gradlew run
```

### Localização
📍 **Endereço**: Balneário Bica

### Porta 8003 já está em uso
```bash
# Encontre o processo usando a porta
lsof -i :8003
# Mate o processo
kill -9 <PID>
```

### Erro de CORS
- O servidor Python já inclui headers CORS
- Verifique se está usando o servidor Python e não o Vite direto para produção

## 📄 Licença

Proprietary - Trailer Uçá
