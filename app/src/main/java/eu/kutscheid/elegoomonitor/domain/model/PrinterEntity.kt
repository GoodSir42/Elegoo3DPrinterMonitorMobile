package eu.kutscheid.elegoomonitor.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class PrinterEntity(
    val id: String,
    val name: String,
    val type: PrinterType,
    val status: PrinterStatus,
) {
    constructor(fullEntity: FullPrinterEntity) : this(
        id = fullEntity.id,
        name = fullEntity.name,
        type = fullEntity.type,
        status = fullEntity.status
    )
}
