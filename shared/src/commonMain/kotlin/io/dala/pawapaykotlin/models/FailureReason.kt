package io.dala.pawapaykotlin.models

import kotlinx.serialization.Serializable

@Serializable
data class FailureReason(
    val failureCode: String,
    val failureMessage: String
)