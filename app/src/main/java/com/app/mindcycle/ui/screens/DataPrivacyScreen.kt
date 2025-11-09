package com.app.mindcycle.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.app.mindcycle.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataPrivacyScreen(
    onExportData: ((String) -> Unit) -> Unit,
    onImportData: (String, (Boolean) -> Unit) -> Unit,
    onNavigateBack: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var jsonPayload by remember { mutableStateOf("") }
    var exportPreview by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val exportCopiedMessage = stringResource(R.string.data_export_success)
    val importSuccessMessage = stringResource(R.string.data_import_success)
    val importFailureMessage = stringResource(R.string.data_import_failed)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.settings_export_header)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = stringResource(R.string.data_privacy_intro), style = MaterialTheme.typography.bodyMedium)
            Button(onClick = {
                onExportData { exported ->
                    exportPreview = exported
                    clipboard.setText(AnnotatedString(exported))
                    scope.launch { snackbarHostState.showSnackbar(message = exportCopiedMessage) }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.settings_export_button))
            }
            AnimatedJsonPreview(exportPreview = exportPreview)
            OutlinedTextField(
                value = jsonPayload,
                onValueChange = { jsonPayload = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                label = { Text(text = stringResource(R.string.data_import_hint)) }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { jsonPayload = exportPreview }, modifier = Modifier.weight(1f), enabled = exportPreview.isNotBlank()) {
                    Text(text = stringResource(R.string.data_paste_last))
                }
                Button(onClick = {
                    onImportData(jsonPayload) { success ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = if (success) importSuccessMessage else importFailureMessage
                            )
                        }
                    }
                }, modifier = Modifier.weight(1f), enabled = jsonPayload.isNotBlank()) {
                    Text(text = stringResource(R.string.data_import_btn))
                }
            }
            Text(text = stringResource(R.string.privacy_statement), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AnimatedJsonPreview(exportPreview: String) {
    AnimatedVisibility(visible = exportPreview.isNotBlank()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = stringResource(R.string.data_recent_export), style = MaterialTheme.typography.labelMedium)
            Text(text = exportPreview.take(280) + if (exportPreview.length > 280) "…" else "", style = MaterialTheme.typography.bodySmall)
        }
    }
}
