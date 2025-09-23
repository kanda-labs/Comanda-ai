package co.kandalabs.comandaai.core.di

import co.kandalabs.comandaai.core.logger.ComandaAiLogger
import co.kandalabs.comandaai.core.logger.createPlatformLogger
import org.kodein.di.DI
import org.kodein.di.bindSingleton

val sdkModule = DI.Module("SdkModule") {
    bindSingleton<ComandaAiLogger> {
        createPlatformLogger()
    }
}