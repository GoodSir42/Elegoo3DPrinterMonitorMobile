package eu.kutscheid.elegoomonitor.di

import eu.kutscheid.elegoomonitor.data.UdpDataSource
import eu.kutscheid.elegoomonitor.domain.DataRepository
import eu.kutscheid.elegoomonitor.presentation.PrinterInfoViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun appModule() = module {
    singleOf(::DataRepository)
    singleOf(::UdpDataSource)
    viewModelOf(::PrinterInfoViewModel)
}