package io.dala.pawapaykotlin.network.dto.deposits

import io.dala.pawapaykotlin.network.dto.shared.AccountDetails
import kotlinx.serialization.Serializable

@Serializable
data class Payer(
    val type: String,
    val accountDetails: AccountDetails
)