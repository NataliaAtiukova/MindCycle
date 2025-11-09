package com.app.mindcycle.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.mindcycle.R
import com.app.mindcycle.data.model.ReminderConfig
import com.app.mindcycle.data.model.ReminderType
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.threeten.bp.LocalTime

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    reminderConfigs: Map<ReminderType, ReminderConfig>,
    onToggleReminder: (ReminderType, Boolean) -> Unit,
    onTimeChange: (ReminderType, Int, Int) -> Unit,
    onMuteReminderToday: (ReminderType) -> Unit,
    onNavigateBack: () -> Unit
) {
    val permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.reminders_preview_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (permissionState != null && !permissionState.status.isGranted) {
                PermissionBanner(onRequest = { permissionState.launchPermissionRequest() })
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ReminderType.values()) { type ->
                    val config = reminderConfigs[type] ?: ReminderConfig()
                    ReminderToggleCard(
                        type = type,
                        config = config,
                        onToggle = { enabled -> onToggleReminder(type, enabled) },
                        onEditTime = { hour, minute -> onTimeChange(type, hour, minute) },
                        onMuteToday = { onMuteReminderToday(type) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionBanner(onRequest: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.reminder_permission_title), fontWeight = FontWeight.SemiBold)
            Text(text = stringResource(R.string.reminder_permission_body), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRequest) {
                Text(text = stringResource(R.string.request_permission))
            }
        }
    }
}

@Composable
private fun ReminderToggleCard(
    type: ReminderType,
    config: ReminderConfig,
    onToggle: (Boolean) -> Unit,
    onEditTime: (Int, Int) -> Unit,
    onMuteToday: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(id = type.titleRes), style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (config.enabled) stringResource(R.string.reminder_time_label, config.hour, config.minute)
                        else stringResource(R.string.reminder_disabled),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = config.enabled, onCheckedChange = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle(it)
                })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = {
                    val time = LocalTime.of(config.hour, config.minute)
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> onEditTime(hour, minute) },
                        time.hour,
                        time.minute,
                        true
                    ).show()
                }) {
                    Text(text = stringResource(R.string.reminder_edit_time))
                }
                TextButton(onClick = onMuteToday, enabled = config.enabled) {
                    Text(text = stringResource(R.string.reminder_mute_today))
                }
            }
        }
    }
}
