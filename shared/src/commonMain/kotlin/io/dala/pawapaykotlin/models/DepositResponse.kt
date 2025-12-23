package io.dala.pawapaykotlin.models

import kotlinx.serialization.Serializable

@Serializable
data class DepositResponse(
    val depositId: String,
    val status: String,
    val reason: String? = null
)