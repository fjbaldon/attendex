package com.github.fjbaldon.attendex.capture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.github.fjbaldon.attendex.capture.data.sync.SyncManager // <--- Import this
import com.github.fjbaldon.attendex.capture.ui.navigation.AppNavigation
import com.github.fjbaldon.attendex.capture.ui.theme.AttendExTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject // <--- Import this

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var syncManager: SyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        syncManager.startPeriodicSync()

        enableEdgeToEdge()
        setContent {
            AttendExTheme {
                AppNavigation()
            }
        }
    }
}
