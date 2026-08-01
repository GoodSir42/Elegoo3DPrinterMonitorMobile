package eu.kutscheid.elegoomonitor.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Live status frame pushed over the printer's `/websocket` endpoint (SDCP `sdcp/status/...` topic).
 *
 * This is a richer, differently-shaped payload than the discovery [PrinterStatus]: [status]
 * is an array, tick counters are fractional, and it carries temperatures/fans/lights. Fields the app
 * does not consume are ignored during decoding (see `WebSocketDataSource`).
 */
@Serializable
data class StatusMessage(
    @SerialName("Status")
    val status: PrinterRuntimeStatus,
    @SerialName("MainboardID")
    val mainboardID: String,
    @SerialName("TimeStamp")
    val timeStamp: Long,
    @SerialName("Topic")
    val topic: String,
)

@Serializable
data class PrinterRuntimeStatus(
    @SerialName("CurrentStatus")
    val currentStatus: List<Int>,
    @SerialName("TempOfHotbed")
    val tempOfHotbed: Double = 0.0,
    @SerialName("TempOfNozzle")
    val tempOfNozzle: Double = 0.0,
    @SerialName("TempOfBox")
    val tempOfBox: Double = 0.0,
    @SerialName("TempTargetHotbed")
    val tempTargetHotbed: Double = 0.0,
    @SerialName("TempTargetNozzle")
    val tempTargetNozzle: Double = 0.0,
    @SerialName("TempTargetBox")
    val tempTargetBox: Double = 0.0,
    // Note: the wire key is misspelled "CurrenCoord" by the firmware.
    @SerialName("CurrenCoord")
    val currentCoord: String = "",
    @SerialName("PrintInfo")
    val printInfo: LivePrintInfo,
)

@Serializable
data class LivePrintInfo(
    @SerialName("Status")
    val status: Int,
    @SerialName("CurrentLayer")
    val currentLayer: Long,
    @SerialName("TotalLayer")
    val totalLayer: Long,
    // Fractional in the websocket feed, unlike the integer ticks in the discovery payload.
    @SerialName("CurrentTicks")
    val currentTicks: Double,
    @SerialName("TotalTicks")
    val totalTicks: Double,
    @SerialName("Filename")
    val filename: String,
    @SerialName("TaskId")
    val taskId: String = "",
    @SerialName("PrintSpeedPct")
    val printSpeedPct: Int = 0,
    @SerialName("Progress")
    val progress: Int = 0,
)
