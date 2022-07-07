package im.threads.android.di

import im.threads.android.use_cases.developer_options.DebugMenuInteractor
import im.threads.android.use_cases.developer_options.DebugMenuUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single<DebugMenuUseCase> { DebugMenuInteractor(androidContext()) }
}
