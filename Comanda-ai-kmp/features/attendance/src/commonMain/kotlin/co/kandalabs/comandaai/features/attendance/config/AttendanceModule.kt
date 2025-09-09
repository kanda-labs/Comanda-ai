package co.kandalabs.comandaai.features.attendance.config

import co.kandalabs.comandaai.features.attendance.data.api.CommanderApi
import co.kandalabs.comandaai.features.attendance.data.repository.ItemsRepositoryImp
import co.kandalabs.comandaai.features.attendance.data.repository.OrderRepositoryImpl
import co.kandalabs.comandaai.features.attendance.data.repository.TablesRepositoryImp
import co.kandalabs.comandaai.features.attendance.domain.repository.ItemsRepository
import co.kandalabs.comandaai.features.attendance.domain.repository.OrderRepository
import co.kandalabs.comandaai.features.attendance.domain.repository.TablesRepository
import co.kandalabs.comandaai.features.attendance.domain.usecase.ProcessDrinksUseCase
import co.kandalabs.comandaai.features.attendance.domain.usecase.ProcessDrinksUseCaseImpl
import co.kandalabs.comandaai.features.attendance.domain.usecase.ProcessPromotionalItemsUseCase
import co.kandalabs.comandaai.features.attendance.domain.usecase.ProcessPromotionalItemsUseCaseImpl
import co.kandalabs.comandaai.features.attendance.presentation.screens.admin.AdminViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.itemsSelection.BreedsListingViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.order.OrderScreenModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.ordercontrol.OrderControlViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.payment.PaymentSummaryViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.tables.details.TablesDetailsViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.tables.listing.TablesViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.tables.migration.TableMigrationViewModel
import de.jensklingenberg.ktorfit.Ktorfit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

object AttendanceModule {
    val attendanceModule = DI.Module("attendanceModule") {
        
        // API
        bindSingleton<CommanderApi> {
            instance<Ktorfit>().create()
        }

        // Repositories
        bindSingleton<ItemsRepository> {
            ItemsRepositoryImp(
                api = instance(),
                logger = instance(),
                dispatcher = Dispatchers.IO
            )
        }

        bindSingleton<OrderRepository> {
            OrderRepositoryImpl(
                commanderApi = instance()
            )
        }

        bindSingleton<TablesRepository> {
            TablesRepositoryImp(
                commanderApi = instance()
            )
        }

        // Use Cases
        bindProvider<ProcessPromotionalItemsUseCase> {
            ProcessPromotionalItemsUseCaseImpl(
                itemsRepository = instance(),
                orderRepository = instance(),
            )
        }

        bindProvider<ProcessDrinksUseCase> {
            ProcessDrinksUseCaseImpl(
                itemsRepository = instance(),
                orderRepository = instance()
            )
        }

        // ViewModels
        bindProvider<TablesViewModel> {
            TablesViewModel(
                repository = instance<TablesRepository>(),
                sessionManager = instance()
            )
        }

        bindProvider<TableMigrationViewModel> {
            TableMigrationViewModel(
                tablesRepository = instance<TablesRepository>(),
            )
        }

        bindProvider<TablesDetailsViewModel> {
            TablesDetailsViewModel(
                repository = instance<TablesRepository>(),
                sessionManager = instance()
            )
        }

        bindProvider<PaymentSummaryViewModel> {
            PaymentSummaryViewModel(
                repository = instance<TablesRepository>()
            )
        }

        bindProvider<BreedsListingViewModel> {
            BreedsListingViewModel(repository = instance<ItemsRepository>())
        }

        bindProvider<OrderScreenModel> {
            OrderScreenModel(
                itemsRepository = instance<ItemsRepository>(),
                orderRepository = instance<OrderRepository>(),
                sessionManager = instance(),
                processPromotionalItemsUseCase = instance<ProcessPromotionalItemsUseCase>(),
                processDrinksUseCase = instance<ProcessDrinksUseCase>()
            )
        }
        
        bindProvider<OrderControlViewModel> {
            OrderControlViewModel(
                sessionManager = instance(),
                orderRepository = instance<OrderRepository>()
            )
        }

        bindProvider<AdminViewModel> {
            AdminViewModel(
                instance()
            )
        }
    }
}