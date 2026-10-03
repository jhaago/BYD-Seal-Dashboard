package io.github.jhaago.sealdashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import io.github.jhaago.sealdashboard.ui.*

class MainActivity : ComponentActivity() {
    lateinit var dashboardViewModel: DashboardViewModel
        private set
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as DashboardApplication).container
        dashboardViewModel = ViewModelProvider(this, DashboardViewModel.Factory(container.vehicle, container.clock))[DashboardViewModel::class.java]
        setContent { DashboardShell(dashboardViewModel, container.simulation) }
    }
}
