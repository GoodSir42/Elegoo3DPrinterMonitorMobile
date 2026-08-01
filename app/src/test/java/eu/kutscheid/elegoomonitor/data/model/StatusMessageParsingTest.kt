package eu.kutscheid.elegoomonitor.data.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies that a live `/websocket` status frame decodes into [StatusMessage], including the fields
 * that differ from the discovery payload (array `CurrentStatus`, fractional ticks) and tolerating
 * the many unmodelled fields (fans, lights, temperatures, ...).
 */
class StatusMessageParsingTest {

    // Matches WebSocketDataSource: ignore the fields the app does not model.
    private val json = Json { ignoreUnknownKeys = true }

    private val statusFrame = """
        {"Status":{"CurrentStatus":[1],"TimeLapseStatus":0,"PlatFormType":0,"AmsConnectStatus":1,"TempOfHotbed":80.01604921875421,"TempOfNozzle":255.06518337559671,"TempOfBox":31.20268566853178,"TempTargetHotbed":80,"TempTargetNozzle":255,"TempTargetBox":0,"CurrenCoord":"191.58,212.08,15.04","CurrentFanSpeed":{"ModelFan":32,"AuxiliaryFan":0,"BoxFan":68},"ZOffset":0.00000000000001,"LightStatus":{"SecondLight":1,"RgbLight":[0,0,0]},"PrintInfo":{"Status":13,"CurrentLayer":78,"TotalLayer":820,"CurrentTicks":3617.0119000170016,"TotalTicks":32265,"Filename":"ECC_0.4_Riser CC1 (Back Left) (Down)_AzureFilm PETG Hyper Speed _0.2_8h58m.gcode","TaskId":"fe12dfd1-a906-47ce-ae45-e636290a0e1f","PrintSpeedPct":100,"Progress":10}},"MainboardID":"504e1b140107103d00000c0000000000","TimeStamp":1785590358,"Topic":"sdcp/status/504e1b140107103d00000c0000000000"}
    """.trimIndent()

    @Test
    fun `websocket status frame decodes into StatusMessage`() {
        val message = json.decodeFromString<StatusMessage>(statusFrame)

        assertEquals("504e1b140107103d00000c0000000000", message.mainboardID)
        assertEquals(1785590358L, message.timeStamp)
        assertEquals("sdcp/status/504e1b140107103d00000c0000000000", message.topic)

        val status = message.status
        assertEquals(listOf(1), status.currentStatus)
        assertEquals(80.01604921875421, status.tempOfHotbed, 1e-9)
        assertEquals("191.58,212.08,15.04", status.currentCoord)

        val printInfo = status.printInfo
        assertEquals(13, printInfo.status)
        assertEquals(78L, printInfo.currentLayer)
        assertEquals(820L, printInfo.totalLayer)
        assertEquals(3617.0119000170016, printInfo.currentTicks, 1e-9)
        assertEquals(32265.0, printInfo.totalTicks, 1e-9)
        assertEquals(10, printInfo.progress)
        assertEquals("fe12dfd1-a906-47ce-ae45-e636290a0e1f", printInfo.taskId)
        assertEquals(100, printInfo.printSpeedPct)
    }

    @Test
    fun `status key presence distinguishes status frames from other frames`() {
        // This mirrors the filter WebSocketDataSource applies before decoding.
        val other = """{"Topic":"sdcp/response/abc","Data":{"Ack":0}}"""
        assertFalse("Status" in json.parseToJsonElement(other).jsonObject)
        assertTrue("Status" in json.parseToJsonElement(statusFrame).jsonObject)
    }
}
