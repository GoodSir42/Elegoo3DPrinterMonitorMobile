package eu.kutscheid.elegoomonitor.data

import co.touchlab.kermit.Logger
import eu.kutscheid.elegoomonitor.data.model.PrinterItem
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

private const val BROADCAST_MESSAGE = "M99999"

private const val PORT = 3000

class UdpDataSource {

    private val logger = Logger.withTag("NETWORK")

    @OptIn(ExperimentalCoroutinesApi::class)
    fun startBroadcast(): Flow<PrinterItem> = flow {
        val selectorManager = ActorSelectorManager(Dispatchers.IO)
        selectorManager.use {
            val socket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", PORT)) {
                broadcast = true
            }
            socket.use {
                while (currentCoroutineContext().isActive) {
                    logger.d { "sending discovery datagram" }
                    it.send(
                        Datagram(
                            Buffer().apply {
                                writeString(BROADCAST_MESSAGE, Charsets.UTF_8)
                            },
                            InetSocketAddress(
                                "255.255.255.255",
                                PORT
                            )
                        )
                    )
                    logger.d { "checking for response" }
                    if (!it.incoming.isEmpty) {
                        it.incoming.receiveCatching()
                            .onSuccess { data ->
                                val messageString = data.packet.readString()
                                logger.d {
                                    "Got message $messageString"
                                }
                                if (messageString != BROADCAST_MESSAGE) {
                                    try {
                                        emit(Json.decodeFromString(messageString))
                                    } catch (ex: Throwable) {
                                        logger.d(ex) { "can't parse message \"$messageString\", ignoring" }
                                    }
                                }
                            }
                            .onFailure { error ->
                                logger.e(error) { "failed to get message" }
                            }
                    }
                    delay(1.seconds)
                }
            }
        }
    }
}