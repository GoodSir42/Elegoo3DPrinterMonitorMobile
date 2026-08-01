package eu.kutscheid.elegoomonitor.domain.model

import eu.kutscheid.elegoomonitor.data.model.PrinterItem
import eu.kutscheid.elegoomonitor.data.model.StatusMessage
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Verifies that a Centauri discovered over UDP (which reports no status there) ends up with its
 * live websocket status woven in, so it looks like any other printer whose status came from UDP.
 */
@OptIn(ExperimentalTime::class)
class LiveStatusOverlayTest {

    private val json = Json { ignoreUnknownKeys = true }

    // Same mainboard id in both payloads: the Centauri's discovery reply and its websocket frame.
    private val centauriDiscovery = """
        {
            "Id": "979d4C788A4a78bC777A870F1A02867A",
            "Data": {
                "Name": "Centauri Carbon",
                "MachineName": "Centauri Carbon",
                "BrandName": "ELEGOO",
                "MainboardIP": "192.168.6.175",
                "MainboardID": "504e1b140107103d00000c0000000000",
                "ProtocolVersion": "V3.0.0",
                "FirmwareVersion": "V1.4.49"
            }
        }
    """.trimIndent()

    private val statusFrame = """
        {"Status":{"CurrentStatus":[1],"TempOfHotbed":80.0,"TempOfNozzle":255.0,"PrintInfo":{"Status":1,"CurrentLayer":78,"TotalLayer":820,"CurrentTicks":3617.0119000170016,"TotalTicks":32265,"Filename":"model.gcode","TaskId":"fe12dfd1","PrintSpeedPct":100,"Progress":10}},"MainboardID":"504e1b140107103d00000c0000000000","TimeStamp":1785590358,"Topic":"sdcp/status/504e1b140107103d00000c0000000000"}
    """.trimIndent()

    @Test
    fun `websocket status overlays onto the discovered printer`() {
        val discovered = FullPrinterEntity(json.decodeFromString<PrinterItem>(centauriDiscovery))
        // Discovery alone gives no live print data.
        assertEquals(PrinterStatus.Unknown, discovered.status)
        assertEquals(0L, discovered.totalLayers)

        val message = json.decodeFromString<StatusMessage>(statusFrame)
        // The overlay is keyed by mainboard id, which lines up across the two payloads.
        assertEquals(discovered.mainboardID, message.mainboardID)

        val live = discovered.withLiveStatus(message)

        // Live print fields are now populated, identity/attributes preserved from discovery.
        assertEquals("Centauri Carbon", live.name)
        assertEquals(PrinterType.CENTAURI_CARBON, live.type)
        assertEquals(PrinterStatus.Printing, live.status) // PrintInfo.Status 1 -> Preparing
        assertEquals(78L, live.currentLayer)
        assertEquals(820L, live.totalLayers)
        assertEquals(3617.0119000170016.seconds, live.elapsedTime)
        assertEquals((32265.0 - 3617.0119000170016).seconds, live.estimatedTime)
        assertEquals(78.0 / 820.0, live.progress, 1e-9)
    }
}
