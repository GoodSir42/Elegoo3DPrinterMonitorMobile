package eu.kutscheid.elegoomonitor.domain.model

import eu.kutscheid.elegoomonitor.data.model.PrinterItem
import java.time.Instant


data class FullPrinterEntity(
    val lastSeen: Instant,
    val id: String,
    val name: String,
    val type: PrinterType,
    val status: PrinterStatus,
) {
    constructor(dataModel: PrinterItem) : this(
        lastSeen = Instant.now(),
        id = dataModel.id,
        name = dataModel.data.attributes.name,
        type = when (dataModel.data.attributes.machineName) {
            "ELEGOO Mars 4 Ultra" -> PrinterType.MARS_4
            "ELEGOO Saturn 3 Ultra" -> PrinterType.SATURN_3
            else -> PrinterType.UNKNOWN
        },
        status = when (dataModel.data.status.currentStatus) {
            0 -> PrinterStatus.Ready
            1 -> PrinterStatus.Preparing
            2 -> PrinterStatus.Retracting
            3 -> PrinterStatus.Exposing
            4 -> PrinterStatus.Lifting
            5 -> PrinterStatus.Pausing
            7 -> PrinterStatus.Paused
            9 -> PrinterStatus.Cancelling
            12 -> PrinterStatus.Finalizing
            13 -> PrinterStatus.Cancelled
            16 -> PrinterStatus.Complete
            else -> PrinterStatus.Unknown
        }
    )
}
