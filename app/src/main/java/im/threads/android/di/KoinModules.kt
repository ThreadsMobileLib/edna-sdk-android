package im.threads.android.di

import im.threads.android.useCases.developerOptions.DebugMenuInteractor
import im.threads.android.useCases.developerOptions.DebugMenuUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single<DebugMenuUseCase> { DebugMenuInteractor(androidContext()) }
}
