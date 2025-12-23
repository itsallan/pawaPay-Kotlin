package io.dala.pawapaykotlin.models

import kotlinx.serialization.Serializable

@Serializable
data class DepositRequest(
    val depositId: String,
    val amount: String,
    val currency: String,
    val payer: Payer,
    val customerMessage: String,
)