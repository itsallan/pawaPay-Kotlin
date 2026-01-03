package io.dala.pawapaykotlin.network.dto.wallet

import kotlinx.serialization.Serializable

@Serializable
data class WalletBalanceResponse(
    val balances: List<WalletBalance>
)