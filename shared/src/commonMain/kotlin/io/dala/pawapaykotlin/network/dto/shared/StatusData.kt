package io.dala.pawapaykotlin.network.dto.shared

import kotlinx.serialization.Serializable

@Serializable
data class StatusData(
    val depositId: String,
    val status: String,
    val amount: String,
    val currency: String,
    val failureReason: FailureReason? = null
)