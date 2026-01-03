package io.dala.pawapaykotlin.network.dto.toolkit

import kotlinx.serialization.Serializable

@Serializable
data class PredictProviderRequest(
    val phoneNumber: String
)