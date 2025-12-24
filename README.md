# pawaPay Kotlin SDK (KMP)

A lightweight Kotlin Multiplatform (KMP) SDK for integrating **pawaPay v2** mobile money payments into Android and iOS applications.


## Installation

### 1. Add the JitPack repository
In your `settings.gradle.kts` (or your root `build.gradle.kts`), add the JitPack repository:

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
```
### 2. Add the dependency
Add this to your `commonMain` source set:

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation("com.github.itsallan:PawapayKotlin:1.0.0-alpha")
    }
}
```

#### For Pure Android Projects
If you are using this in a standard Android app, use the `-android` suffix to ensure correct library resolution:

```kotlin
dependencies {
    implementation("com.github.itsallan:PawapayKotlin-android:1.0.0-alpha")
}
```

## Usage

Initialize the SDK in your platform entry point (e.g., `MainActivity.kt` or `MainViewController.kt`) by providing your environment credentials:

```kotlin
initKoin(
    baseUrl = "https://api.sandbox.pawapay.io/v2/",
    apiToken = "YOUR_PAWAPAY_TOKEN"
)
```

### Initiate a Deposit
Request a payment from a user by specifying the amount, phone number, and provider.



```kotlin
val repository: PawaPayRepository = koin.get()

val result = repository.pay(
    amount = "1000",
    phoneNumber = "256778529661",
    currency = "UGX",
    provider = "MTN_MOMO_UGA"
)

result.onSuccess { depositResponse ->
    // Request accepted by pawaPay, now wait for final status
    pollPayment(depositResponse.depositId)
}
```

### Poll for Final Status
Mobile money transactions are asynchronous. Use the polling utility to wait for a terminal state (`COMPLETED` or `FAILED`).

```kotlin
suspend fun pollPayment(id: String) {
    repository.pollDepositStatus(id).fold(
        onSuccess = { response ->
            // Success: response.data contains the final status
            println("Payment Successful: ${response.data?.status}")
        },
        onFailure = { error ->
            println("Payment Failed: ${error.message}")
        }
    )
}
```

##  Roadmap & Capabilities

### What it handles now
* **Deposit Initiation**: Support for pawaPay v2 `MMO` (Mobile Money) payments.
* **Smart Polling**: Automatic handling of `NOT_FOUND` and `PROCESSING` states.
* **Nested Data Mapping**: Correct parsing of the v2 `StatusResponse` data objects.
* **KMP Support**: Shared logic for both Android and iOS targets.

### Coming Soon (Roadmap)
* **Payouts (Withdrawals)**: Send money to users directly from the SDK.
* **Refunds**: Support for initiating and checking refund statuses.
* **Signature Verification**: Enhanced security for signed API requests.
* **Payment Page**: Integration with the hosted pawaPay payment page.

---

## Real-Life Example

For a complete, working implementation including a Compose Multiplatform UI, check out the `composeApp` module in this repository.

It demonstrates:
* **UI State Management**: Handling `Idle`, `Loading`, `Success`, and `Error` states reactively.
* **Architecture**: Integrating the Repository with a ViewModel or Screen-level Coroutine Scope.
* **Error Handling**: Practical examples of how to display failed transaction messages to the user.

---

> [!WARNING]  
> **Disclaimer**: This is a **community-maintained, unofficial SDK**. It is not developed, endorsed, or supported by pawaPay. Always test thoroughly in the Sandbox environment before moving to Production.