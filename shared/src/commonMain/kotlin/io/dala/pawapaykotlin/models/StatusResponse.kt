package io.dala.pawapaykotlin.models

import kotlinx.serialization.Serializable

@Serializable
data class StatusResponse(
    val status: String,
    val data: StatusData? = null
)