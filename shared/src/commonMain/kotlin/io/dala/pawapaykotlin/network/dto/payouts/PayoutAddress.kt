package io.dala.pawapaykotlin.network.dto.payouts

import kotlinx.serialization.Serializable

@Serializable
data class PayoutAddress(
    val value: String
)