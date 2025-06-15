package eu.kutscheid.elegoomonitor.domain

import eu.kutscheid.elegoomonitor.data.UdpDataSource
import eu.kutscheid.elegoomonitor.data.model.PrinterItem
import eu.kutscheid.elegoomonitor.domain.model.FullPrinterEntity
import eu.kutscheid.elegoomonitor.domain.model.PrinterEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold

class DataRepository(dataSource: UdpDataSource) {
    private val printers = dataSource
        .startBroadcast()
        .runningFold(emptyList<PrinterItem>()) { initialList, item ->
            val existingIndex = initialList.indexOfFirst { it.id == item.id }
            if (existingIndex > -1) {
                val mutableList = initialList.toMutableList()
                mutableList.removeAt(existingIndex)
                mutableList.add(existingIndex, item)
                mutableList.toList()
            } else {
                initialList + item
            }
        }
        .map {
            it.map { FullPrinterEntity(it) }
        }

    fun getPrinterList(): Flow<List<PrinterEntity>> {
        return printers.map { fullPrinters -> fullPrinters.map { PrinterEntity(it) } }
    }

    fun getPrinterDetail(id: String): Flow<FullPrinterEntity> {
        return printers
            .map { allPrinters -> allPrinters.find { it.id == id } }
            .filterNotNull()
            .distinctUntilChanged()
    }
}