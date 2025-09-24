# Plano de Desenvolvimento - Sistema de Gerenciamento de Categorias

## 📋 Análise da Situação Atual
- **ItemCategory** existe como enum fixo (SKEWER, DRINK, SNACK, PROMOTIONAL)
- **CategoriesManagementScreen** é apenas um placeholder
- Não há endpoints de API para gerenciar categorias
- Não há modelo de dados para Category como entidade

## 🏗️ Arquitetura Proposta

### 1. **Modelo de Dados**
```kotlin
// features/domain/src/commonMain/kotlin/co/kandalabs/comandaai/domain/Category.kt
data class Category(
    val id: Int? = null,
    val name: String,
    val description: String? = null,
    val color: String? = null, // Hex color para badges
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)
```

### 2. **API & Repository**
- **CommanderApi**: Adicionar endpoints CRUD para categorias
- **CategoriesRepository**: Interface e implementação
- **CategoriesRepositoryImpl**: Implementação usando CommanderApi

### 3. **ViewModels e Estados**
- **CategoriesManagementViewModel**: Gerencia lista de categorias
- **CategoriesManagementScreenState**: Estado da tela de listagem
- **CategoryFormViewModel**: Gerencia formulário de categoria
- **CategoryFormState**: Estado do formulário

### 4. **Telas (UI)**
- **CategoriesManagementScreen**: Lista com filtros e ações
- **CategoryFormScreen**: Formulário para criar/editar categoria

## 📝 Implementação Detalhada

### **Fase 1: Foundation (Domain & API)**
1. **Criar modelo Category** no módulo domain
2. **Atualizar CommanderApi** com endpoints de categorias:
   - `GET /api/v1/categories` - Listar categorias
   - `GET /api/v1/categories/{id}` - Buscar por ID
   - `POST /api/v1/categories` - Criar categoria
   - `PUT /api/v1/categories/{id}` - Atualizar categoria
   - `DELETE /api/v1/categories/{id}` - Deletar categoria
3. **Criar CategoriesRepository** (interface + implementação)

### **Fase 2: Business Logic (ViewModels)**
4. **CategoriesManagementViewModel**:
   - Estado: lista de categorias, loading, error
   - Ações: loadCategories(), deleteCategory(), toggleActiveStatus()

5. **CategoryFormViewModel**:
   - Estado: campos do formulário, validação, loading
   - Ações: updateField(), validateForm(), saveCategory(), loadCategory()

6. **Estados**:
   - **CategoriesManagementScreenState**: categories, isLoading, error, showDeleteModal
   - **CategoryFormState**: name, description, color, isActive, errors, isSaving

### **Fase 3: User Interface**
7. **CategoriesManagementScreen** - Seguindo padrão de ItemsManagementScreen:
   - **Header**: "Gerenciar Categorias" + botão voltar
   - **Lista**: LazyColumn com ComandaAiListItem para cada categoria
   - **Item da Lista**:
     - Nome da categoria + descrição
     - Badge colorida (preview da cor)
     - Status ativo/inativo
   - **FAB/Button**: "Criar nova categoria" (ComandaAiButton)
   - **Ações**: Click no item → editar, swipe/menu → deletar

8. **CategoryFormScreen** - Seguindo padrão de ItemFormScreen:
   - **Header**: "Nova Categoria" ou "Editar Categoria" + botão voltar
   - **Formulário**:
     - Campo nome (obrigatório)
     - Campo descrição (opcional)
     - Seletor de cor (palette de cores pré-definidas)
     - Toggle ativo/inativo
   - **Botões Bottom**: "Salvar" + "Deletar" (se editando)
   - **Modal de Confirmação**: ComandaAiBottomSheetModal para delete

### **Fase 4: Integration & Validation**
9. **Atualizar AttendanceModule** - Registrar novos ViewModels e Repository
10. **Validação de Formulário**:
    - Nome obrigatório (min 2 chars)
    - Nome único (não duplicar)
    - Cor válida (hex format)
11. **Error Handling**: Estados de erro para falhas de API
12. **Loading States**: Indicadores durante operações async

### **Fase 5: UI Consistency & Polish**
13. **Design System**: Usar todos os componentes ComandaAi
    - ComandaAiButton, ComandaAiListItem, ComandaAiBottomSheetModal
    - ComandaAiTheme, ComandaAiColors, ComandaAiSpacing
14. **Color Picker**: Lista de cores pré-definidas como badges clicáveis
15. **Responsive Layout**: Botões sempre no bottom, scroll no conteúdo
16. **Empty State**: Mensagem quando não há categorias
17. **Success Feedback**: Feedback visual ao salvar/deletar

## 🔄 Fluxos de Usuário

### **Listagem de Categorias**
1. Usuario acessa CategoriesManagementScreen
2. ViewModel carrega categorias via repository
3. Lista exibe categorias com badges coloridas
4. Click em categoria → navega para edição
5. Click em "Criar nova categoria" → navega para criação

### **Criar Categoria**
1. Usuario clica "Criar nova categoria"
2. Navega para CategoryFormScreen (modo criação)
3. Preenche nome, descrição, escolhe cor
4. Click "Salvar" → valida + salva via API
5. Success → volta para lista + refresh

### **Editar Categoria**
1. Usuario clica em categoria na lista
2. Navega para CategoryFormScreen (modo edição)
3. Form pre-populado com dados existentes
4. Usuário modifica campos necessários
5. Click "Salvar" → atualiza via API
6. Click "Deletar" → modal confirmação → delete via API

## 🎨 Especificações Visuais

- **Lista**: Mesmo layout que ItemsManagementScreen
- **Item**: Nome + descrição + badge colorida preview
- **Formulário**: Mesmo layout que ItemFormScreen
- **Color Picker**: Row de badges coloridas clicáveis
- **Cores sugeridas**: Azul, Verde, Laranja, Roxo, Rosa, Vermelho
- **Modal Delete**: "Tem certeza que deseja deletar esta categoria?"

## ⚠️ Considerações Técnicas

- **Migração**: ItemCategory enum → Category entities (gradual)
- **Backwards Compatibility**: Manter enum durante transição
- **Validation**: Não permitir deletar categoria se tem items associados
- **Performance**: Lazy loading para listas grandes
- **Caching**: Cache local de categorias para performance

## 🚀 Próximos Passos

1. **Backend**: Implementar endpoints de categorias na API
2. **Domain**: Criar modelo Category
3. **Repository**: Implementar CategoriesRepository
4. **ViewModels**: Criar ViewModels para gerenciamento
5. **UI**: Implementar telas seguindo design system
6. **Testing**: Testes unitários e de integração
7. **Migration**: Estratégia de migração do enum para entities

---
*Documento criado em: 2025-09-24*
*Status: 📋 Planejado - Aguardando implementação*