package io.dala.pawapaykotlin.network.dto.payouts

import kotlinx.serialization.Serializable

@Serializable
data class PayoutResponse(
    val payoutId: String,
    val status: String,
    val acceptanceDateTime: String? = null
)