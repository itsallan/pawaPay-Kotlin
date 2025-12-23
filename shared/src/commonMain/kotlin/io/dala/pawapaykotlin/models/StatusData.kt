package io.dala.pawapaykotlin.models

import kotlinx.serialization.Serializable

@Serializable
data class StatusData(
    val depositId: String,
    val status: String,
    val amount: String,
    val currency: String,
    val failureReason: FailureReason? = null
)