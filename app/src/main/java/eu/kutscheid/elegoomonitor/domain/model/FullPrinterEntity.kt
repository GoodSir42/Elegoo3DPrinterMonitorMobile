package eu.kutscheid.elegoomonitor.domain.model

import eu.kutscheid.elegoomonitor.data.model.PrinterItem
import eu.kutscheid.elegoomonitor.data.model.StatusMessage
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalTime::class)
data class FullPrinterEntity(
    val lastSeen: Instant,
    val id: String,
    val mainboardID: String,
    val ipAddress: String,
    val name: String,
    val type: PrinterType,
    val status: PrinterStatus,
    val resolution: String,
    val firmwareVersion: String,
    val totalLayers: Long,
    val currentLayer: Long,
    val elapsedTime: Duration,
    val estimatedTime: Duration,
    val progress: Double = if (totalLayers > 0) currentLayer.toDouble() / totalLayers.toDouble() else 0.0,
) {
    constructor(dataModel: PrinterItem) : this(
        lastSeen = Clock.System.now(),
        id = dataModel.id,
        mainboardID = dataModel.data.attributes.mainboardID,
        ipAddress = dataModel.data.attributes.mainboardIP,
        name = dataModel.data.attributes.name,
        type = when (dataModel.data.attributes.machineName) {
            "ELEGOO Mars 4 Ultra" -> PrinterType.MARS_4
            "ELEGOO Saturn 3 Ultra" -> PrinterType.SATURN_3
            "Centauri Carbon" -> PrinterType.CENTAURI_CARBON
            else -> PrinterType.UNKNOWN
        },
        status = PrinterStatus.fromStatusCode(dataModel.data.status?.printInfo?.status),
        resolution = dataModel.data.attributes.resolution ?: "",
        firmwareVersion = dataModel.data.attributes.firmwareVersion,
        currentLayer = dataModel.data.status?.printInfo?.currentLayer ?: 0L,
        totalLayers = dataModel.data.status?.printInfo?.totalLayer ?: 0L,
        elapsedTime = (dataModel.data.status?.printInfo?.currentTicks ?: 0L).milliseconds,
        estimatedTime = ((dataModel.data.status?.printInfo?.totalTicks
            ?: 0L) - (dataModel.data.status?.printInfo?.currentTicks ?: 0L)).milliseconds,
    )

    /**
     * Overlays a live websocket [StatusMessage] onto the discovery data so a socket-reporting
     * printer ends up with the same fields populated as one that returned its status over UDP.
     */
    fun withLiveStatus(message: StatusMessage): FullPrinterEntity {
        val printInfo = message.status.printInfo
        return copy(
            lastSeen = Clock.System.now(),
            status = PrinterStatus.fromStatusCode(message.status.currentStatus.first()),
            currentLayer = printInfo.currentLayer,
            totalLayers = printInfo.totalLayer,
            // Websocket ticks are fractional seconds (unlike the millisecond ticks in the UDP payload).
            elapsedTime = printInfo.currentTicks.seconds,
            estimatedTime = (printInfo.totalTicks - printInfo.currentTicks).seconds,
            progress = if (printInfo.totalLayer > 0) {
                printInfo.currentLayer.toDouble() / printInfo.totalLayer.toDouble()
            } else {
                0.0
            },
        )
    }
}
