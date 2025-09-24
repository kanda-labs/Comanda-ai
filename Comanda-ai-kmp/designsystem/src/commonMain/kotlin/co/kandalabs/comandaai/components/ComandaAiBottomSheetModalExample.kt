package co.kandalabs.comandaai.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Exemplos de uso do ComandaAiBottomSheetModal com diferentes modos de apresentação
 */

// EXEMPLO 1: Modo Dynamic (padrão) - altura automática baseada no conteúdo
@Composable
fun ExampleDynamicModal(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    ComandaAiBottomSheetModal(
        isVisible = isVisible,
        title = "Modal Dinâmico",
        onDismiss = onDismiss,
        presentationMode = ComandaAiModalPresentationMode.Dynamic, // Padrão, pode omitir
        actions = {
            ComandaAiButton(
                text = "Confirmar",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text("Este modal se adapta automaticamente ao tamanho do conteúdo.")
            Spacer(modifier = Modifier.height(16.dp))
            Text("Ideal para formulários pequenos ou confirmações simples.")
        }
    }
}

// EXEMPLO 2: Modo Small (30%) - modal pequeno para confirmações
@Composable
fun ExampleSmallModal(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    ComandaAiBottomSheetModal(
        isVisible = isVisible,
        title = "Confirmação",
        onDismiss = onDismiss,
        presentationMode = ComandaAiModalPresentationMode.Small,
        actions = {
            ComandaAiButton(
                text = "Confirmar",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text("Tem certeza que deseja continuar?")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Esta ação não pode ser desfeita.",
                style = ComandaAiTheme.typography.bodySmall,
                color = ComandaAiTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// EXEMPLO 3: Modo Mid (50%) - modal médio para formulários
@Composable
fun ExampleMidModal(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    ComandaAiBottomSheetModal(
        isVisible = isVisible,
        title = "Cadastro Rápido",
        onDismiss = onDismiss,
        presentationMode = ComandaAiModalPresentationMode.Mid,
        actions = {
            ComandaAiButton(
                text = "Salvar",
                onClick = onDismiss,
                isEnabled = name.isNotBlank() && email.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome completo") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                "Preencha os campos acima para continuar.",
                style = ComandaAiTheme.typography.bodySmall,
                color = ComandaAiTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// EXEMPLO 4: Modo Full (100%) - modal de tela cheia para conteúdo extenso
@Composable
fun ExampleFullModal(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    ComandaAiBottomSheetModal(
        isVisible = isVisible,
        title = "Relatório Detalhado",
        onDismiss = onDismiss,
        presentationMode = ComandaAiModalPresentationMode.Full,
        dismissOnDrag = false, // Opcional: desabilitar drag para modais full
        actions = {
            ComandaAiButton(
                text = "Exportar PDF",
                onClick = { /* ação de exportar */ },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fechar")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Resumo Financeiro",
                style = ComandaAiTheme.typography.titleMedium
            )
            
            // Simulação de conteúdo extenso
            repeat(10) { index ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ComandaAiTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Item ${index + 1}",
                            style = ComandaAiTheme.typography.titleSmall
                        )
                        Text(
                            "Descrição detalhada do item ${index + 1} com informações relevantes.",
                            style = ComandaAiTheme.typography.bodyMedium,
                            color = ComandaAiTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * RESUMO DOS MODOS DE APRESENTAÇÃO:
 * 
 * Dynamic (Padrão): 
 * - Altura automática baseada no conteúdo
 * - Ideal para: confirmações simples, formulários pequenos
 * - Botões aparecem logo após o conteúdo
 * 
 * Small (30%):
 * - Ocupa 30% da altura da tela
 * - Ideal para: confirmações, alerts, mensagens curtas
 * - Botões sempre fixados no bottom da tela
 * 
 * Mid (50%):
 * - Ocupa 50% da altura da tela  
 * - Ideal para: formulários médios, seleções com scroll
 * - Botões sempre fixados no bottom da tela
 * 
 * Full (100%):
 * - Ocupa altura máxima da tela
 * - Ideal para: listas extensas, relatórios, conteúdo complexo
 * - Botões sempre fixados no bottom da tela
 * - Sugestão: desabilitar dismissOnDrag para evitar fechamento acidental
 */