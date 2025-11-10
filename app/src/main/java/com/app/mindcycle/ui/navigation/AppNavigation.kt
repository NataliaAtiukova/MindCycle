package com.app.mindcycle.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import android.net.Uri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.mindcycle.R
import com.app.mindcycle.data.model.ContraceptionMethod
import com.app.mindcycle.data.model.CycleMode
import com.app.mindcycle.data.model.MoodEntry
import com.app.mindcycle.data.model.ReminderType
import com.app.mindcycle.ui.viewmodel.MainUiState
import com.app.mindcycle.ui.screens.AddEntryScreen
import com.app.mindcycle.ui.screens.CalendarScreen
import com.app.mindcycle.ui.screens.AnalyticsScreen
import com.app.mindcycle.ui.screens.DataPrivacyScreen
import com.app.mindcycle.ui.screens.EntriesListScreen
import com.app.mindcycle.ui.screens.RemindersScreen
import com.app.mindcycle.ui.screens.SettingsScreen
import com.app.mindcycle.ui.screens.SymptomJournalScreen
import com.app.mindcycle.ui.screens.TodayScreen
import androidx.compose.material3.SnackbarDuration
import com.app.mindcycle.ads.YandexAdsManager
import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import org.threeten.bp.LocalDate

object AppDestinations {
    const val TODAY_ROUTE = "today"
    const val CALENDAR_ROUTE = "calendar"
    const val ANALYTICS_ROUTE = "analytics"
    const val SETTINGS_ROUTE = "settings"
    const val SYMPTOMS_ROUTE = "symptoms"
    const val REMINDERS_ROUTE = "reminders"
    const val DATA_ROUTE = "data_privacy"
    const val ADD_ENTRY_ROUTE = "add_entry"
    const val ENTRIES_LIST_ROUTE = "entries_list"
    const val ENTRY_ID_ARG = "entryId"
    const val DATE_ARG = "date"
    const val PERIOD_START_ARG = "periodStart"
    const val PERIOD_DAY_ARG = "periodDay"
    const val SYMPTOM_ARG = "symptom"
}

private data class BottomDestination(val route: String, val icon: ImageVector, @StringRes val labelRes: Int)

private val bottomDestinations = listOf(
    BottomDestination(AppDestinations.TODAY_ROUTE, Icons.Rounded.Today, R.string.today_title),
    BottomDestination(AppDestinations.CALENDAR_ROUTE, Icons.Rounded.CalendarMonth, R.string.calendar),
    BottomDestination(AppDestinations.ANALYTICS_ROUTE, Icons.Rounded.Analytics, R.string.analytics_title),
    BottomDestination(AppDestinations.SETTINGS_ROUTE, Icons.Rounded.Settings, R.string.settings_title)
)

@Composable
fun AppNavigation(
    uiState: MainUiState,
    isLoading: Boolean,
    errorMessage: String?,
    onAddEntry: (MoodEntry) -> Unit,
    onDeleteEntry: (MoodEntry) -> Unit,
    onModeChange: (CycleMode) -> Unit,
    onReminderToggle: (ReminderType, Boolean) -> Unit,
    onReminderTimeChange: (ReminderType, Int, Int) -> Unit,
    onMuteReminderToday: (ReminderType) -> Unit,
    onRecordContraception: (ContraceptionMethod) -> Unit,
    onExportData: ((String) -> Unit) -> Unit,
    onImportData: (String, (Boolean) -> Unit) -> Unit,
    initialDeepLink: String?,
    onDismissError: () -> Unit,
    yandexAdsManager: YandexAdsManager,
    activity: Activity
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    LaunchedEffect(initialDeepLink) {
        val route = initialDeepLink
        if (!route.isNullOrBlank()) {
            navController.navigate(route) {
                popUpTo(AppDestinations.TODAY_ROUTE)
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(AppDestinations.TODAY_ROUTE) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = stringResource(id = destination.labelRes)
                            )
                        },
                        label = { Text(stringResource(id = destination.labelRes)) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(navController = navController, startDestination = AppDestinations.TODAY_ROUTE) {
                composable(AppDestinations.TODAY_ROUTE) {
                TodayScreen(
                    uiState = uiState,
                    onNavigateToAddEntry = { date, isPeriodStart, isPeriodDay, symptom ->
                        val encodedSymptom = symptom?.let { Uri.encode(it) } ?: ""
                        if (isPeriodStart) {
                            yandexAdsManager.loadAndShowInterstitial(activity, activity)
                        }
                        navController.navigate(
                            "${AppDestinations.ADD_ENTRY_ROUTE}?${AppDestinations.ENTRY_ID_ARG}=-1&" +
                                "${AppDestinations.DATE_ARG}=$date&" +
                                "${AppDestinations.PERIOD_START_ARG}=$isPeriodStart&" +
                                "${AppDestinations.PERIOD_DAY_ARG}=$isPeriodDay&" +
                                "${AppDestinations.SYMPTOM_ARG}=$encodedSymptom"
                        )
                    },
                    onNavigateToCalendar = {
                        navController.navigate(AppDestinations.CALENDAR_ROUTE)
                    },
                    onNavigateToEntries = {
                        yandexAdsManager.loadAndShowInterstitial(activity, activity)
                        navController.navigate(AppDestinations.ENTRIES_LIST_ROUTE)
                    },
                    onRecordContraception = { method ->
                        onRecordContraception(method)
                    },
                    onOpenSymptomJournal = {
                        navController.navigate(AppDestinations.SYMPTOMS_ROUTE)
                    },
                    onOpenReminders = {
                        navController.navigate(AppDestinations.REMINDERS_ROUTE)
                    }
                )
            }

            composable(AppDestinations.CALENDAR_ROUTE) {
                CalendarScreen(
                    entries = uiState.entries,
                    cycleForecast = uiState.forecast,
                    onNavigateToAddEntry = { date, isPeriodStart, isPeriodDay ->
                        if (isPeriodStart) {
                            yandexAdsManager.loadAndShowInterstitial(activity, activity)
                        }
                        navController.navigate(
                            "${AppDestinations.ADD_ENTRY_ROUTE}?${AppDestinations.ENTRY_ID_ARG}=-1&" +
                                "${AppDestinations.DATE_ARG}=$date&" +
                                "${AppDestinations.PERIOD_START_ARG}=$isPeriodStart&" +
                                "${AppDestinations.PERIOD_DAY_ARG}=$isPeriodDay"
                        )
                    },
                    onNavigateToEditEntry = { entryId ->
                        yandexAdsManager.loadAndShowInterstitial(activity, activity)
                        navController.navigate("${AppDestinations.ADD_ENTRY_ROUTE}?${AppDestinations.ENTRY_ID_ARG}=$entryId")
                    },
                    onNavigateToEntriesList = {
                        yandexAdsManager.loadAndShowInterstitial(activity, activity)
                        navController.navigate(AppDestinations.ENTRIES_LIST_ROUTE)
                    }
                )
            }

                composable(AppDestinations.ANALYTICS_ROUTE) {
                    AnalyticsScreen(
                        uiState = uiState,
                        onNavigateToEntries = {
                            navController.navigate(AppDestinations.ENTRIES_LIST_ROUTE)
                        }
                    )
                }

                composable(AppDestinations.SETTINGS_ROUTE) {
                    SettingsScreen(
                    uiState = uiState,
                    onSelectMode = onModeChange,
                    onNavigateToReminders = {
                        navController.navigate(AppDestinations.REMINDERS_ROUTE)
                    },
                    onNavigateToDataPrivacy = {
                        navController.navigate(AppDestinations.DATA_ROUTE)
                    },
                    onNavigateToSymptomJournal = {
                        navController.navigate(AppDestinations.SYMPTOMS_ROUTE)
                    }
                )
            }

            composable(
                    route = "${AppDestinations.ADD_ENTRY_ROUTE}?${AppDestinations.ENTRY_ID_ARG}={${AppDestinations.ENTRY_ID_ARG}}&${AppDestinations.DATE_ARG}={${AppDestinations.DATE_ARG}}&${AppDestinations.PERIOD_START_ARG}={${AppDestinations.PERIOD_START_ARG}}&${AppDestinations.PERIOD_DAY_ARG}={${AppDestinations.PERIOD_DAY_ARG}}&${AppDestinations.SYMPTOM_ARG}={${AppDestinations.SYMPTOM_ARG}}",
                    arguments = listOf(
                        navArgument(AppDestinations.ENTRY_ID_ARG) {
                            type = androidx.navigation.NavType.LongType
                            defaultValue = -1L
                        },
                        navArgument(AppDestinations.DATE_ARG) {
                            type = androidx.navigation.NavType.StringType
                            nullable = true
                        },
                        navArgument(AppDestinations.PERIOD_START_ARG) {
                            type = androidx.navigation.NavType.BoolType
                            defaultValue = false
                        },
                        navArgument(AppDestinations.PERIOD_DAY_ARG) {
                            type = androidx.navigation.NavType.BoolType
                            defaultValue = false
                        },
                        navArgument(AppDestinations.SYMPTOM_ARG) {
                            type = androidx.navigation.NavType.StringType
                            defaultValue = ""
                        }
                    )
                ) { backStackEntry ->
                    val entryId = backStackEntry.arguments?.getLong(AppDestinations.ENTRY_ID_ARG)
                    val dateStr = backStackEntry.arguments?.getString(AppDestinations.DATE_ARG)
                    val preselectPeriodStart =
                        backStackEntry.arguments?.getBoolean(AppDestinations.PERIOD_START_ARG)
                            ?: false
                    val preselectPeriodDay =
                        backStackEntry.arguments?.getBoolean(AppDestinations.PERIOD_DAY_ARG)
                            ?: false
                    val quickSymptom =
                        backStackEntry.arguments?.getString(AppDestinations.SYMPTOM_ARG)
                            ?.let { Uri.decode(it) }?.takeIf { it.isNotBlank() }

                    val entryToEdit = if (entryId != null && entryId != -1L) {
                        uiState.entries.find { it.id == entryId }
                    } else {
                        null
                    }

                    val initialDate = dateStr?.let {
                        org.threeten.bp.LocalDate.parse(it).atStartOfDay()
                    }

                    AddEntryScreen(
                        entryToEdit = entryToEdit,
                        initialDate = initialDate,
                        defaultIsPeriodStart = preselectPeriodStart,
                        defaultIsPeriodDay = preselectPeriodDay,
                        quickSymptom = quickSymptom,
                        onSaveEntry = { entry ->
                            onAddEntry(entry)
                            yandexAdsManager.loadAndShowInterstitial(activity, activity)
                            navController.navigateUp()
                        },
                        onNavigateBack = {
                            navController.navigateUp()
                        }
                    )
                }

            composable(AppDestinations.ENTRIES_LIST_ROUTE) {
                EntriesListScreen(
                    entries = uiState.entries,
                    onDeleteEntry = onDeleteEntry,
                    onNavigateToEditEntry = { entryId ->
                        yandexAdsManager.loadAndShowInterstitial(activity, activity)
                        navController.navigate("${AppDestinations.ADD_ENTRY_ROUTE}?${AppDestinations.ENTRY_ID_ARG}=$entryId")
                    },
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }

            composable(AppDestinations.SYMPTOMS_ROUTE) {
                SymptomJournalScreen(
                    entries = uiState.entries,
                    isLoading = uiState.isLoading,
                    onNavigateBack = { navController.navigateUp() },
                    onQuickAdd = { symptom ->
                        val encoded = Uri.encode(symptom)
                        navController.navigate(
                            "${AppDestinations.ADD_ENTRY_ROUTE}?${AppDestinations.ENTRY_ID_ARG}=-1&" +
                                "${AppDestinations.DATE_ARG}=${org.threeten.bp.LocalDate.now()}&" +
                                "${AppDestinations.PERIOD_START_ARG}=false&" +
                                "${AppDestinations.PERIOD_DAY_ARG}=false&" +
                                "${AppDestinations.SYMPTOM_ARG}=$encoded"
                        )
                    }
                )
            }

            composable(AppDestinations.REMINDERS_ROUTE) {
                RemindersScreen(
                    reminderConfigs = uiState.reminderConfigs,
                    onToggleReminder = onReminderToggle,
                    onTimeChange = onReminderTimeChange,
                    onMuteReminderToday = onMuteReminderToday,
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            composable(AppDestinations.DATA_ROUTE) {
                DataPrivacyScreen(
                    onExportData = onExportData,
                    onImportData = onImportData,
                    onNavigateBack = { navController.navigateUp() }
                )
            }
        }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            errorMessage?.let { error ->
                LaunchedEffect(error) {
                    snackbarHostState.showSnackbar(
                        message = error,
                        duration = SnackbarDuration.Short
                    )
                    onDismissError()
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
