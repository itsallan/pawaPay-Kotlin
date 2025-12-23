package io.dala.pawapaykotlin.models

import kotlinx.serialization.Serializable

@Serializable
data class AccountDetails(
    val phoneNumber: String,
    val provider: String
)