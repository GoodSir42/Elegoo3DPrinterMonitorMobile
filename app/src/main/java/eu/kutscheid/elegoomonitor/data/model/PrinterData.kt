package eu.kutscheid.elegoomonitor.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject

@Serializable
data class PrinterAttributes(
    // Shared by every printer.
    @SerialName("Name")
    val name: String,
    @SerialName("MachineName")
    val machineName: String,
    @SerialName("ProtocolVersion")
    val protocolVersion: String,
    @SerialName("FirmwareVersion")
    val firmwareVersion: String,
    @SerialName("MainboardIP")
    val mainboardIP: String,
    @SerialName("MainboardID")
    val mainboardID: String,
    // Only sent by the flat (Centauri) response format.
    @SerialName("BrandName")
    val brandName: String? = null,
    // Only sent by the nested (Mars/Saturn) response format.
    @SerialName("Resolution")
    val resolution: String? = null,
    @SerialName("SDCPStatus")
    val sDCPStatus: Int? = null,
    @SerialName("LocalSDCPAddress")
    val localSDCPAddress: String? = null,
    @SerialName("SDCPAddress")
    val sDCPAddress: String? = null,
    @SerialName("Capabilities")
    val capabilities: List<String> = emptyList(),
)

@Serializable
data class PrintInfo(
    @SerialName("Status")
    val status: Int,
    @SerialName("CurrentLayer")
    val currentLayer: Long,
    @SerialName("TotalLayer")
    val totalLayer: Long,
    @SerialName("CurrentTicks")
    val currentTicks: Long,
    @SerialName("TotalTicks")
    val totalTicks: Long,
    @SerialName("ErrorNumber")
    val errorNumber: Int,
    @SerialName("Filename")
    val filename: String,
)

@Serializable
data class FileTransferInfo(
    @SerialName("Status")
    val status: Int,
    @SerialName("DownloadOffset")
    val downloadOffset: Long,
    @SerialName("CheckOffset")
    val checkOffset: Long,
    @SerialName("FileTotalSize")
    val fileTotalSize: Long,
    @SerialName("Filename")
    val filename: String,
)

@Serializable
data class PrinterStatus(
    @SerialName("CurrentStatus")
    val currentStatus: Int,
    @SerialName("PreviousStatus")
    val previousStatus: Int,
    @SerialName("PrintInfo")
    val printInfo: PrintInfo,
    @SerialName("FileTransferInfo")
    val fileTransferInfo: FileTransferInfo,
)

@Serializable
data class PrinterData(
    @SerialName("Attributes")
    val attributes: PrinterAttributes,
    // Absent in the flat (Centauri) response format.
    @SerialName("Status")
    val status: PrinterStatus? = null,
)

@Serializable
data class PrinterItem(
    @SerialName("Id")
    val id: String,
    @SerialName("Data")
    @Serializable(with = PrinterDataSerializer::class)
    val data: PrinterData,
)

/**
 * Normalises the two Elegoo response shapes into [PrinterData]:
 *
 *  - Nested (Mars/Saturn): `Data` already contains `Attributes` (+ `Status`), so it is passed
 *    through untouched.
 *  - Flat (Centauri): the attribute fields sit directly under `Data`, so we wrap them into an
 *    `Attributes` object. There is no `Status` block, which [PrinterData.status] defaults to null.
 */
object PrinterDataSerializer : JsonTransformingSerializer<PrinterData>(PrinterData.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        if ("Attributes" in element.jsonObject) return element
        return buildJsonObject { put("Attributes", element) }
    }
}