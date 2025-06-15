package eu.kutscheid.elegoomonitor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.kutscheid.elegoomonitor.domain.DataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class PrinterInfoViewModel(repository: DataRepository) : ViewModel() {
    val printerInfo = repository.getPrinterList()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = emptyList())
}