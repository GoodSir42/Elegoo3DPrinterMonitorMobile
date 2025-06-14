package eu.kutscheid.elegoomonitor.data

import co.touchlab.kermit.Logger
import eu.kutscheid.elegoomonitor.data.model.PrinterItem
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.ByteReadPacket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.isActive
import kotlinx.coroutines.isActive
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

class UdpDataSource {

    private val logger = Logger.withTag("NETWORK")

    @OptIn(ExperimentalCoroutinesApi::class)
    fun startBroadcast(): Flow<PrinterItem> = flow {
        val selectorManager = ActorSelectorManager(Dispatchers.IO)
        selectorManager.use {
            val socket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", 3000)) {
                broadcast = true
            }
            socket.use {
                while (currentCoroutineContext().isActive) {
                    it.send(Datagram(ByteReadPacket("M99999".toByteArray(Charsets.UTF_8)), InetSocketAddress("255.255.255.255", 3000)))
                    logger.d { "sending discovery datagram" }
                    logger.d { "checking for response" }
                    if (!it.incoming.isEmpty) {
                        it.incoming.receiveCatching()
                            .onSuccess { data ->
                                val messageString =
                                    data.packet.readByteArray().toString(Charsets.UTF_8)
                                logger.d {
                                    "Got message $messageString" }
                                try {
                                    emit(Json.decodeFromString(messageString))
                                } catch (ex: Throwable) {
                                    logger.d(ex) { "can't parse message \"$messageString\", ignoring" }
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

    suspend fun receiveData(): ByteArray {
        val selectorManager = ActorSelectorManager(Dispatchers.IO)
        return selectorManager.use {
            // Use aDatagramSocket for UDP
            val socket = aSocket(selectorManager).udp().connect(InetSocketAddress("0.0.0.0", 3000))
            socket.use {
                // Receive a datagram
                val datagram = it.receive()
                // Read the packet content as ByteArray
                datagram.packet.readByteArray()
            }
        }
    }
}