package io.dala.pawapaykotlin.models

import kotlinx.serialization.Serializable

@Serializable
data class Payer(
    val type: String,
    val accountDetails: AccountDetails
)