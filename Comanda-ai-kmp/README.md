# Dogify: A Multiplatform Dog Breed Explorer

Dogify is a Kotlin Multiplatform (KMP) project that fetches and displays information about various dog breeds from the [Dog API](https://dog.ceo/dog-api/documentation/). It's built using modern Mobile tools, such as Compose Multiplatform (CMP), allowing sharing of the entire UI for multiple platforms, showcasing best practices for data handling and UI design.

## Features
- **Browse Dog Breeds:** View a scrollable list of dog breeds.
- **Sub-breeds Support:** Display sub-breeds nested within each main breed.
- **Image Display:** Show images of the breeds fetched from the API.
- **Pagination:** Efficiently load and display large sets of breeds with pagination.
- **Offline Caching:** Store fetched breed data in a local SQLite database using SQLDelight for offline access and improved performance.
- **KMP:** Build for multiple platforms (Android and iOS) from a shared codebase.
- **CMP:** Declarative UI framework for building modern UIs shared across all platform targets.

## Technologies
- **Kotlin Multiplatform (KMP):** Share code between Android and iOS.
- **Compose Multiplatform:** Declarative UI framework for building modern UIs and sharing it across all platform targets.
- **SQLDelight:** Type-safe SQL library for local data persistence.
- **Kodein:** Dependency injection framework for managing dependencies.
- **Kotlin Coroutines:** For asynchronous programming and background tasks.
- **Ktorfit:** HTTP client for making network requests.
- **Ktor:** For client HTTP operations.
- **Coil:** Image loading and caching.
- **Voyager:** Navigation library for Jetpack Compose Multiplatform.
- **Immutable Collections:** Handling immutable lists efficiently.
- **Dog API:** The external API used as the data source.
- **Kover:** Test coverage report generation tool.

## Project Structure
The project follows a typical KMP structure with the following key modules:

- **`App Module`:**
- **`commonMain`:**
    - Contains core business logic, data models, repositories, use cases, and all the UI and its entry point.
    - `DogifyApp`, a composable that initializes `Voyager` and the first screen.
    - Defines the common database schema (`.sq` files) located in the `sqldelight` package.
    - Includes the Kodein dependency injection setup.
- **`androidMain`:**
    - Contains Android-specific code like the Application and Activity entry point.
    - Implements the `DriverFactory` for SQLDelight.
    - Handles Android-specific dependency injection setup.
- **`iosMain`:**
    - Contains iOS-specific code, like `MainViewController`, which is the entry point for the iOS app and contains the setup of dependency injection.
    - Implements the `DriverFactory` for SQLDelight.

- **`Core Module`:**
A KMP library module containing core functionalities and utilities essential for the application, such as custom exceptions `DogifyException`, the `DogifyLogger` interface for logging, and the `DogifyResult` sealed class for handling operation outcomes.

- **`Design System Module`:**
A KMP library module that contains the design system elements, such as theme, tokens, typography, and UI components. It encapsulates the visual and interaction elements of the app.

  
## Getting Started

### Prerequisites
- **Android Studio:** Latest version with Kotlin and KMP plugins installed.
- **Java Development Kit (JDK):** Version 17 or higher.
- **Xcode:** Required for building in iOS.

### Installation
1. **Clone the repository.**
2. **Open in Android Studio.**
    - Select "Open an existing project" and navigate to the cloned directory.
3. **Sync Project with Gradle Files.**
    - Click the elephant icon with the green arrow in Android Studio.
4. **Configure the SDKs.**
    - Verify that all the platform SDKs are configured.

### Building and Running
- **Android:**
    1. Select the `app` run configuration.
    2. Click the green "Run" button.
    3. An Android Virtual Device will be loaded, or you can select a physical Android device.

- **iOS:**
    1. Open Xcode.
    2. Select the `iosApp` folder to open the project.
    3. Select a device of your choice.
    4. Click the "Run" button.
    5. An iOS simulator will be loaded.

## Key Components

### System Architecture Overview
This project follows a Clean Architecture approach with **Domain**, **Data**, and **Presentation** layers.

![image](https://github.com/user-attachments/assets/f95443b1-7f14-4e62-b0bd-a462bdc99430)

### Domain Layer
- **`Breed` Domain Model:** Represents a dog breed with properties like `name`, `subBreedName`, and `url` (image URL).
- **`Page` Domain Model:** Represents a generic source of data with properties like `items` and `pageConfig`.
- **`PageConfig` Domain Model:** Represents the configuration of a page with properties like `offset`, `size`, and `totalItems`.
- **`BreedRepository`:** Interface for fetching and managing breed data.
- **`BreedsPaginationUseCase`:** Use case for controlling the flow of data, fetching breeds, and providing them as `Page` elements.

### Data Layer
- **`BreedsAndSubBreedsResponse` Data Model:** Used to deserialize dog breed JSON data from the API response with properties such as `status` and `message`.
- **`BreedImageResponse` Data Model:** Used to deserialize dog breed image JSON data from the API response with properties such as `status` and `message`.
- **`BreedsRepositoryImp`:** Implementation of `BreedRepository` that handles fetching from the API and local storage.
- **`LocalBreedRepository`:** Interface for local database operations.
- **`LocalBreedRepositoryImp`:** Implementation of `LocalBreedRepository` using SQLDelight.
- **SQLDelight Database:** Manages the local SQLite database for storing breeds.
- **`DriverFactory`:** Creates `SqlDriver` instances for each platform.

### Presentation | UI Layer
- **Screens:** Center screens and their elements, such as UI, ViewModel, ScreenState, and Strings.
- **`DogifyApp`:** The main composable that initializes Voyager and the first screen.

### Dependency Injection
- **Kodein:** Used throughout the project for dependency injection.
- **`databaseModule`:** Creates and provides the `DogifyDatabase`.
- **`driverFactoryModule`:** Creates and provides the `DriverFactory` for each platform.

## SQLDelight Database Setup
1. **Dependencies:**  
   SQLDelight plugin and dependencies are added in the `build.gradle.kts` file.
2. **Schema:**  
   `.sq` files define the database schema, located in `/app/src/commonMain/sqldelight`.
3. **Code Generation:**  
   SQLDelight generates Kotlin code based on the `.sq` files using the `ksp` library.
4. **Database Access:**  
   The generated `DogifyDatabase` class is used to access the database. You'll find its reference in `LocalBreedRepositoryImp`.  
   The database instance is provided by Kodein for each platform.
5. **Database `DriverFactory`:**  
   This class should be injected via dependency injection on each platform (e.g., `DogifyApplication` for Android, `MainViewController` for iOS).


## Testing

### Running Tests
The project includes a suite of unit and instrumentation tests to ensure code quality and reliability. To run the tests, use the following Gradle tasks:
- **Unit Tests:** `:app:testDebugUnitTest`
- **Instrumentation Tests:** `:app:testDebugAndroidUnitTest`
  

## Next Steps
- **UI improvements:** Improve the UI design.
- **More Features:** Add additional features to enhance the app's functionality.
- **Desktop support:** Add desktop support via Compose Multiplatform.
