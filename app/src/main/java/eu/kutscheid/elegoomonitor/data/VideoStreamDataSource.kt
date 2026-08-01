package eu.kutscheid.elegoomonitor.data

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.head
import io.ktor.client.statement.HttpResponse

class VideoStreamDataSource {

    private val logger = Logger.withTag("NETWORK")

    // expectSuccess = false so a non-2xx status is inspected rather than thrown.
    private val client = HttpClient(CIO) { expectSuccess = false }

    /**
     * Probes an MJPEG endpoint with a HEAD request. Returns true when it responds without a transport
     * error and without a 4xx/5xx status, i.e. the printer is offering a stream we can display.
     */
    suspend fun isStreamAvailable(url: String): Boolean = try {
        val response: HttpResponse = client.head(url)
        (response.status.value < 400).also {
            logger.d { "video HEAD $url -> ${response.status} (available=$it)" }
        }
    } catch (ex: Throwable) {
        logger.d(ex) { "video HEAD $url failed, treating as unavailable" }
        false
    }
}
