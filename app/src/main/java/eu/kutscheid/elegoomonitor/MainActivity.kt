package eu.kutscheid.elegoomonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.kutscheid.elegoomonitor.data.UdpDataSource
import eu.kutscheid.elegoomonitor.ui.theme.ElegooMonitorTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val dataSource = UdpDataSource()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElegooMonitorTheme {
                val dataItem by dataSource.startBroadcast().collectAsStateWithLifecycle(null)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Greeting(
                            name = "Android",
                        )
                        Button(onClick = {
                        }) {
                            Text("Start!")
                        }
                        dataItem?.let {
                            Text("Got data Item from printer")
                            Text("id: " + it.id)
                            Text("status: " + it.data.status.currentStatus)
                            Text("name: " + it.data.attributes.name)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ElegooMonitorTheme {
        Greeting("Android")
    }
}