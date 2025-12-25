package io.dala.pawapaykotlin.network.dto.shared

import kotlinx.serialization.Serializable

@Serializable
data class FailureReason(
    val failureCode: String,
    val failureMessage: String
)