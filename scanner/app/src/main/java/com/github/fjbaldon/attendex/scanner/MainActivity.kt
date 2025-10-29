package com.github.fjbaldon.attendex.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.github.fjbaldon.attendex.scanner.ui.navigation.AppNavigation
import com.github.fjbaldon.attendex.scanner.ui.theme.AttendExTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AttendExTheme {
                AppNavigation()
            }
        }
    }
}
