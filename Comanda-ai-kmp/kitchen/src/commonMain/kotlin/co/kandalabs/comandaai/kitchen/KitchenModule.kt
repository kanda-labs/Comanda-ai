package co.kandalabs.comandaai.kitchen

import cafe.adriel.voyager.core.screen.Screen
import co.kandalabs.comandaai.kitchen.di.KitchenDIModule
import co.kandalabs.comandaai.kitchen.presentation.KitchenScreen
import org.kodein.di.DI

object KitchenModule {
    fun getKitchenScreen(): Screen = KitchenScreen()
    
    val kitchenDI = KitchenDIModule.kitchenModule
}