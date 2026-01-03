package io.dala.pawapaykotlin.network.dto.toolkit

import kotlinx.serialization.Serializable

@Serializable
data class PredictProviderResponse(
    val country: String,
    val provider: String,
    val phoneNumber: String
)