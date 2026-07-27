package app.lasttime.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.lasttime.R
import app.lasttime.domain.EventCategory
import app.lasttime.domain.EventDraft
import app.lasttime.domain.RepeatInterval
import app.lasttime.domain.RepeatUnit
import app.lasttime.domain.TrackedEvent
import app.lasttime.domain.validateDraft
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun EventFormScreen(
    viewModel: LastTimeViewModel,
    eventId: Long?,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    val existing = eventId?.let { id -> events.firstOrNull { it.id == id } }
    if (eventId != null && existing == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    EventFormContent(
        key = existing?.id ?: 0,
        existing = existing,
        viewModel = viewModel,
        onBack = onBack,
        onSaved = onSaved,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventFormContent(
    key: Long,
    existing: TrackedEvent?,
    viewModel: LastTimeViewModel,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
) {
    var title by rememberSaveable(key) { mutableStateOf(existing?.title.orEmpty()) }
    var categoryName by rememberSaveable(key) {
        mutableStateOf((existing?.category ?: EventCategory.HOME).name)
    }
    var selectedDateText by rememberSaveable(key) {
        mutableStateOf((existing?.lastDate ?: LocalDate.now()).toString())
    }
    var amountText by rememberSaveable(key) {
        mutableStateOf((existing?.interval?.amount ?: 1).toString())
    }
    var unitName by rememberSaveable(key) {
        mutableStateOf((existing?.interval?.unit ?: RepeatUnit.MONTHS).name)
    }
    var reminderEnabled by rememberSaveable(key) {
        mutableStateOf(existing?.reminderDaysBefore != null)
    }
    var reminderText by rememberSaveable(key) {
        mutableStateOf((existing?.reminderDaysBefore ?: 7).toString())
    }
    var note by rememberSaveable(key) { mutableStateOf(existing?.note.orEmpty()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val resources = LocalResources.current
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) {}
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val selectedDate = LocalDate.parse(selectedDateText)
    val today = LocalDate.now()

    fun save() {
        val amount = amountText.toIntOrNull()
        val reminder = if (reminderEnabled) reminderText.toIntOrNull() else null
        if (amount == null || amount !in 1..999) {
            scope.launch {
                snackbarHostState.showSnackbar(resources.getString(R.string.error_interval_amount))
            }
            return
        }
        if (reminderEnabled && reminder == null) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    resources.getString(R.string.error_reminder_days_required),
                )
            }
            return
        }
        val draft =
            EventDraft(
                title = title,
                category = EventCategory.valueOf(categoryName),
                initialDate = selectedDate,
                interval = RepeatInterval(amount, RepeatUnit.valueOf(unitName)),
                reminderDaysBefore = reminder,
                note = note,
            )
        val error = validateDraft(draft, today)
        if (error != null) {
            scope.launch {
                snackbarHostState.showSnackbar(resources.getString(error.messageResource()))
            }
            return
        }
        saving = true
        val onError: () -> Unit = {
            saving = false
            val message =
                if (existing == null) {
                    R.string.error_create_event
                } else {
                    R.string.error_update_event
                }
            scope.launch { snackbarHostState.showSnackbar(resources.getString(message)) }
        }
        if (existing == null) {
            viewModel.create(
                draft = draft,
                onComplete = onSaved,
                onError = onError,
            )
        } else {
            viewModel.update(
                id = existing.id,
                draft = draft,
                onComplete = { onSaved(existing.id) },
                onError = onError,
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (existing == null) R.string.new_event else R.string.edit_event,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painterResource(R.drawable.ms_arrow_back),
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = ::save, enabled = !saving) {
                        if (saving) {
                            CircularProgressIndicator(modifier = Modifier.padding(10.dp))
                        } else {
                            Icon(
                                painterResource(R.drawable.ms_save),
                                contentDescription = stringResource(R.string.save),
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp)),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FormSection(
                title = stringResource(R.string.form_basics),
                icon = R.drawable.ms_event_note,
                accentContainer = MaterialTheme.colorScheme.secondaryContainer,
                accentContent = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                TextField(
                    value = title,
                    onValueChange = { if (it.length <= 80) title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.title)) },
                    supportingText = { Text("${title.length}/80") },
                    leadingIcon = {
                        Icon(painterResource(R.drawable.ms_edit), contentDescription = null)
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    colors = modernTextFieldColors(),
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                ) {
                    TextField(
                        value = EventCategory.valueOf(categoryName).localizedTitle(),
                        onValueChange = {},
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        readOnly = true,
                        label = { Text(stringResource(R.string.category)) },
                        leadingIcon = {
                            Icon(
                                painterResource(
                                    EventCategory.valueOf(categoryName).iconResource(),
                                ),
                                contentDescription = null,
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded)
                        },
                        shape = MaterialTheme.shapes.large,
                        colors = modernTextFieldColors(),
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                    ) {
                        EventCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.localizedTitle()) },
                                leadingIcon = {
                                    Icon(
                                        painterResource(category.iconResource()),
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    categoryName = category.name
                                    categoryExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            FormSection(
                title = stringResource(R.string.form_schedule),
                icon = R.drawable.ms_event_repeat,
                accentContainer = MaterialTheme.colorScheme.primaryContainer,
                accentContent = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Card(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        ),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painterResource(R.drawable.ms_calendar_month),
                                    contentDescription = null,
                                )
                            }
                        }
                        Column {
                            Text(
                                stringResource(R.string.last_time),
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Text(
                                formatDate(selectedDate),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }

                Text(
                    stringResource(R.string.repeat_after),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter(Char::isDigit).take(3) },
                        modifier = Modifier.weight(0.38f),
                        label = { Text(stringResource(R.string.amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                        colors = modernTextFieldColors(),
                    )
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = it },
                        modifier = Modifier.weight(0.62f),
                    ) {
                        TextField(
                            value = RepeatUnit.valueOf(unitName).localizedTitle(),
                            onValueChange = {},
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            readOnly = true,
                            label = { Text(stringResource(R.string.unit)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(unitExpanded)
                            },
                            shape = MaterialTheme.shapes.large,
                            colors = modernTextFieldColors(),
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false },
                        ) {
                            RepeatUnit.entries.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit.localizedTitle()) },
                                    onClick = {
                                        unitName = unit.name
                                        unitExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.reminder),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                stringResource(R.string.reminder_description),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { enabled ->
                                reminderEnabled = enabled
                                if (
                                    enabled &&
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS,
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                        )
                    }
                }
                AnimatedVisibility(visible = reminderEnabled) {
                    TextField(
                        value = reminderText,
                        onValueChange = { reminderText = it.filter(Char::isDigit).take(3) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.reminder_days)) },
                        supportingText = { Text(stringResource(R.string.reminder_time_hint)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                        colors = modernTextFieldColors(),
                    )
                }
            }

            FormSection(
                title = stringResource(R.string.note),
                icon = R.drawable.ms_edit,
                accentContainer = MaterialTheme.colorScheme.tertiaryContainer,
                accentContent = MaterialTheme.colorScheme.onTertiaryContainer,
            ) {
                TextField(
                    value = note,
                    onValueChange = { if (it.length <= 500) note = it },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(148.dp),
                    placeholder = { Text(stringResource(R.string.note_hint)) },
                    supportingText = { Text("${note.length}/500") },
                    shape = MaterialTheme.shapes.large,
                    colors = modernTextFieldColors(),
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showDatePicker) {
        LocalDatePickerDialog(
            initialDate = selectedDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { selectedDateText = it.toString() },
        )
    }
}

@Composable
private fun FormSection(
    title: String,
    @androidx.annotation.DrawableRes icon: Int,
    accentContainer: Color,
    accentContent: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = MaterialTheme.shapes.large,
                    color = accentContainer,
                    contentColor = accentContent,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            content()
        }
    }
}

@Composable
private fun modernTextFieldColors(): TextFieldColors =
    TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = MaterialTheme.colorScheme.error,
    )
