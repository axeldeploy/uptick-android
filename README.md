
# UptickManager

`UptickManager` is a Kotlin class designed to manage the flow of offers and display them in a customizable view. It handles network requests to fetch offer data and renders the offer UI within the provided container.

## Features
- Allows customization of primary, secondary, and background colors.
- Fetches offer flows and displays them based on the provided integration ID and placement.
- Automatically handles errors and supports dynamic content updates in the view.
- Supports multiple offer types (e.g., buttons, text, disclaimers) and allows interaction through buttons.

## Installation

[![](https://jitpack.io/v/axeldeploy/uptick-android.svg)](https://jitpack.io/#axeldeploy/uptick-android)

To use the `UptickManager` class in your Android project, include the following in your project's `settings.gradle`:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

and app level `build.gradle` file:

```gradle
dependencies {
    implementation 'com.github.axeldeploy:uptick-android:$last_version'
}
```

### Requirements
- Android SDK with minimum API level 26 (Oreo).
- Kotlin support in your Android project.
- Coroutines for asynchronous task management.

## Usage

### Initialization
To initialize `UptickManager` and set up the view for displaying offers, use the `initiateView()` method.

```kotlin
// Initialize UptickManager and set up the offer display
val uptickManager = UptickManager()
uptickManager.initiateView(context, container, integrationId, Placement.ORDER_CONFIRMATION)
```

#### Parameters:
- **context**: The `Context` in which the Uptick offer view will be rendered (e.g., `Activity` or `Fragment`).
- **container**: The `FrameLayout` container where the offer UI will be placed.
- **integrationId**: A unique identifier for the offer integration. This ID must be provided by the backend.
- **placement**: Defines where the offer is being presented (default is `Placement.ORDER_CONFIRMATION`).

### Customizing Colors
You can customize the colors of the offer view by setting the primary, secondary, and background colors. The default colors are provided, but you can change them as follows:

```kotlin
// Set custom colors
uptickManager.setPrimaryColor(Color.parseColor("#5bb85d"))   // Example primary color
uptickManager.setSecondaryColor(Color.parseColor("#efefef")) // Example secondary color
uptickManager.setBgColor(Color.parseColor("#4D000000"))      // Example background color
```

### Error Handling
`UptickManager` supports handling errors during the offer flow. You can define a custom error handler function using the `onError` property:

```kotlin
uptickManager.onError = { errorMessage ->
    // Handle error here, for example, show a Toast or log the error
    Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
}
```

## Example

Here is an example of how to integrate `UptickManager` into your project:

```kotlin
val container = findViewById<FrameLayout>(R.id.offer_container)
val uptickManager = UptickManager()

uptickManager.setPrimaryColor(Color.parseColor("#ff5722"))
uptickManager.setSecondaryColor(Color.parseColor("#ffccbc"))
uptickManager.setBgColor(Color.parseColor("#eeeeee"))

uptickManager.onError = { errorMessage ->
    Log.e("UptickManager", "Error: $errorMessage")
}

uptickManager.initiateView(this, container, "integration_id_here", Placement.ORDER_CONFIRMATION)
```

## Contributing
If you find any issues or have suggestions for improvements, feel free to open a pull request or file an issue in the repository.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
