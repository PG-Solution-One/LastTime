package app.lasttime.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import app.lasttime.R
import app.lasttime.domain.ThemeMode

private enum class AppLanguage(
    @param:StringRes val label: Int,
    val languageTag: String?,
) {
    SYSTEM(R.string.language_system_short, null),
    RUSSIAN(R.string.language_russian, "ru"),
    ENGLISH(R.string.language_english, "en"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
) {
    val currentLanguage = currentAppLanguage()

    Scaffold(
        topBar = {
            LargeTopAppBar(title = { Text(stringResource(R.string.settings_title)) })
        },
    ) { padding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_appearance),
                    subtitle = stringResource(R.string.settings_appearance_description),
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            ThemeChoiceCard(
                                mode = mode,
                                selected = themeMode == mode,
                                onClick = { onThemeModeSelected(mode) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            item {
                SettingsSection(
                    title = stringResource(R.string.settings_language),
                    subtitle = stringResource(R.string.settings_language_description),
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        AppLanguage.entries.forEachIndexed { index, language ->
                            SegmentedButton(
                                selected = currentLanguage == language,
                                onClick = { setAppLanguage(language) },
                                shape =
                                    SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = AppLanguage.entries.size,
                                    ),
                                label = {
                                    Text(
                                        text = stringResource(language.label),
                                        maxLines = 1,
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        content()
    }
}

@Composable
private fun ThemeChoiceCard(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visual = mode.visual()
    Card(
        onClick = onClick,
        modifier = modifier,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (selected) {
                        visual.containerColor()
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
            ),
        border =
            if (selected) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else {
                null
            },
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = MaterialTheme.shapes.large,
                color = visual.containerColor(),
                contentColor = visual.contentColor(),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(visual.icon),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Text(
                text = stringResource(visual.title),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

private data class ThemeVisual(
    @param:StringRes val title: Int,
    @param:DrawableRes val icon: Int,
    val containerColor: @Composable () -> Color,
    val contentColor: @Composable () -> Color,
)

private fun ThemeMode.visual(): ThemeVisual =
    when (this) {
        ThemeMode.SYSTEM ->
            ThemeVisual(
                title = R.string.theme_system_short,
                icon = R.drawable.ms_brightness_auto,
                containerColor = { MaterialTheme.colorScheme.tertiaryContainer },
                contentColor = { MaterialTheme.colorScheme.onTertiaryContainer },
            )

        ThemeMode.LIGHT ->
            ThemeVisual(
                title = R.string.theme_light,
                icon = R.drawable.ms_light_mode,
                containerColor = { MaterialTheme.colorScheme.secondaryContainer },
                contentColor = { MaterialTheme.colorScheme.onSecondaryContainer },
            )

        ThemeMode.DARK ->
            ThemeVisual(
                title = R.string.theme_dark,
                icon = R.drawable.ms_dark_mode,
                containerColor = { MaterialTheme.colorScheme.primaryContainer },
                contentColor = { MaterialTheme.colorScheme.onPrimaryContainer },
            )
    }

private fun currentAppLanguage(): AppLanguage {
    val firstLanguageTag =
        AppCompatDelegate
            .getApplicationLocales()
            .toLanguageTags()
            .substringBefore(',')
            .substringBefore('-')
    return AppLanguage.entries.firstOrNull { it.languageTag == firstLanguageTag }
        ?: AppLanguage.SYSTEM
}

private fun setAppLanguage(language: AppLanguage) {
    val locales =
        language.languageTag?.let(LocaleListCompat::forLanguageTags)
            ?: LocaleListCompat.getEmptyLocaleList()
    AppCompatDelegate.setApplicationLocales(locales)
}
