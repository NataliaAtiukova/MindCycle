package com.app.mindcycle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.mindcycle.ui.navigation.AppNavigation
import com.app.mindcycle.ui.theme.MindCycleTheme
import com.app.mindcycle.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import com.app.mindcycle.ads.YandexAdsManager
import com.app.mindcycle.reminders.ReminderNotifications
import org.threeten.bp.LocalDate

class MainActivity : ComponentActivity() {

    private val yandexAdsManager by lazy { YandexAdsManager() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show App Open Ad
        yandexAdsManager.loadAndShowAppOpenAd(this, this)

        setContent {
            MindCycleTheme {
                val viewModel: MainViewModel = viewModel()
                var localLoading by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                val coroutineScope = rememberCoroutineScope()
                val uiState by viewModel.uiState.collectAsState()
                val deepLinkRoute = remember { intent?.getStringExtra(ReminderNotifications.EXTRA_DEEP_LINK) }

                // Load initial data
                LaunchedEffect(Unit) {
                    try {
                        localLoading = true
                        viewModel.loadInitialData()
                    } catch (e: Exception) {
                        errorMessage = "Error loading data: ${e.message}"
                    } finally {
                        localLoading = false
                    }
                }

                AppNavigation(
                    uiState = uiState,
                    isLoading = uiState.isLoading || localLoading,
                    errorMessage = errorMessage ?: uiState.errorMessage,
                    onAddEntry = { entry ->
                        localLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            try {
                                viewModel.addEntry(entry)
                            } catch (e: Exception) {
                                errorMessage = "Error saving: ${e.message}"
                            } finally {
                                localLoading = false
                            }
                        }
                    },
                    onDeleteEntry = { entry ->
                        localLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            try {
                                viewModel.deleteEntry(entry)
                            } catch (e: Exception) {
                                errorMessage = "Error deleting: ${e.message}"
                            } finally {
                                localLoading = false
                            }
                        }
                    },
                    onModeChange = { mode ->
                        viewModel.setMode(mode)
                    },
                    onReminderToggle = { type, enabled ->
                        viewModel.updateReminderEnabled(type, enabled)
                    },
                    onReminderTimeChange = { type, hour, minute ->
                        viewModel.updateReminderTime(type, hour, minute)
                    },
                    onMuteReminderToday = { type -> viewModel.muteReminderForToday(type) },
                    onRecordContraception = { method -> viewModel.recordContraception(method, LocalDate.now()) },
                    initialDeepLink = deepLinkRoute,
                    onDismissError = {
                        errorMessage = null
                        viewModel.clearError()
                    },
                    yandexAdsManager = yandexAdsManager,
                    activity = this
                )
            }
        }
    }
}
