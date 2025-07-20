# Commander API

Uma API RESTful moderna construída com Kotlin e Ktor, seguindo princípios de clean architecture.

## Visão Geral do Projeto

Commander API é uma implementação moderna de API usando Kotlin e o framework Ktor. O projeto segue princípios de clean architecture para garantir manutenibilidade, testabilidade e escalabilidade. A aplicação demonstra boas práticas como injeção de dependência, logging estruturado, tratamento de erros e documentação abrangente.

### Arquitetura

O projeto está organizado nas seguintes camadas:

```
src/main/kotlin/kandalabs/commander/
├── application/           # Camada de aplicação (configuração, DI)
│   └── config/           # Configurações da aplicação
├── core/                 # Utilitários e extensões centrais
├── data/                 # Implementações de repositórios e DAOs
├── domain/               # Camada de domínio (lógica de negócio)
│   ├── model/           # Entidades de domínio (User, Table, Order, Item, Bill)
│   ├── repository/      # Interfaces de repositório (UserRepository, TableRepository, OrderRepository, ItemRepository, BillRepository)
│   └── service/         # Serviços de domínio (UserService, TableService, OrderService, ItemService, BillService)
└── presentation/        # Camada de apresentação
    ├── routes/         # Definições de rotas (UserRoutes, TableRoutes, OrderRoutes, ItemRoutes, BillRoutes)
    └── dto/            # Data Transfer Objects (UserRequest, UserResponse, TableRequest, TableResponse, OrderRequest, OrderResponse, ItemRequest, ItemResponse, BillRequest, BillResponse)
```

### Principais Funcionalidades

- Arquitetura limpa e em camadas
- Injeção de dependência com Koin
- API RESTful com códigos de status adequados
- Tratamento abrangente de erros
- Logging estruturado
- Suporte a paginação
- Documentação Swagger/OpenAPI
- Suporte a Docker
- Configuração por ambiente
- Cabeçalhos de segurança

## Instruções de Setup

### Pré-requisitos

- JDK 22 ou superior
- Gradle 8.5 ou superior (ou use o Gradle wrapper incluso)
- Docker (opcional, para conteinerização)

### Setup para Desenvolvimento Local

1. Clone o repositório:
   ```
   git clone https://github.com/your-username/commander-api.git
   cd commander-api
   ```

2. Construa o projeto:
   ```
   ./gradlew build
   ```

3. Rode a aplicação:
   ```
   ./gradlew run
   ```

4. A API estará disponível em `http://localhost:8080`

### Variáveis de Ambiente

A aplicação pode ser configurada usando as seguintes variáveis de ambiente:

| Variável | Descrição | Valor Padrão |
|----------|-----------|--------------|
| `PORT` | Porta do servidor | `8080` |
| `HOST` | Endereço de bind | `0.0.0.0` |
| `DATABASE_URL` | URL JDBC do banco | `jdbc:sqlite:data.db` |
| `DATABASE_DRIVER` | Driver JDBC | `org.sqlite.JDBC` |
| `LOG_LEVEL` | Nível de log | `INFO` |

## Documentação da API

### URL Base

```
http://localhost:8080/api/v1
```

### Endpoints

#### Gerenciamento de Usu��rios

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/users` | Listar usuários (paginado) |
| GET | `/users/{id}` | Buscar usuário por ID |
| POST | `/users` | Criar novo usuário |
| PUT | `/users/{id}` | Atualizar usuário |
| DELETE | `/users/{id}` | Remover usuário |

#### Gerenciamento de Contas (Bills)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/bills` | Listar contas (paginado) |
| GET | `/bills/{id}` | Buscar conta por ID |
| POST | `/bills` | Criar nova conta |
| PUT | `/bills/{id}` | Atualizar conta |
| DELETE | `/bills/{id}` | Remover conta |

#### Gerenciamento de Mesas (Tables)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/tables` | Listar mesas (paginado) |
| GET | `/tables/{id}` | Buscar mesa por ID |
| POST | `/tables` | Criar nova mesa |
| PUT | `/tables/{id}` | Atualizar mesa |
| DELETE | `/tables/{id}` | Remover mesa |

#### Gerenciamento de Pedidos (Orders)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/orders` | Listar pedidos (paginado) |
| GET | `/orders/{id}` | Buscar pedido por ID |
| POST | `/orders` | Criar novo pedido |
| PUT | `/orders/{id}` | Atualizar pedido |
| DELETE | `/orders/{id}` | Remover pedido |

#### Gerenciamento de Itens (Items)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/items` | Listar itens (paginado) |
| GET | `/items/{id}` | Buscar item por ID |
| POST | `/items` | Criar novo item |
| PUT | `/items/{id}` | Atualizar item |
| DELETE | `/items/{id}` | Remover item |

### Swagger UI

A documentação interativa está disponível em:

```
http://localhost:8080/swagger-ui
```

### Exemplos de Requisição

#### Listar Usuários (Paginado)

```http
GET /api/v1/users?page=1&size=10
```

#### Criar Usuário

```http
POST /api/v1/users
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "active": true
}
```

#### Atualizar Usuário

```http
PUT /api/v1/users/1
Content-Type: application/json

{
  "name": "John Smith",
  "email": "john.smith@example.com",
  "active": true
}
```

#### Listar Contas (Bills)

```http
GET /api/v1/bills?page=1&size=10
```

#### Criar Conta (Bill)

```http
POST /api/v1/bills
Content-Type: application/json

{
  "description": "Conta de água",
  "amount": 120.50,
  "dueDate": "2025-06-10"
}
```

### Exemplos de Resposta

#### Resposta de Usuário

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "active": true,
  "createdAt": 1646128400000,
  "createdAtFormatted": "2022-03-01 10:00:00"
}
```

#### Resposta Paginada

```json
{
  "items": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john.doe@example.com",
      "active": true,
      "createdAt": 1646128400000,
      "createdAtFormatted": "2022-03-01 10:00:00"
    }
  ],
  "total": 50,
  "page": 1,
  "size": 10,
  "totalPages": 5,
  "hasNext": true,
  "hasPrevious": false
}
```

#### Resposta de Erro

```json
{
  "status": 400,
  "message": "Formato de e-mail inválido",
  "timestamp": 1646128400000,
  "path": "/api/v1/users"
}
```

## Diretrizes de Desenvolvimento

### Padrões de Código

- Siga as convenções do Kotlin
- Use nomes descritivos para variáveis e funções
- Escreva documentação clara
- Mantenha funções pequenas e focadas
- Escreva testes unitários para toda lógica de negócio

### Estrutura do Projeto

- Mantenha modelos de domínio livres de preocupações de infraestrutura
- Use DTOs para transferência de dados entre camadas
- Separe a camada de apresentação da lógica de negócio
- Use injeção de dependência para todos os componentes

### Testes

Execute os testes com:

```
./gradlew test
```

O projeto utiliza:
- JUnit 5 para testes unitários
- MockK para mocks
- Ktor Test para testes de API

#### Testes de Integração

Para rodar testes de integração, utilize:

```
./gradlew integrationTest
```

> Certifique-se de que o banco de dados de teste está configurado corretamente em `src/test/resources/application-test.conf`.

### Tratamento de Erros

- Use códigos HTTP apropriados
- Forneça mensagens de erro claras
- Logue exceções com níveis adequados
- Retorne formato consistente de resposta de erro

## Uso com Docker

### Build da Imagem Docker

```
docker build -t commander-api .
```

### Rodando com Docker

```
docker run -p 8080:8080 \
  -e PORT=8080 \
  -e DATABASE_URL=jdbc:sqlite:/app/data/data.db \
  -v $(pwd)/data:/app/data \
  -v $(pwd)/logs:/app/logs \
  commander-api
```

### Usando Docker Compose

1. Inicie a aplicação:
   ```
   docker-compose up -d
   ```

2. Veja os logs:
   ```
   docker-compose logs -f api
   ```

3. Pare a aplicação:
   ```
   docker-compose down
   ```

### Configuração Docker

O setup Docker inclui:
- Build multi-stage para imagens menores
- Usuário não-root para segurança
- Health checks
- Volumes para persistência
- Configuração via variáveis de ambiente
- Imagem baseada em Alpine para footprint reduzido

## Informações Adicionais

### Health Checks

A aplicação expõe o endpoint de health check em:

```
GET /health
```

### Logging

Logs são gravados em:
- Console (desenvolvimento)
- `logs/commander-api.log` (com rotação diária)

### Segurança

A aplicação implementa boas práticas de segurança:
- Content Security Policy headers
- XSS Protection headers
- Configuração de CORS
- Validação de entrada
- Usuário não-root no Docker

## Como Contribuir

1. Faça um fork do projeto
2. Crie uma branch para sua feature ou correção:
   ```
   git checkout -b minha-feature
   ```
3. Faça commits claros e objetivos
4. Envie um pull request com a descrição detalhada

Sugestões, issues e pull requests são bem-vindos!
