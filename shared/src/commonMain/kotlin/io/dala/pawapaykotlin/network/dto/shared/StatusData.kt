package io.dala.pawapaykotlin.network.dto.shared

import io.dala.pawapaykotlin.network.dto.payouts.Recipient
import kotlinx.serialization.Serializable

@Serializable
data class StatusData(
    val payoutId: String? = null,
    val depositId: String? = null,
    val status: String? = null,
    val amount: String? = null,
    val currency: String? = null,
    val country: String? = null,
    val created: String? = null,
    val providerTransactionId: String? = null,
    val recipient: Recipient? = null,
    val failureReason: FailureReason? = null
)