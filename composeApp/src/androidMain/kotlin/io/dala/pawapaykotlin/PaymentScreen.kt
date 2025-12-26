package io.dala.pawapaykotlin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.dala.pawapaykotlin.domain.TransactionType
import io.dala.pawapaykotlin.network.dto.deposits.DepositResponse
import io.dala.pawapaykotlin.network.dto.payouts.PayoutResponse
import io.dala.pawapaykotlin.network.dto.shared.PaymentUiState
import io.dala.pawapaykotlin.repository.PawaPayRepository
import io.dala.pawapaykotlin.util.generateUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun PaymentScreen(repository: PawaPayRepository) {
    val scope = rememberCoroutineScope()
    var uiState by remember { mutableStateOf<PaymentUiState>(PaymentUiState.Idle) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = uiState) {
            is PaymentUiState.Idle -> {
                Text(
                    text = "pawaPay SDK test",
                    style = MaterialTheme.typography.headlineMedium,
                )

                Spacer(modifier = Modifier.height(32.dp))

                PaymentButton(
                    label = "Deposit 1,000 UGX",
                    onClick = {
                        processTransaction(scope, repository, TransactionType.DEPOSIT) {
                            uiState = it
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PaymentButton(
                    label = "Payout 500 UGX",
                    containerColor = MaterialTheme.colorScheme.secondary,
                    onClick = {
                        processTransaction(scope, repository, TransactionType.PAYOUT) {
                            uiState = it
                        }
                    }
                )
            }

            is PaymentUiState.Loading -> {
                CircularProgressIndicator(strokeWidth = 4.dp)
                Spacer(modifier = Modifier.height(24.dp))
                Text("Talking to pawaPay...", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "This might take a few seconds",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            is PaymentUiState.Success -> {
                val tx = state.data

                Text(
                    text = "Transaction Complete",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow("Amount", "${tx.amount} ${tx.currency}")
                    DetailRow("Status", tx.status ?: "COMPLETED")
                    DetailRow("Reference", tx.providerTransactionId ?: "Pending")
                    DetailRow("Transaction ID", tx.payoutId ?: tx.depositId ?: "N/A")
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = { uiState = PaymentUiState.Idle },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Dashboard")
                }
            }

            is PaymentUiState.Error -> {
                Text(
                    text = "Something went wrong",
                    color = Color.Red,
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = state.message,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = { uiState = PaymentUiState.Idle },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Try Again")
                }
            }
        }
    }
}


private fun processTransaction(
    scope: CoroutineScope,
    repository: PawaPayRepository,
    type: TransactionType,
    onStateChange: (PaymentUiState) -> Unit
) {
    onStateChange(PaymentUiState.Loading)

    scope.launch {
        // We generate a local ID to track the attempt before the server responds
        val requestId = generateUUID()

        val initialResult = if (type == TransactionType.DEPOSIT) {
            repository.pay(amount = "1000", phoneNumber = "256778529661")
        } else {
            repository.sendPayout(
                payoutId = requestId,
                amount = "500",
                phoneNumber = "256778529661",
                currency = "UGX",
                correspondent = "MTN_MOMO_UGA",
                description = "SDK Test Payout"
            )
        }

        initialResult.fold(
            onSuccess = { response ->
                // The status tells us if pawaPay accepted the request into their queue
                val status = when (response) {
                    is DepositResponse -> response.status
                    is PayoutResponse -> response.status
                    else -> "UNKNOWN"
                }

                // We prefer the ID returned by pawaPay but fall back to our local ID if needed
                val confirmedId = when (response) {
                    is DepositResponse -> response.depositId
                    is PayoutResponse -> response.payoutId
                    else -> requestId
                }

                // If REJECTED, the transaction won't be processed; we stop here
                if (status == "REJECTED") {
                    onStateChange(PaymentUiState.Error("The transaction was rejected. Please check your account balance."))
                    return@fold
                }

                // Since these are asynchronous, we poll to wait for the final outcome
                val finalStatusResult = repository.pollTransactionStatus(confirmedId, type)

                onStateChange(
                    finalStatusResult.fold(
                        onSuccess = { statusResponse ->
                            statusResponse.data?.let {
                                PaymentUiState.Success(it)
                            } ?: PaymentUiState.Error("We couldn't retrieve the final status.")
                        },
                        onFailure = { error ->
                            PaymentUiState.Error(error.message ?: "The request timed out.")
                        }
                    )
                )
            },
            onFailure = { error ->
                onStateChange(PaymentUiState.Error("Network error: ${error.message}"))
            }
        )
    }
}

@Composable
fun PaymentButton(
    label: String,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        Text(text = label)
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End
        )
    }
}