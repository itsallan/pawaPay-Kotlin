package io.dala.pawapaykotlin.network.dto.payouts

import kotlinx.serialization.Serializable

@Serializable
data class PayoutRequest(
    val payoutId: String,
    val amount: String,
    val currency: String,
    val correspondent: String,
    val recipient: PayoutRecipient,
    val statementDescription: String,
    val customerTimestamp: String
)