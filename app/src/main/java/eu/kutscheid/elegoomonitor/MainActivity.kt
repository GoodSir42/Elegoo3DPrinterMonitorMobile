package eu.kutscheid.elegoomonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import eu.kutscheid.elegoomonitor.presentation.Destination
import eu.kutscheid.elegoomonitor.presentation.PrinterInfoViewModel
import eu.kutscheid.elegoomonitor.presentation.PrinterListScreen
import eu.kutscheid.elegoomonitor.ui.theme.ElegooMonitorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElegooMonitorTheme {
                // Create a back stack, specifying the key the app should start with
                val backStack = rememberNavBackStack<Destination>(Destination.InitialLoading)
                val printerViewModel = viewModel<PrinterInfoViewModel>()
                val dataItem by printerViewModel.printerInfo.collectAsStateWithLifecycle()

                LaunchedEffect(dataItem) {
                    if (dataItem.isNotEmpty()) {
                        backStack.clear()
                        backStack.add(Destination.PrinterList(dataItem.map { it.data }))
                    }
                }

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() }
                ) { key ->
                    when (key) {
                        is Destination.InitialLoading -> NavEntry(key) {
                            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .padding(innerPadding)
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        "Waiting for data",
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(
                                            Alignment.CenterHorizontally
                                        )
                                    )
                                }
                            }
                        }

                        is Destination.PrinterList -> NavEntry(key) {
                            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                                PrinterListScreen(
                                    printers = key.printers,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }

                        else -> NavEntry(key) {
                            Text("Unknown destination")
                        }
                    }
                }
            }
        }
    }
}
