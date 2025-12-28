package io.dala.pawapaykotlin.network.dto.refund

import kotlinx.serialization.Serializable

@Serializable
data class RefundRequest(
    val refundId: String,
    val depositId: String,
    val amount: String
)