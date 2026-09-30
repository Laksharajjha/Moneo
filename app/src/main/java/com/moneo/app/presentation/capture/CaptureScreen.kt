package com.moneo.app.presentation.capture

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moneo.app.ai.model.ParseResult
import com.moneo.app.ai.parser.AmountParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    onDismiss: () -> Unit,
    viewModel: CaptureViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var isKeyboardMode by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        } else {
            isKeyboardMode = true
        }
    }

    // Auto-dismiss after save
    LaunchedEffect(state.step) {
        if (state.step == CaptureStep.SAVED) {
            kotlinx.coroutines.delay(400)
            onDismiss()
        }
    }

    LaunchedEffect(isKeyboardMode) {
        if (isKeyboardMode) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(28.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.reset(); onDismiss() }) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "Capture",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            if (state.step == CaptureStep.IDLE || state.step == CaptureStep.ERROR) {
                IconButton(onClick = { isKeyboardMode = !isKeyboardMode }) {
                    Icon(
                        if (isKeyboardMode) Icons.Outlined.Mic else Icons.Outlined.Keyboard,
                        contentDescription = "Toggle Input Mode",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Spacer(Modifier.width(48.dp))
            }
        }

        Spacer(Modifier.height(36.dp))

        when (state.step) {
            CaptureStep.IDLE, CaptureStep.ERROR -> {
                if (isKeyboardMode) {
                    CaptureInputSection(
                        inputText = state.inputText,
                        onTextChange = viewModel::onTextInput,
                        onSubmit = { viewModel.processText() },
                        errorMessage = state.errorMessage,
                        focusRequester = focusRequester,
                        keyboardController = keyboardController
                    )
                } else {
                    CaptureVoiceSection(
                        errorMessage = state.errorMessage,
                        onMicTap = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                viewModel.startListening()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    )
                }
            }
            CaptureStep.PROCESSING -> {
                CaptureProcessingSection()
            }
            CaptureStep.CONFIRMING -> {
                state.parseResult?.let { result ->
                    CaptureConfirmSection(
                        parseResult = result,
                        onConfirm = { viewModel.confirmAndSave() },
                        onEdit = {
                            viewModel.reset()
                            isKeyboardMode = true
                            viewModel.onTextInput(result.rawText)
                        },
                        onCancel = { viewModel.reset() }
                    )
                }
            }
            CaptureStep.SAVED -> {
                CaptureSavedSection(parseResult = state.parseResult)
            }
            CaptureStep.LISTENING -> {
                CaptureListeningSection(
                    partialText = state.inputText,
                    onStopTap = { viewModel.stopListening() }
                )
            }
        }
    }
}

@Composable
private fun CaptureVoiceSection(
    errorMessage: String?,
    onMicTap: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface)
                .clickable { onMicTap() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Mic,
                contentDescription = "Tap to speak",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.surface
            )
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            text = "Tap to speak",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Try: \"Spent 280 at Starbucks\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (errorMessage != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CaptureInputSection(
    inputText: String,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    errorMessage: String?,
    focusRequester: FocusRequester,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?
) {
    Column {
        Text(
            text = "What did you spend?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Try: \"Spent 280 at Starbucks\"",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = inputText,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = {
                Text(
                    "e.g. Starbucks 280",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            isError = errorMessage != null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
                onSubmit()
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = MaterialTheme.shapes.small
        )

        if (errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = inputText.isNotBlank(),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Text("Parse", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun CaptureProcessingSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.onSurface,
            strokeWidth = 2.dp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Understanding...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CaptureConfirmSection(
    parseResult: ParseResult,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit
) {
    Column {
        Text(
            text = "Does this look right?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(28.dp))

        parseResult.transactions.forEach { tx ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        text = AmountParser.formatPaise(tx.amountInPaise),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    if (tx.merchant != null) {
                        Text(
                            text = tx.merchant,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = tx.category.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = tx.date.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small
            ) { Text("Cancel") }
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small
            ) { Text("Edit") }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) { Text("Confirm") }
        }
    }
}

@Composable
private fun CaptureSavedSection(parseResult: ParseResult?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        parseResult?.transactions?.firstOrNull()?.let { tx ->
            Text(
                text = AmountParser.formatPaise(tx.amountInPaise),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            if (tx.merchant != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = tx.merchant,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = tx.category.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Saved",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CaptureListeningSection(
    partialText: String,
    onStopTap: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                .clickable { onStopTap() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Mic,
                contentDescription = "Stop listening",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.height(32.dp))
        Text(
            if (partialText.isNotBlank()) partialText else "Listening...",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
