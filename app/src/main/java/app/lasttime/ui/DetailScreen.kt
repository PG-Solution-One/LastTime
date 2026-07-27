package app.lasttime.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.lasttime.R
import app.lasttime.data.repository.CompletionResult
import app.lasttime.domain.Completion
import app.lasttime.domain.TrackedEvent
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    eventId: Long,
    viewModel: LastTimeViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
) {
    val eventFlow = remember(eventId) { viewModel.observeEvent(eventId) }
    val event by eventFlow.collectAsStateWithLifecycle(initialValue = null)
    val today by viewModel.today.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current
    var showDeleteEvent by remember { mutableStateOf(false) }
    var dateAction by remember { mutableStateOf<DateAction?>(null) }
    var completionToDelete by remember { mutableStateOf<Completion?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.title ?: stringResource(R.string.event)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painterResource(R.drawable.ms_arrow_back),
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEdit, enabled = event != null) {
                        Icon(
                            painterResource(R.drawable.ms_edit),
                            contentDescription = stringResource(R.string.edit),
                        )
                    }
                    IconButton(onClick = { showDeleteEvent = true }, enabled = event != null) {
                        Icon(
                            painterResource(R.drawable.ms_delete),
                            contentDescription = stringResource(R.string.delete),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val current = event
        if (current == null) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SummaryCard(current, today)
                }
                item {
                    NotificationPermissionWarning(current)
                }
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = {
                                viewModel.recordCompletion(current.id, today) { result ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            resources.getString(result.messageResource()),
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(painterResource(R.drawable.ms_event_repeat), contentDescription = null)
                            Spacer(Modifier.size(8.dp))
                            Text(stringResource(R.string.mark_today))
                        }
                        OutlinedButton(
                            onClick = { dateAction = DateAction.Add(today) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(painterResource(R.drawable.ms_add), contentDescription = null)
                            Spacer(Modifier.size(8.dp))
                            Text(stringResource(R.string.add_another_date))
                        }
                    }
                }
                if (current.note.isNotBlank()) {
                    item {
                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    stringResource(R.string.note),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    current.note,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
                item {
                    Text(
                        stringResource(R.string.history),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
                    )
                }
                items(current.completions, key = { it.id }) { completion ->
                    CompletionRow(
                        completion = completion,
                        onEdit = { dateAction = DateAction.Edit(completion) },
                        onDelete = { completionToDelete = completion },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }

    if (showDeleteEvent) {
        AlertDialog(
            onDismissRequest = { showDeleteEvent = false },
            title = { Text(stringResource(R.string.delete_event_title)) },
            text = { Text(stringResource(R.string.delete_event_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteEvent = false
                        viewModel.delete(
                            id = eventId,
                            onComplete = onDeleted,
                            onError = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        resources.getString(R.string.error_delete_event),
                                    )
                                }
                            },
                        )
                    },
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteEvent = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    completionToDelete?.let { completion ->
        AlertDialog(
            onDismissRequest = { completionToDelete = null },
            title = { Text(stringResource(R.string.delete_completion_title)) },
            text = { Text(formatDate(completion.date)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        completionToDelete = null
                        viewModel.deleteCompletion(eventId, completion.id) { result ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    resources.getString(result.messageResource()),
                                )
                            }
                        }
                    },
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { completionToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    dateAction?.let { action ->
        LocalDatePickerDialog(
            initialDate = action.initialDate,
            onDismiss = { dateAction = null },
            onDateSelected = { selectedDate ->
                dateAction = null
                if (selectedDate.isAfter(today)) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            resources.getString(R.string.validation_future_date),
                        )
                    }
                } else {
                    when (action) {
                        is DateAction.Add ->
                            viewModel.recordCompletion(eventId, selectedDate) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        resources.getString(it.messageResource()),
                                    )
                                }
                            }
                        is DateAction.Edit ->
                            viewModel.updateCompletion(
                                eventId,
                                action.completion.id,
                                selectedDate,
                            ) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        resources.getString(it.messageResource()),
                                    )
                                }
                            }
                    }
                }
            },
        )
    }
}

@Composable
private fun SummaryCard(
    event: TrackedEvent,
    today: LocalDate,
) {
    val status = event.status(today)
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(event.category.iconResource()),
                    contentDescription = event.category.localizedTitle(),
                    modifier = Modifier.size(30.dp),
                )
                Spacer(Modifier.size(10.dp))
                Text(event.category.localizedTitle(), style = MaterialTheme.typography.labelLarge)
            }
            Text(
                formatElapsed(event.lastDate, today),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                stringResource(R.string.last_time_date, formatDate(event.lastDate)),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                statusText(status, event.nextDate, today),
                color = statusColor(status),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                stringResource(
                    R.string.interval_value,
                    event.interval.unit.localizedInterval(event.interval.amount),
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun NotificationPermissionWarning(event: TrackedEvent) {
    if (event.reminderDaysBefore == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return
    }
    val context = LocalContext.current
    val denied =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) != PackageManager.PERMISSION_GRANTED
    if (!denied) return

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable {
                    context.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                    )
                },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(R.drawable.ms_notifications_off), contentDescription = null)
            Text(
                stringResource(R.string.notifications_disabled),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun CompletionRow(
    completion: Completion,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            formatDate(completion.date),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        IconButton(onClick = onEdit) {
            Icon(
                painterResource(R.drawable.ms_edit),
                contentDescription = stringResource(R.string.edit_date),
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                painterResource(R.drawable.ms_delete),
                contentDescription = stringResource(R.string.delete_history_entry),
            )
        }
    }
}

private sealed interface DateAction {
    val initialDate: LocalDate

    data class Add(
        override val initialDate: LocalDate,
    ) : DateAction

    data class Edit(
        val completion: Completion,
    ) : DateAction {
        override val initialDate: LocalDate = completion.date
    }
}

private fun CompletionResult.messageResource(): Int =
    when (this) {
        CompletionResult.Success -> R.string.saved
        CompletionResult.DuplicateDate -> R.string.duplicate_date
        CompletionResult.LastCompletion -> R.string.last_completion
        CompletionResult.NotFound -> R.string.entry_not_found
        CompletionResult.Failed -> R.string.change_failed
    }
