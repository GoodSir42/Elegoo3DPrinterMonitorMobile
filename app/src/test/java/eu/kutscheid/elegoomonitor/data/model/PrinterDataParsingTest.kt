package eu.kutscheid.elegoomonitor.data.model

import eu.kutscheid.elegoomonitor.domain.model.FullPrinterEntity
import eu.kutscheid.elegoomonitor.domain.model.PrinterStatus
import eu.kutscheid.elegoomonitor.domain.model.PrinterType
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Verifies that the two Elegoo response shapes — the flat (Centauri) format and the nested
 * (Mars/Saturn) format — both decode into a single [PrinterItem] via [PrinterDataSerializer].
 */
class PrinterDataParsingTest {

    // Matches production: UdpDataSource decodes with the default Json instance.
    private val json = Json

    private val centauriPayload = """
        {
            "Id": "979d4C788A4a78bC777A870F1A02867A",
            "Data":
            {
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

    private val marsPayload = """
        {
            "Id": "f25273b12b094c5a8b9513a30ca60049",
            "Data":
            {
                "Attributes":
                {
                    "Name": "Mars 4 Ultra",
                    "MachineName": "ELEGOO Mars 4 Ultra",
                    "ProtocolVersion": "V1.0.0",
                    "FirmwareVersion": "V1.3.0",
                    "Resolution": "8520x4320",
                    "MainboardIP": "192.168.6.192",
                    "MainboardID": "48e083567eb20100",
                    "SDCPStatus": 0,
                    "LocalSDCPAddress": "",
                    "SDCPAddress": "",
                    "Capabilities":
                    [
                        "FILE_TRANSFER",
                        "PRINT_CONTROL"
                    ]
                },
                "Status":
                {
                    "CurrentStatus": 0,
                    "PreviousStatus": 0,
                    "PrintInfo":
                    {
                        "Status": 0,
                        "CurrentLayer": 0,
                        "TotalLayer": 0,
                        "CurrentTicks": 0,
                        "TotalTicks": 0,
                        "ErrorNumber": 0,
                        "Filename": ""
                    },
                    "FileTransferInfo":
                    {
                        "Status": 0,
                        "DownloadOffset": 0,
                        "CheckOffset": 0,
                        "FileTotalSize": 0,
                        "Filename": ""
                    }
                }
            }
        }
    """.trimIndent()

    @Test
    fun `flat Centauri format decodes with attributes wrapped and no status`() {
        val item = json.decodeFromString<PrinterItem>(centauriPayload)

        assertEquals("979d4C788A4a78bC777A870F1A02867A", item.id)
        val attributes = item.data.attributes
        assertEquals("Centauri Carbon", attributes.name)
        assertEquals("Centauri Carbon", attributes.machineName)
        assertEquals("ELEGOO", attributes.brandName)
        assertEquals("192.168.6.175", attributes.mainboardIP)
        assertEquals("V1.4.49", attributes.firmwareVersion)
        // Fields only present in the nested format are absent here.
        assertNull(attributes.resolution)
        assertNull(attributes.sDCPStatus)
        assertEquals(emptyList<String>(), attributes.capabilities)
        // No Status block in the flat format.
        assertNull(item.data.status)
    }

    @Test
    fun `nested Mars format decodes with attributes and status`() {
        val item = json.decodeFromString<PrinterItem>(marsPayload)

        assertEquals("f25273b12b094c5a8b9513a30ca60049", item.id)
        val attributes = item.data.attributes
        assertEquals("Mars 4 Ultra", attributes.name)
        assertEquals("ELEGOO Mars 4 Ultra", attributes.machineName)
        assertEquals("8520x4320", attributes.resolution)
        assertEquals(listOf("FILE_TRANSFER", "PRINT_CONTROL"), attributes.capabilities)
        // Not sent by the nested format.
        assertNull(attributes.brandName)

        val status = requireNotNull(item.data.status)
        assertEquals(0, status.currentStatus)
        assertEquals("", status.printInfo.filename)
    }

    @Test
    fun `both formats map onto the FullPrinterEntity domain model`() {
        val centauri = FullPrinterEntity(json.decodeFromString<PrinterItem>(centauriPayload))
        assertEquals(PrinterType.CENTAURI_CARBON, centauri.type)
        assertEquals(PrinterStatus.Unknown, centauri.status) // no Status block -> Unknown
        assertEquals("", centauri.resolution)
        assertEquals(0.0, centauri.progress, 0.0) // no active print, no divide-by-zero

        val mars = FullPrinterEntity(json.decodeFromString<PrinterItem>(marsPayload))
        assertEquals(PrinterType.MARS_4, mars.type)
        assertEquals(PrinterStatus.Ready, mars.status)
        assertEquals("8520x4320", mars.resolution)
    }
}