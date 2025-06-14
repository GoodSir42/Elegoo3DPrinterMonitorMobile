package eu.kutscheid.elegoomonitor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.kutscheid.elegoomonitor.data.UdpDataSource
import eu.kutscheid.elegoomonitor.data.model.PrinterItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn

class PrinterInfoViewModel : ViewModel() {
    private val dataSource = UdpDataSource()

    val printerInfo =
        dataSource.startBroadcast().runningFold(emptyList<PrinterItem>()) { initialList, item ->
            val existingIndex = initialList.indexOfFirst { it.id == item.id }
            if (existingIndex > -1) {
                val mutableList = initialList.toMutableList()
                mutableList.removeAt(existingIndex)
                mutableList.add(existingIndex, item)
                mutableList.toList()
            } else {
                initialList + item
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = emptyList())
}