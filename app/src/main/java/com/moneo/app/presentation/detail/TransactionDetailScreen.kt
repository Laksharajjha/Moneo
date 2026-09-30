package com.moneo.app.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moneo.app.presentation.components.AmountText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: String,
    onBack: () -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val transaction by viewModel.transaction.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.delete()
                        onBack()
                    }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        val tx = transaction
        if (tx == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.padding(padding).padding(24.dp).fillMaxWidth()) {
                Text(
                    text = tx.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                AmountText(amountInPaise = tx.amountInPaise, type = tx.type, style = MaterialTheme.typography.displayMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(text = "Category: ${tx.category.displayName}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Type: ${tx.type?.name ?: "UNKNOWN"}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                if (tx.account != null) {
                    Text(text = "Account: ${tx.account}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(text = "Date: ${tx.date}", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
