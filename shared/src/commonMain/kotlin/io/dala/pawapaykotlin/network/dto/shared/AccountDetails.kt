package io.dala.pawapaykotlin.network.dto.shared

import kotlinx.serialization.Serializable

@Serializable
data class AccountDetails(
    val phoneNumber: String,
    val provider: String
)