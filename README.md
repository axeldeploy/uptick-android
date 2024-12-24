
# Uptick SDK

Uptick SDK is an Android library designed to integrate personalized offers into your app. This SDK allows developers to render offers based on specific placements with minimal configuration.

---

## Features
- **Dynamic Offer Rendering**: Display offers tailored to specific contexts and user data.
- **Multiple Placements**: Supports predefined placements such as `ORDER_CONFIRMATION`, `ORDER_STATUS`, and `SURVEY`.
- **Error Handling and Callbacks**: Includes hooks for handling errors and receiving rendering events.
- **Supports Dynamic Content**: Renders multiple offer types, including buttons, text, disclaimers, and more.
- **Automatic Flow Management**: Fetches and updates offers dynamically based on integration details.

---

## Installation

[![](https://jitpack.io/v/axeldeploy/uptick-android.svg)](https://jitpack.io/#axeldeploy/uptick-android)

To include the Uptick SDK in your Android project, add the following to your `settings.gradle`:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency in your app-level `build.gradle` file:

```gradle
dependencies {
    implementation 'com.github.axeldeploy:uptick-android:$last_version'
}
```

---

## Requirements
- **Minimum Android Version**: API Level 26 (Android 8.0 Oreo)

---

## Usage

### 1. Add a Container
Define two `FrameLayout` containers in your layout to host the offers, one for popup offers and one for inline offers. Set both containers to match the parent width and height.

```xml
<FrameLayout
    android:id="@+id/adView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@android:color/white" />

<FrameLayout
    android:id="@+id/adViewInline"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@android:color/white" />
```

### 2. Initialize UptickManager
Create an instance of `UptickManager` in your activity or fragment.

```kotlin
val uptickManager = UptickManager()
```

### 3. Configure Callbacks
Define callbacks for handling errors and receiving render types. Dynamically determine where the offer is displayed based on the `renderType`.

```kotlin
val uptickView = FrameLayout(this)
uptickManager.onRenderTypeReceived = { renderType ->
    (uptickView.parent as? ViewGroup)?.removeView(uptickView)
    if (renderType == "popup") {
        findViewById<FrameLayout>(R.id.adView).addView(uptickView)
    } else {
        findViewById<FrameLayout>(R.id.adViewInline).addView(uptickView)
    }
}
```

### 4. Render Offers
Call the `initiateView` method with the required parameters to display an offer.

```kotlin
import com.uptick.sdk.model.Placement

uptickManager.initiateView(
    context = this,
    container = uptickView,
    integrationId = "YOUR_INTEGRATION_ID",
    placement = Placement.ORDER_CONFIRMATION,
    optionalParams = mapOf("user_id" to "123", "order_id" to "ABC123")
)
```

---

## Placement Enum
The `Placement` enum is defined in the `com.uptick.sdk.model` package. It provides predefined contexts for offer rendering.

```kotlin
enum class Placement(val value: String) {
    ORDER_CONFIRMATION("order_confirmation"),
    ORDER_STATUS("order_status"),
    SURVEY("survey")
}
```

---

## Error Handling
`UptickManager` supports handling errors during the offer flow. You can define a custom error handler function using the `onError` property:

```kotlin
uptickManager.onError = { errorMessage ->
    // Handle error here, for example, show a Toast or log the error
    Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
}
```

---

## Example

Here is an example of how to integrate `UptickManager` into your project:

```kotlin
val uptickView = FrameLayout(this)
val uptickManager = UptickManager()

uptickManager.onRenderTypeReceived = { renderType ->
    (uptickView.parent as? ViewGroup)?.removeView(uptickView)
    if (renderType == "popup") {
        findViewById<FrameLayout>(R.id.adView).addView(uptickView)
    } else {
        findViewById<FrameLayout>(R.id.adViewInline).addView(uptickView)
    }
}

uptickManager.onError = { errorMessage ->
    Log.e("UptickManager", "Error: $errorMessage")
}

uptickManager.initiateView(this, uptickView, "integration_id_here", Placement.ORDER_CONFIRMATION)
```

---

## Contributing
If you find any issues or have suggestions for improvements, feel free to open a pull request or file an issue in the repository.

---

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
