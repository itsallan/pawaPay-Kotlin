package io.dala.pawapaykotlin.network.dto.refund

import kotlinx.serialization.Serializable

@Serializable
data class RefundResponse(
    val refundId: String,
    val status: String,
    val created: String? = null
)