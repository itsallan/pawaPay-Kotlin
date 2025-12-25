package io.dala.pawapaykotlin.network.dto.shared

import kotlinx.serialization.Serializable

@Serializable
data class StatusResponse(
    val status: String,
    val data: StatusData? = null
)