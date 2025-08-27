package eu.kutscheid.elegoomonitor.domain.model

import eu.kutscheid.elegoomonitor.data.model.PrinterItem
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalTime::class)
data class FullPrinterEntity(
    val lastSeen: Instant,
    val id: String,
    val name: String,
    val type: PrinterType,
    val status: PrinterStatus,
    val resolution: String,
    val firmwareVersion: String,
    val totalLayers: Long,
    val currentLayer: Long,
    val elapsedTime: Duration,
    val estimatedTime: Duration,
    val progress: Double = currentLayer.toDouble() / totalLayers.toDouble(),
) {
    constructor(dataModel: PrinterItem) : this(
        lastSeen = Clock.System.now(),
        id = dataModel.id,
        name = dataModel.data.attributes.name,
        type = when (dataModel.data.attributes.machineName) {
            "ELEGOO Mars 4 Ultra" -> PrinterType.MARS_4
            "ELEGOO Saturn 3 Ultra" -> PrinterType.SATURN_3
            else -> PrinterType.UNKNOWN
        },
        status = when (dataModel.data.status.printInfo.status) {
            0 -> PrinterStatus.Ready
            1 -> PrinterStatus.Preparing
            2 -> PrinterStatus.Retracting
            3 -> PrinterStatus.Exposing
            4 -> PrinterStatus.Lifting
            5, 6 -> PrinterStatus.Pausing
            7 -> PrinterStatus.Paused
            9 -> PrinterStatus.Cancelling
            12 -> PrinterStatus.Finalizing
            13 -> PrinterStatus.Cancelled
            16 -> PrinterStatus.Complete
            else -> PrinterStatus.Unknown
        },
        resolution = dataModel.data.attributes.resolution,
        firmwareVersion = dataModel.data.attributes.firmwareVersion,
        currentLayer = dataModel.data.status.printInfo.currentLayer,
        totalLayers = dataModel.data.status.printInfo.totalLayer,
        elapsedTime = dataModel.data.status.printInfo.currentTicks.milliseconds,
        estimatedTime = (dataModel.data.status.printInfo.totalTicks - dataModel.data.status.printInfo.currentTicks).milliseconds,
    )
}
