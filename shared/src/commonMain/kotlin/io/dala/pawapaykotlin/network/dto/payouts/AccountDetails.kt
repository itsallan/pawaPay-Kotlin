package io.dala.pawapaykotlin.network.dto.payouts

import kotlinx.serialization.Serializable

@Serializable
data class AccountDetails(
    val phoneNumber: String,
    val provider: String
)