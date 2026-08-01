package eu.kutscheid.elegoomonitor.data

import co.touchlab.kermit.Logger
import eu.kutscheid.elegoomonitor.data.model.StatusMessage
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class WebSocketDataSource {

    private val logger = Logger.withTag("NETWORK")

    // The status frame carries many fields the app does not model (fans, lights, ...); ignore them.
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Opens `ws://<printerIp>/websocket` and emits every incoming frame that carries a `Status`
     * object, parsed into a [StatusMessage]. Non-status frames and unparseable payloads are logged
     * and skipped. The flow completes when the socket closes; collect with a retry to reconnect.
     */
    fun connect(printerIp: String): Flow<StatusMessage> = flow {
        val client = HttpClient(CIO) { install(WebSockets) }
        client.use { client ->
            client.webSocket(urlString = "ws://$printerIp:3030/websocket") {
                logger.d { "websocket connected to $printerIp" }
                for (frame in incoming) {
                    val text = (frame as? Frame.Text)?.readText() ?: continue
                    logger.d { "Got websocket message $text" }
                    parseStatus(text)?.let { emit(it) }
                }
            }
        }
    }

    private fun parseStatus(text: String): StatusMessage? = try {
        // Only frames with a top-level "Status" key are status updates; ignore the rest.
        if ("Status" in json.parseToJsonElement(text).jsonObject) {
            json.decodeFromString<StatusMessage>(text)
        } else {
            null
        }
    } catch (ex: Throwable) {
        logger.d(ex) { "can't parse websocket message \"$text\", ignoring" }
        null
    }
}
