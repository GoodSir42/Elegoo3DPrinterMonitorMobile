package eu.kutscheid.elegoomonitor.di

import eu.kutscheid.elegoomonitor.data.UdpDataSource
import eu.kutscheid.elegoomonitor.data.VideoStreamDataSource
import eu.kutscheid.elegoomonitor.data.WebSocketDataSource
import eu.kutscheid.elegoomonitor.domain.DataRepository
import eu.kutscheid.elegoomonitor.presentation.PrinterDetailViewModel
import eu.kutscheid.elegoomonitor.presentation.PrinterInfoViewModel
import eu.kutscheid.elegoomonitor.presentation.widget.WidgetUpdateWorker
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun appModule() = module {
    singleOf(::DataRepository)
    singleOf(::UdpDataSource)
    singleOf(::WebSocketDataSource)
    singleOf(::VideoStreamDataSource)
    viewModelOf(::PrinterInfoViewModel)
    viewModelOf(::PrinterDetailViewModel)
    workerOf(::WidgetUpdateWorker)
}