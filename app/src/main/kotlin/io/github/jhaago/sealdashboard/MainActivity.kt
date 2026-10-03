package io.github.jhaago.sealdashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Launchable build foundation; vehicle simulation and dashboard follow next. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                background = Color(0xFF0B1015), onBackground = Color(0xFFF1F2EE),
                primary = Color(0xFF72DED8),
            )) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier.safeDrawingPadding().padding(40.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text("SIMULATED · FOUNDATION BUILD", color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                        Text("Seal Dashboard", fontSize = 48.sp)
                        Text("Australian 2024 Dynamic · single-motor RWD", fontSize = 20.sp)
                        Text("Build verification shell. Live simulation and dashboard screens are not implemented yet.", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
