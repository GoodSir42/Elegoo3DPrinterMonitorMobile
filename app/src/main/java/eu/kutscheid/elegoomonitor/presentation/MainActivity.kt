package eu.kutscheid.elegoomonitor.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import eu.kutscheid.elegoomonitor.R
import eu.kutscheid.elegoomonitor.ui.theme.ElegooMonitorTheme
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElegooMonitorTheme {
                // Create a back stack, specifying the key the app should start with
                val backStack = rememberNavBackStack<Destination>(Destination.InitialLoading)
                val printerViewModel = koinViewModel<PrinterInfoViewModel>()
                val dataItem by printerViewModel.printerInfo.collectAsStateWithLifecycle()

                LaunchedEffect(dataItem) {
                    if (dataItem.isNotEmpty()) {
                        backStack.clear()
                        backStack.add(Destination.PrinterList(dataItem))
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
                                        stringResource(R.string.main_scanning_and_waiting),
                                        style = MaterialTheme.typography.headlineLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    LoadingIndicator(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .align(
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