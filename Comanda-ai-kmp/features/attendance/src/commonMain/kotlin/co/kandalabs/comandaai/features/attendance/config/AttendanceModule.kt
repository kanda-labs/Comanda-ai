package co.kandalabs.comandaai.features.attendance.config

import co.kandalabs.comandaai.features.attendance.data.api.CommanderApi
import co.kandalabs.comandaai.features.attendance.data.repository.ItemsRepositoryImp
import co.kandalabs.comandaai.features.attendance.data.repository.OrderRepositoryImpl
import co.kandalabs.comandaai.features.attendance.data.repository.TablesRepositoryImp
import co.kandalabs.comandaai.features.attendance.data.repository.UserRepositoryImpl
import co.kandalabs.comandaai.features.attendance.domain.repository.ItemsRepository
import co.kandalabs.comandaai.features.attendance.domain.repository.OrderRepository
import co.kandalabs.comandaai.features.attendance.domain.repository.TablesRepository
import co.kandalabs.comandaai.features.attendance.domain.repository.UserRepository
import co.kandalabs.comandaai.features.attendance.domain.usecase.ProcessDrinksUseCase
import co.kandalabs.comandaai.features.attendance.domain.usecase.ProcessDrinksUseCaseImpl
import co.kandalabs.comandaai.features.attendance.domain.usecase.ProcessPromotionalItemsUseCase
import co.kandalabs.comandaai.features.attendance.domain.usecase.ProcessPromotionalItemsUseCaseImpl
import co.kandalabs.comandaai.features.attendance.domain.usecases.CreateUserUseCase
import co.kandalabs.comandaai.features.attendance.domain.usecases.GetAllUsersUseCase
import co.kandalabs.comandaai.features.attendance.domain.usecases.GetUserByIdUseCase
import co.kandalabs.comandaai.features.attendance.domain.usecases.UpdateUserUseCase
import co.kandalabs.comandaai.features.attendance.presentation.screens.admin.AdminViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.admin.UsersManagementViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.admin.UsersListViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.admin.UserDetailsViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.admin.ItemsManagementViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.admin.ItemFormViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.itemsSelection.BreedsListingViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.order.OrderScreenModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.ordercontrol.OrderControlViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.payment.PaymentSummaryViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.paymentHistory.PaymentHistoryViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.partialPaymentDetails.PartialPaymentDetailsViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.tables.details.TablesDetailsViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.tables.listing.TablesViewModel
import co.kandalabs.comandaai.features.attendance.presentation.screens.tables.migration.TableMigrationViewModel
import co.kandalabs.comandaai.sdk.session.SessionManager
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.util.logging.Logger
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
                commanderApi = instance(),
                logger = instance()

            )
        }

        bindSingleton<TablesRepository> {
            TablesRepositoryImp(
                commanderApi = instance()
            )
        }

        bindSingleton<UserRepository> {
            UserRepositoryImpl(
                api = instance(),
                dispatcher = Dispatchers.IO
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

        bindProvider<CreateUserUseCase> {
            CreateUserUseCase(
                userRepository = instance()
            )
        }

        bindProvider<GetAllUsersUseCase> {
            GetAllUsersUseCase(
                userRepository = instance()
            )
        }

        bindProvider<GetUserByIdUseCase> {
            GetUserByIdUseCase(
                userRepository = instance()
            )
        }

        bindProvider<UpdateUserUseCase> {
            UpdateUserUseCase(
                userRepository = instance()
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
                repository = instance<TablesRepository>(),
                sessionManager = instance<SessionManager>()
            )
        }

        bindProvider<PartialPaymentDetailsViewModel> {
            PartialPaymentDetailsViewModel(
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

        bindProvider<UsersManagementViewModel> {
            UsersManagementViewModel(
                createUserUseCase = instance()
            )
        }

        bindProvider<UsersListViewModel> {
            UsersListViewModel(
                getAllUsersUseCase = instance()
            )
        }

        bindProvider<UserDetailsViewModel> {
            UserDetailsViewModel(
                getUserByIdUseCase = instance(),
                updateUserUseCase = instance()
            )
        }

        bindProvider<PaymentHistoryViewModel> {
            PaymentHistoryViewModel(
                repository = instance<TablesRepository>(),
                sessionManager = instance()
            )
        }

        bindProvider<ItemsManagementViewModel> {
            ItemsManagementViewModel(
                itemsRepository = instance(),
                logger = instance()
            )
        }

        bindProvider<ItemFormViewModel> {
            ItemFormViewModel(
                itemsRepository = instance(),
                logger = instance()
            )
        }

    }
}