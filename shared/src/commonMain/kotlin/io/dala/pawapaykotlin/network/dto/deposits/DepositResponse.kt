package io.dala.pawapaykotlin.network.dto.deposits

import kotlinx.serialization.Serializable

@Serializable
data class DepositResponse(
    val depositId: String,
    val status: String,
    val reason: String? = null
)