package app.lasttime

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.lasttime.domain.ThemeMode
import app.lasttime.ui.LastTimeApp
import app.lasttime.ui.LastTimeViewModel
import app.lasttime.ui.theme.LastTimeTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : AppCompatActivity() {
    private val targetEventId = MutableStateFlow<Long?>(null)
    private val viewModel: LastTimeViewModel by viewModels {
        val container = (application as LastTimeApplication).container
        LastTimeViewModel.Factory(
            container.repository,
            container.reminderScheduler,
            container.userPreferencesRepository,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        readTarget(intent)
        setContent {
            val notificationTarget by targetEventId.collectAsStateWithLifecycle()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle(ThemeMode.SYSTEM)
            LastTimeTheme(themeMode = themeMode) {
                LastTimeApp(
                    viewModel = viewModel,
                    notificationTarget = notificationTarget,
                    onNotificationTargetConsumed = { targetEventId.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        readTarget(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDate()
    }

    private fun readTarget(intent: Intent?) {
        val id = intent?.getLongExtra(EXTRA_EVENT_ID, -1) ?: -1
        if (id > 0) targetEventId.value = id
    }

    companion object {
        const val EXTRA_EVENT_ID = "open_event_id"
    }
}
