# pawaPay Kotlin SDK (KMP)

A lightweight Kotlin Multiplatform (KMP) SDK for integrating **pawaPay v2** mobile money payments into Android and iOS applications.

[![](https://jitpack.io/v/itsallan/PawapayKotlin.svg)](https://jitpack.io/#itsallan/PawapayKotlin)

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
        implementation("com.github.itsallan.PawapayKotlin:shared:1.0.0-alpha04")
    }
}
```

#### For Pure Android Projects
If you are using this in a standard Android app:

```kotlin
dependencies {
    implementation("com.github.itsallan.PawapayKotlin:shared-android:1.0.0-alpha04")
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
### Initiate a Payout (Disbursement)
Send money to a customer's wallet. This follows the strict pawaPay v2 schema using the `recipient` and `accountDetails` structure.

```kotlin
val repository: PawaPayRepository = koin.get()
val requestId = generateUUID()

val result = repository.sendPayout(
    payoutId = requestId,
    amount = "500",
    phoneNumber = "256778529661",
    currency = "UGX",
    correspondent = "MTN_MOMO_UGA",
    description = "Withdrawal"
)

result.onSuccess { response ->
    // Initiation successful, status will be "ACCEPTED"
    pollTransaction(response.payoutId, TransactionType.PAYOUT)
}
```

### Initiate a Deposit (Collection)
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
Mobile money transactions are asynchronous. Use the polling utility to wait for a terminal state (`COMPLETED`, `FAILED` or `REJECTED`).

```kotlin
suspend fun pollTransaction(id: String, type: TransactionType) {
    repository.pollTransactionStatus(id, type).fold(
        onSuccess = { response ->
            // Success: response.data contains the final status
            println("Payment Successful: ${response.data?.status}")
        },
        onFailure = { error ->
            println("Transaction Failed: ${error.message}")
        }
    )
}
```

##  Roadmap & Capabilities

### What it handles now
- [x] **Deposit Initiation**: Support for pawaPay v2 `MMO` (Mobile Money) payments.
- [x] **Payouts (Withdrawals)**: Send money to users directly from the SDK.
- [x] **Smart Polling**: Automatic handling of `NOT_FOUND` and `PROCESSING` states.
- [x] **Nested Data Mapping**: Correct parsing of the v2 `StatusResponse` data objects.
- [x] **KMP Support**: Shared logic for both Android and iOS targets.

### Coming Soon (Roadmap)
- [ ] **Refunds**: Support for initiating and checking refund statuses.
- [ ] **Signature Verification**: Enhanced security for signed API requests.
- [ ] **Payment Page**: Integration with the hosted pawaPay payment page.

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
