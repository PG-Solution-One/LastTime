package app.lasttime.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.lasttime.R
import app.lasttime.domain.EventStatus
import app.lasttime.domain.TrackedEvent
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: LastTimeViewModel,
    onCreate: () -> Unit,
    onOpen: (Long) -> Unit,
) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    val today by viewModel.today.collectAsStateWithLifecycle()
    val sortedEvents =
        events.sortedWith(
            compareBy({ it.status(today).ordinal }, { it.nextDate }, { it.title }),
        )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.home_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            stringResource(R.string.home_subtitle),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate) {
                Icon(
                    painterResource(R.drawable.ms_add),
                    contentDescription = stringResource(R.string.add_event),
                )
            }
        },
    ) { padding ->
        if (sortedEvents.isEmpty()) {
            EmptyState(
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(sortedEvents, key = { it.id }) { event ->
                    EventCard(event = event, today = today, onClick = { onOpen(event.id) })
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painterResource(R.drawable.ms_history),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.empty_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.empty_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EventCard(
    event: TrackedEvent,
    today: LocalDate,
    onClick: () -> Unit,
) {
    val status = event.status(today)
    val accent = statusColor(status)
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                painter = painterResource(event.category.iconResource()),
                contentDescription = event.category.localizedTitle(),
                tint = accent,
                modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(
                        R.string.last_time_elapsed,
                        formatElapsed(event.lastDate, today),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    formatDate(event.lastDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    statusText(status, event.nextDate, today),
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                )
            }
        }
    }
}

@Composable
fun statusColor(status: EventStatus): Color =
    when (status) {
        EventStatus.OVERDUE -> MaterialTheme.colorScheme.error
        EventStatus.TODAY -> Color(0xFFB65D00)
        EventStatus.SOON -> Color(0xFF806000)
        EventStatus.UPCOMING -> MaterialTheme.colorScheme.primary
    }
