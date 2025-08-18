package co.kandalabs.comandaai.kitchen.di

import co.kandalabs.comandaai.kitchen.data.api.KitchenApi
import co.kandalabs.comandaai.kitchen.data.api.KitchenApiImpl
import co.kandalabs.comandaai.kitchen.data.api.KitchenSSEClient
import co.kandalabs.comandaai.kitchen.data.repository.KitchenRepositoryImpl
import co.kandalabs.comandaai.kitchen.domain.repository.KitchenRepository
import co.kandalabs.comandaai.kitchen.presentation.KitchenViewModel
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val kitchenModule = DI.Module("kitchenModule") {
    
    bindSingleton<KitchenApi> {
        KitchenApiImpl(
            httpClient = instance(),
            baseUrl = "http://10.0.2.2:8081/" // Use same baseUrl as CommanderApi
        )
    }
    
    bindSingleton<KitchenSSEClient> {
        KitchenSSEClient(
            httpClient = instance(),
            json = instance(),
            baseUrl = "http://10.0.2.2:8081/",
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