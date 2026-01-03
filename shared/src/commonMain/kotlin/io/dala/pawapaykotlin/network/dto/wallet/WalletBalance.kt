package io.dala.pawapaykotlin.network.dto.wallet

import kotlinx.serialization.Serializable

@Serializable
data class WalletBalance(
    val country: String,
    val balance: String,
    val currency: String,
    val provider: String? = null
)