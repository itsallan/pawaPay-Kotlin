package io.dala.pawapaykotlin.repository

import io.dala.pawapaykotlin.models.DepositRequest
import io.dala.pawapaykotlin.models.DepositResponse
import io.dala.pawapaykotlin.models.Payer
import io.dala.pawapaykotlin.models.AccountDetails
import io.dala.pawapaykotlin.models.StatusResponse
import io.dala.pawapaykotlin.network.PawaPayApi
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay

expect fun generateUUID(): String

class PawaPayRepositoryImpl(
    private val api: PawaPayApi
) : PawaPayRepository {

    override suspend fun pay(
        amount: String,
        phoneNumber: String,
        currency: String,
        provider: String
    ): Result<DepositResponse> {
        return try {
            val request = DepositRequest(
                depositId = generateUUID(),
                amount = amount,
                currency = currency,
               // correspondent = provider,
                payer = Payer(
                    type = "MMO",
                    accountDetails = AccountDetails(
                        phoneNumber = phoneNumber,
                        provider = provider
                    )
                ),
                customerMessage = "Payment of $amount $currency",
                //statementDescription = "Dala Payment"
            )

            val response = api.initiateDeposit(request)
            Result.success(response)
        } catch (e: ResponseException) {
            val errorBody = e.response.bodyAsText()
            Result.failure(Exception(errorBody))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTransactionStatus(depositId: String): Result<StatusResponse> {
        return try {
            val status = api.getStatus(depositId)
            Result.success(status)
        } catch (e: ResponseException) {
            val errorBody = e.response.bodyAsText()
            Result.failure(Exception(errorBody))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pollDepositStatus(depositId: String): Result<StatusResponse> {
        val maxAttempts = 30
        var attempts = 0

        while (attempts < maxAttempts) {
            val statusResult = getTransactionStatus(depositId)

            statusResult.onSuccess { response ->
                val actualStatus = response.data?.status ?: response.status

                when (actualStatus) {
                    "COMPLETED" -> return Result.success(response)
                    "FAILED", "REJECTED" -> {
                        val message = response.data?.failureReason?.failureMessage ?: "Payment failed"
                        return Result.failure(Exception(message))
                    }
                    // If "PROCESSING" or "ACCEPTED", the loop continues
                }
            }

            attempts++
            delay(4000)
        }
        return Result.failure(Exception("Payment timed out"))
    }
}