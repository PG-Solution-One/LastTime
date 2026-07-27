package app.lasttime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.lasttime.R

private const val HOME = "home"
private const val SETTINGS = "settings"
private const val CREATE = "create"
private const val DETAIL = "detail"
private const val EDIT = "edit"

@Composable
fun LastTimeApp(
    viewModel: LastTimeViewModel,
    notificationTarget: Long?,
    onNotificationTargetConsumed: () -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val showBottomBar = currentRoute == HOME || currentRoute == SETTINGS

    LaunchedEffect(notificationTarget) {
        if (notificationTarget != null) {
            navController.navigate("$DETAIL/$notificationTarget") {
                launchSingleTop = true
            }
            onNotificationTargetConsumed()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter =
                    fadeIn(tween(160)) +
                        expandVertically(
                            animationSpec = tween(220, easing = FastOutSlowInEasing),
                            expandFrom = Alignment.Bottom,
                        ),
                exit =
                    fadeOut(tween(120)) +
                        shrinkVertically(
                            animationSpec = tween(180, easing = FastOutSlowInEasing),
                            shrinkTowards = Alignment.Bottom,
                        ),
            ) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == HOME,
                        onClick = { navController.navigateRoot(HOME) },
                        icon = {
                            Icon(
                                painterResource(R.drawable.ms_event_note),
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(R.string.nav_events)) },
                    )
                    NavigationBarItem(
                        selected = currentRoute == SETTINGS,
                        onClick = { navController.navigateRoot(SETTINGS) },
                        icon = {
                            Icon(
                                painterResource(R.drawable.ms_settings),
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(R.string.nav_settings)) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = HOME,
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(tween(180)) +
                    slideInHorizontally(
                        animationSpec = tween(220, easing = FastOutSlowInEasing),
                        initialOffsetX = { it / 14 },
                    )
            },
            exitTransition = {
                fadeOut(tween(140)) +
                    slideOutHorizontally(
                        animationSpec = tween(200, easing = FastOutSlowInEasing),
                        targetOffsetX = { -it / 18 },
                    )
            },
            popEnterTransition = {
                fadeIn(tween(180)) +
                    slideInHorizontally(
                        animationSpec = tween(220, easing = FastOutSlowInEasing),
                        initialOffsetX = { -it / 14 },
                    )
            },
            popExitTransition = {
                fadeOut(tween(140)) +
                    slideOutHorizontally(
                        animationSpec = tween(200, easing = FastOutSlowInEasing),
                        targetOffsetX = { it / 18 },
                    )
            },
        ) {
            composable(HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    onCreate = { navController.navigate(CREATE) },
                    onOpen = { navController.navigate("$DETAIL/$it") },
                )
            }
            composable(SETTINGS) {
                SettingsScreen(
                    themeMode = themeMode,
                    onThemeModeSelected = viewModel::setThemeMode,
                )
            }
            composable(CREATE) {
                EventFormScreen(
                    viewModel = viewModel,
                    eventId = null,
                    onBack = navController::navigateUp,
                    onSaved = { eventId ->
                        navController.navigate("$DETAIL/$eventId") {
                            popUpTo(CREATE) { inclusive = true }
                        }
                    },
                )
            }
            composable(
                route = "$DETAIL/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.LongType }),
            ) { entry ->
                val eventId = entry.arguments?.getLong("eventId") ?: return@composable
                DetailScreen(
                    eventId = eventId,
                    viewModel = viewModel,
                    onBack = navController::navigateUp,
                    onEdit = { navController.navigate("$EDIT/$eventId") },
                    onDeleted = {
                        navController.navigate(HOME) {
                            popUpTo(HOME) { inclusive = true }
                        }
                    },
                )
            }
            composable(
                route = "$EDIT/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.LongType }),
            ) { entry ->
                val eventId = entry.arguments?.getLong("eventId") ?: return@composable
                EventFormScreen(
                    viewModel = viewModel,
                    eventId = eventId,
                    onBack = navController::navigateUp,
                    onSaved = { navController.navigateUp() },
                )
            }
        }
    }
}

private fun androidx.navigation.NavHostController.navigateRoot(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
