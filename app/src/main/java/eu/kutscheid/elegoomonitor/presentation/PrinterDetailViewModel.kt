package eu.kutscheid.elegoomonitor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.kutscheid.elegoomonitor.domain.DataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PrinterDetailViewModel(private val repository: DataRepository) : ViewModel() {
    lateinit var printerId: String

    val printer by lazy {
        repository.getPrinterDetail(printerId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = null)
    }

    /**
     * The MJPEG stream URL to display, or null if the printer offers no reachable stream. The HEAD
     * probe runs only when the printer's IP changes, not on every status update.
     */
    val videoStreamUrl by lazy {
        repository.getPrinterDetail(printerId)
            .map { it.ipAddress }
            .distinctUntilChanged()
            .map { ip -> repository.getVideoStreamUrl(ip) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = null)
    }
}
