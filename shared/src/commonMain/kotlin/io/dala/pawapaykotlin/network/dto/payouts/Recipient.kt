package io.dala.pawapaykotlin.network.dto.payouts

import io.dala.pawapaykotlin.network.dto.shared.AccountDetails
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class Recipient @OptIn(ExperimentalSerializationApi::class) constructor(
    @EncodeDefault
    val type: String = "MMO",
    val accountDetails: AccountDetails
)