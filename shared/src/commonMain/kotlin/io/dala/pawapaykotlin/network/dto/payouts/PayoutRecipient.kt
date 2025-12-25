package io.dala.pawapaykotlin.network.dto.payouts

import kotlinx.serialization.Serializable

@Serializable
data class PayoutRecipient(
    val type: String = "MSISDN",
    val address: PayoutAddress
)