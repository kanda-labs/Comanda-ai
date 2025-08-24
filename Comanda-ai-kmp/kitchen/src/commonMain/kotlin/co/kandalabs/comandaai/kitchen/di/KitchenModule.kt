package co.kandalabs.comandaai.kitchen.di

import co.kandalabs.comandaai.kitchen.data.api.KitchenApi
import co.kandalabs.comandaai.kitchen.data.api.KitchenApiImpl
import co.kandalabs.comandaai.kitchen.data.api.KitchenSSEClient
import co.kandalabs.comandaai.kitchen.data.repository.KitchenRepositoryImpl
import co.kandalabs.comandaai.kitchen.domain.repository.KitchenRepository
import co.kandalabs.comandaai.kitchen.presentation.KitchenViewModel
import co.kandalabs.comandaai.network.NetworkConfig
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

object KitchenDIModule {
    val kitchenModule = DI.Module("kitchenModule") {
        
        bindSingleton<KitchenApi> {
            KitchenApiImpl(
                httpClient = instance(),
                baseUrl = NetworkConfig.currentBaseUrl
            )
        }
        
        bindSingleton<KitchenSSEClient> {
            KitchenSSEClient(
                httpClient = instance(),
                json = instance(),
                baseUrl = NetworkConfig.currentBaseUrl,
                logger = instance()
            )
        }
        
        bindSingleton<KitchenRepository> {
            KitchenRepositoryImpl(instance(), instance())
        }
        
        bindProvider {
            KitchenViewModel(instance(), instance())
        }
    }
}