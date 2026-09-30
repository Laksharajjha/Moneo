package com.moneo.app.presentation.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack

// ...

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appLockEnabled by viewModel.appLockEnabled.collectAsStateWithLifecycle(initialValue = false)

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.importFromCsv(context, uri)
        }
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack, 
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Column(modifier = Modifier.padding(horizontal = 28.dp)) {
            Spacer(Modifier.height(16.dp))
        Spacer(Modifier.height(32.dp))

        SettingsSection(title = "Privacy") {
            SettingsRow(
                title = "App Lock",
                subtitle = "Require biometric / PIN to open",
                trailingContent = {
                    Switch(
                        checked = appLockEnabled,
                        onCheckedChange = { viewModel.toggleAppLock(it) }
                    )
                }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
            SettingsRow(title = "Data stays on device", subtitle = "No cloud sync is enabled")
        }

        Spacer(Modifier.height(32.dp))

        val isNotificationEnabled by viewModel.notificationListenerEnabled.collectAsStateWithLifecycle(initialValue = false)
        val monitoredApps by viewModel.monitoredApps.collectAsStateWithLifecycle(initialValue = emptySet())

        SettingsSection(title = "Intelligence") {
            SettingsRow(
                title = "Notification Access",
                subtitle = "Detect financial activity from notifications",
                trailingContent = {
                    Switch(
                        checked = isNotificationEnabled,
                        onCheckedChange = { 
                            viewModel.toggleNotificationListener(it) 
                            if (it) {
                                // Prompt user to go to Android Settings
                                val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            )
            if (isNotificationEnabled) {
                Text(
                    text = "Moneo can detect financial activity from notifications on your device. Nothing is uploaded to the cloud.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
                
                val apps = listOf(
                    "com.google.android.apps.nbu.paisa.user" to "Google Pay",
                    "com.phonepe.app" to "PhonePe",
                    "com.sbi.upi" to "SBI",
                    "com.hdfcbank.payzapp" to "HDFC",
                    "com.ss.android.ugc.trill" to "Swiggy",
                    "com.application.zomato" to "Zomato",
                    "in.amazon.mShop.android.shopping" to "Amazon",
                    "com.ubercab" to "Uber"
                )
                
                apps.forEach { (packageName, appName) ->
                    SettingsRow(
                        title = appName,
                        subtitle = "Monitor notifications",
                        trailingContent = {
                            Checkbox(
                                checked = monitoredApps.contains(packageName),
                                onCheckedChange = { isChecked ->
                                    viewModel.toggleMonitoredApp(packageName, isChecked)
                                }
                            )
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        SettingsSection(title = "Data") {
            SettingsRow(
                title = "Export data",
                subtitle = "Export transactions to CSV",
                onClick = {
                    coroutineScope.launch {
                        val file = viewModel.exportToCsv(context)
                        if (file != null) {
                            val uri = FileProvider.getUriForFile(
                                context,
                                context.packageName + ".fileprovider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Export Backup"))
                        }
                    }
                }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
            SettingsRow(
                title = "Import data",
                subtitle = "Restore transactions from CSV",
                onClick = {
                    importLauncher.launch("*/*")
                }
            )
        }

        Spacer(Modifier.height(32.dp))

        SettingsSection(title = "About") {
            SettingsRow(title = "Moneo", subtitle = "v1.0.0 \u00B7 Privacy-first finance")
        }
        
        Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(12.dp))
    Column {
        content()
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (trailingContent != null) {
            trailingContent()
        }
    }
}
