package io.dala.pawapaykotlin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.dala.pawapaykotlin.models.PaymentUiState
import io.dala.pawapaykotlin.repository.PawaPayRepository
import kotlinx.coroutines.launch

@Composable
fun PaymentScreen(repository: PawaPayRepository) {
    val scope = rememberCoroutineScope()
    var uiState by remember { mutableStateOf<PaymentUiState>(PaymentUiState.Idle) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = uiState) {
            is PaymentUiState.Idle -> {
                Text(
                    text = "pawaPay SDK Test",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Button(
                    onClick = {
                        uiState = PaymentUiState.Loading
                        scope.launch {
                            val result = repository.pay(
                                amount = "1000",
                                phoneNumber = "256778529661"
                            )

                            result.onSuccess { response ->
                                val finalResult = repository.pollDepositStatus(response.depositId)

                                uiState = finalResult.fold(
                                    onSuccess = { response ->
                                        response.data?.let {
                                            PaymentUiState.Success(it)
                                        } ?: PaymentUiState.Error("Data missing from response")
                                    },
                                    onFailure = { error ->
                                        PaymentUiState.Error(error.message ?: "Payment incomplete")
                                    }
                                )
                            }.onFailure { error ->
                                uiState = PaymentUiState.Error("Request failed: ${error.message}")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Pay 1,000 UGX")
                }
            }

            is PaymentUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Please enter your PIN on your phone...")

                Spacer(modifier = Modifier.height(32.dp))

                TextButton(onClick = { uiState = PaymentUiState.Idle }) {
                    Text("Stop Waiting", color = Color.Gray)
                }
            }

            is PaymentUiState.Success -> {
                Text(
                    text = "Payment Confirmed!",
                    color = Color.Green,
                    style = MaterialTheme.typography.headlineSmall
                )
                // Access amount and currency through the 'data' object
                Text("Amount: ${state.data.amount} ${state.data.currency}")
                Text("Transaction ID: ${state.data.depositId}")

                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { uiState = PaymentUiState.Idle }) {
                    Text("Done")
                }
            }

            is PaymentUiState.Error -> {
                Text(
                    text = "Payment Failed",
                    color = Color.Red,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = state.message,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { uiState = PaymentUiState.Idle }) {
                    Text("Retry")
                }
            }
        }
    }
}