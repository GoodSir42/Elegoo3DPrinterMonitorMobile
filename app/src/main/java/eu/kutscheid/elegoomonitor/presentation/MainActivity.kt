package eu.kutscheid.elegoomonitor.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElegooMonitorTheme {
                // Create a back stack, specifying the key the app should start with
                val backStack = rememberNavBackStack(Destination.InitialLoading)
                val printerViewModel = koinViewModel<PrinterInfoViewModel>()
                val printerList by printerViewModel.printerInfo.collectAsStateWithLifecycle()

                LaunchedEffect(printerList) {
                    if (printerList.isNotEmpty() && backStack.last() == Destination.InitialLoading) {
                        backStack.clear()
                        backStack.add(Destination.PrinterList)
                    }
                }

                SharedTransitionLayout {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
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
                                    Scaffold(
                                        modifier = Modifier.fillMaxSize(),
                                        contentWindowInsets = WindowInsets(),
                                        topBar = {
                                            TopAppBar(
                                                title = { Text(stringResource(R.string.app_name)) },
                                                actions = {
                                                    IconButton(onClick = { backStack.add(Destination.LicenseOverview) }) {
                                                        Icon(
                                                            painterResource(R.drawable.ic_info),
                                                            contentDescription = stringResource(R.string.licenses)
                                                        )
                                                    }
                                                },
                                                modifier = Modifier.sharedBounds(topBar())
                                            )
                                        }
                                    ) { innerPadding ->
                                        PrinterListScreen(
                                            printers = printerList,
                                            onPrinterSelected = { printerId ->
                                                backStack.add(Destination.PrinterDetail(printerId))
                                            },
                                            contentPadding = innerPadding,
                                        )
                                    }
                                }

                                is Destination.PrinterDetail -> NavEntry(key) {
                                    val viewModel =
                                        koinViewModel<PrinterDetailViewModel>(
                                            key = "detail_${key.printerId}",
                                            parameters = {
                                                parametersOf(key.printerId)
                                            })
                                    val printer by viewModel.printer.collectAsStateWithLifecycle()
                                    val videoStreamUrl by viewModel.videoStreamUrl.collectAsStateWithLifecycle()

                                    Scaffold(
                                        modifier = Modifier.fillMaxSize(),
                                        contentWindowInsets = WindowInsets(),
                                        topBar = {
                                        TopAppBar(
                                            title = {
                                                printer?.let {
                                                    Text(
                                                        it.name,
                                                        modifier = Modifier.sharedBounds(
                                                            printerNameKey(it.id)
                                                        )
                                                    )
                                                }
                                            }, navigationIcon = {
                                                IconButton(onClick = { backStack.removeLastOrNull() }) {
                                                    Icon(
                                                        painterResource(R.drawable.ic_arrow_back),
                                                        contentDescription = stringResource(R.string.back)
                                                    )
                                                }
                                            },
                                            modifier = Modifier.sharedBounds(topBar())
                                        )
                                    }) { innerPadding ->

                                        printer?.let {
                                            PrinterDetailScreen(
                                                printer = it,
                                                videoStreamUrl = videoStreamUrl,
                                                contentPadding = innerPadding
                                            )
                                        }
                                    }
                                }

                                is Destination.LicenseOverview -> NavEntry(key) {
                                    Scaffold(
                                        modifier = Modifier.fillMaxSize(),
                                        topBar = {
                                            TopAppBar(
                                                title = { Text(stringResource(R.string.licenses)) },
                                                navigationIcon = {
                                                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                                                        Icon(
                                                            painterResource(R.drawable.ic_arrow_back),
                                                            contentDescription = stringResource(R.string.back)
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    ) { innerPadding ->
                                        LicenseOverviewScreen(
                                            modifier = Modifier.padding(
                                                innerPadding
                                            )
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
    }
}
