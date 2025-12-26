package io.dala.pawapaykotlin.network.dto.deposits

import io.dala.pawapaykotlin.network.dto.deposits.Payer
import kotlinx.serialization.Serializable

@Serializable
data class DepositRequest(
    val depositId: String,
    val amount: String,
    val currency: String,
    val payer: Payer,
    val customerMessage: String,
)