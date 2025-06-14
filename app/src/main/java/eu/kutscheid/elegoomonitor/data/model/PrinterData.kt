package eu.kutscheid.elegoomonitor.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrinterAttributes(
    @SerialName("Name")
    val name: String,
    @SerialName("MachineName")
    val machineName: String,
    @SerialName("ProtocolVersion")
    val protocolVersion: String,
    @SerialName("FirmwareVersion")
    val firmwareVersion: String,
    @SerialName("Resolution")
    val resolution: String,
    @SerialName("MainboardIP")
    val mainboardIP: String,
    @SerialName("MainboardID")
    val mainboardID: String,
    @SerialName("SDCPStatus")
    val sDCPStatus: Int,
    @SerialName("LocalSDCPAddress")
    val localSDCPAddress: String,
    @SerialName("SDCPAddress")
    val sDCPAddress: String,
    @SerialName("Capabilities")
    val capabilities: List<String>,
    )

@Serializable
data class PrintInfo(
    @SerialName("Status")
    val status: Int,
    @SerialName("CurrentLayer")
    val currentLayer: Int,
    @SerialName("TotalLayer")
    val totalLayer: Int,
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
    @SerialName("Status")
    val status: PrinterStatus,
)

@Serializable
data class PrinterItem(
    @SerialName("Id")
    val id: String,
    @SerialName("Data")
    val data: PrinterData,
)