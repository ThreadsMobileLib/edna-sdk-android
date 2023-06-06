package io.edna.threads.demo.appCode.business

import io.edna.threads.demo.appCode.business.mockJsonProvider.CurrentJsonProvider
import io.edna.threads.demo.appCode.business.mockJsonProvider.SamplesJsonProvider
import io.edna.threads.demo.appCode.fragments.demoSamplesFragment.DemoSamplesViewModel
import io.edna.threads.demo.appCode.fragments.demoSamplesList.DemoSamplesListViewModel
import io.edna.threads.demo.appCode.fragments.server.AddServerViewModel
import io.edna.threads.demo.appCode.fragments.server.ServerListViewModel
import io.edna.threads.demo.appCode.fragments.user.AddUserViewModel
import io.edna.threads.demo.appCode.fragments.user.UserListViewModel
import io.edna.threads.demo.integrationCode.fragments.launch.LaunchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { CurrentJsonProvider(get()) }
    single { SamplesJsonProvider(get()) }
    single { StringsProvider(get()) }
    single { PreferencesProvider(get()) }
    single { UiThemeProvider(get()) }
    factory { ServersProvider(get(), get()) }
    viewModel { LaunchViewModel(get(), get(), get()) }
    viewModel { UserListViewModel(get()) }
    viewModel { AddUserViewModel(get()) }
    viewModel { ServerListViewModel(get(), get()) }
    viewModel { AddServerViewModel(get()) }
    viewModel { DemoSamplesViewModel(get(), get()) }
    viewModel { DemoSamplesListViewModel(get(), get(), get()) }
}
