package com.app.mindcycle.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.mindcycle.R
import com.app.mindcycle.data.model.CyclePrediction
import com.app.mindcycle.data.model.MoodEntry
import com.app.mindcycle.ui.screens.AddEntryScreen
import com.app.mindcycle.ui.screens.CalendarScreen
import com.app.mindcycle.ui.screens.EntriesListScreen
import androidx.compose.material3.SnackbarDuration
import com.app.mindcycle.ads.YandexAdsManager
import android.app.Activity

object AppDestinations {
    const val CALENDAR_ROUTE = "calendar"
    const val ADD_ENTRY_ROUTE = "add_entry"
    const val ENTRIES_LIST_ROUTE = "entries_list"
    const val ENTRY_ID_ARG = "entryId"
    const val DATE_ARG = "date"
}

@Composable
fun AppNavigation(
    entries: List<MoodEntry>,
    cyclePrediction: CyclePrediction?,
    isLoading: Boolean,
    errorMessage: String?,
    onAddEntry: (MoodEntry) -> Unit,
    onDeleteEntry: (MoodEntry) -> Unit,
    onDismissError: () -> Unit,
    yandexAdsManager: YandexAdsManager,
    activity: Activity
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        NavHost(navController = navController, startDestination = AppDestinations.CALENDAR_ROUTE) {
            composable(AppDestinations.CALENDAR_ROUTE) {
                CalendarScreen(
                    entries = entries,
                    cyclePrediction = cyclePrediction,
                    onNavigateToAddEntry = { date ->
                        navController.navigate("${AppDestinations.ADD_ENTRY_ROUTE}?${AppDestinations.ENTRY_ID_ARG}=-1&${AppDestinations.DATE_ARG}=$date")
                    },
                    onNavigateToEditEntry = { entryId ->
                        navController.navigate("${AppDestinations.ADD_ENTRY_ROUTE}?${AppDestinations.ENTRY_ID_ARG}=$entryId")
                    },
                    onNavigateToEntriesList = {
                        yandexAdsManager.loadAndShowInterstitial(activity, activity)
                        navController.navigate(AppDestinations.ENTRIES_LIST_ROUTE)
                    }
                )
            }

            composable(
                route = "${AppDestinations.ADD_ENTRY_ROUTE}?${AppDestinations.ENTRY_ID_ARG}={${AppDestinations.ENTRY_ID_ARG}}&${AppDestinations.DATE_ARG}={${AppDestinations.DATE_ARG}}",
                arguments = listOf(
                    navArgument(AppDestinations.ENTRY_ID_ARG) {
                        type = androidx.navigation.NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument(AppDestinations.DATE_ARG) {
                        type = androidx.navigation.NavType.StringType
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getLong(AppDestinations.ENTRY_ID_ARG)
                val dateStr = backStackEntry.arguments?.getString(AppDestinations.DATE_ARG)

                val entryToEdit = if (entryId != null && entryId != -1L) {
                    entries.find { it.id == entryId }
                } else {
                    null
                }

                val initialDate = dateStr?.let {
                    org.threeten.bp.LocalDate.parse(it).atStartOfDay()
                }

                AddEntryScreen(
                    entryToEdit = entryToEdit,
                    initialDate = initialDate,
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
                    entries = entries,
                    onDeleteEntry = onDeleteEntry,
                    onNavigateToEditEntry = { entryId ->
                        navController.navigate("${AppDestinations.ADD_ENTRY_ROUTE}?${AppDestinations.ENTRY_ID_ARG}=$entryId")
                    },
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }
        }

        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Error handling
        errorMessage?.let { error ->
            LaunchedEffect(error) {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Short
                )
                onDismissError()
            }
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
} 