# pawaPay Kotlin

A lightweight, KMP SDK for integrating pawaPay payments into Android and iOS applications..

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
        implementation("com.github.itsallan.PawapayKotlin:shared:1.0.0-alpha07")
    }
}
```

#### For Pure Android Projects
If you are using this in a standard Android app:

```kotlin
dependencies {
    implementation("com.github.itsallan.PawapayKotlin:shared-android:1.0.0-alpha07")
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

### Initiate a Deposit (Collection)
Request money from a customer's wallet.

```kotlin
val repository: PawaPayRepository = koin.get()

val result = repository.pay(
    amount = "1000",
    phoneNumber = "256778529661",
    currency = "UGX",
    provider = "MTN_MOMO_UGA"
)
```
### Initiate a Payout (Disbursement)
Send money to a customer's wallet.

```kotlin
val result = repository.sendPayout(
    payoutId = generateUUID(),
    amount = "500",
    phoneNumber = "256778529661",
    currency = "UGX",
    correspondent = "MTN_MOMO_UGA",
    description = "User Withdrawal"
)
```

### Initiate a Refund
Return funds from a successful deposit back to the customer's wallet.

```kotlin
val result = repository.refund(
    depositId = "ORIGINAL_DEPOSIT_ID",
    amount = "1000",
    currency = "UGX"
)
```

## Check Wallet Balances
Return available funds for a specific country & currency.

```kotlin
repository.getWalletBalances(country = "UGA").onSuccess { response ->
    response.balances.forEach { wallet ->
        println("Currency: ${wallet.currency} | Balance: ${wallet.balance}")
    }
}
```

## Predict Provider
Automatically detect the mobile network and country from a phone number.

```kotlin
repository.predictProvider("256778000000").onSuccess { result ->
    println("Country: ${result.country}")
    println("Provider: ${result.provider}")
    println("Sanitized: ${result.phoneNumber}")
}
```

### Poll for Final Status
Mobile money transactions are asynchronous. Use the polling utility to wait for a terminal state (`COMPLETED`, `FAILED` or `REJECTED`).

```kotlin
// Works for any TransactionType (DEPOSIT, PAYOUT, or REFUND)
repository.pollTransactionStatus(id, TransactionType.DEPOSIT).fold(
    onSuccess = { response ->
        println("Status: ${response.data?.status}")
    },
    onFailure = { error ->
        println("Error: ${error.message}")
    }
)
```

##  Roadmap & Capabilities

### What it handles now
- [x] **Deposit Initiation**: Support for initiating `MMO` (Mobile Money) payments.
- [x] **Payouts (Withdrawals)**: Send money to users directly from the SDK.
- [x] **Smart Polling**: Automatic handling of `NOT_FOUND` and `PROCESSING` states.
- [x] **Nested Data Mapping**: Correct parsing of the v2 `StatusResponse` data objects.
- [x] **Refunds**: Support for initiating and checking refund statuses.
- [x] **Wallet Balance**: Real-time checking of merchant account funds.
- [x] **Predict Provider**: Utilities to verify phone numbers and provider codes for different regions.

### Coming Soon (Roadmap)
- [ ] **Signature Verification**: Enhanced security for signed API requests.
- [ ] **Payment Page**: Integration with the hosted pawaPay payment page.

---

## Real-Life Example

Check out the `composeApp` module in this repository for a complete implementation. It includes:

* **UI State Management**: Handling `Loading`, `Success`, and `Error`states with Jetpack Compose.

---

> [!WARNING]  
> **Disclaimer**: This is a **community-maintained, unofficial SDK**. It is not developed, endorsed, or supported by pawaPay. Always test thoroughly in the Sandbox environment before moving to Production.
