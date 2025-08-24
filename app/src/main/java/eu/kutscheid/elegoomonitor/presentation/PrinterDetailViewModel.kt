package eu.kutscheid.elegoomonitor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.kutscheid.elegoomonitor.domain.DataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class PrinterDetailViewModel(repository: DataRepository) : ViewModel() {
    lateinit var printerId: String

    val printer by lazy {
        repository.getPrinterDetail(printerId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = null)
    }
}