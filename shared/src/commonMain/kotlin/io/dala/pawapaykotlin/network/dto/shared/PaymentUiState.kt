package io.dala.pawapaykotlin.network.dto.shared

/**
 * Represents the state of a pawaPay transaction in the UI.
 */

sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Loading : PaymentUiState()
    data class Success(val data: StatusData) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
}