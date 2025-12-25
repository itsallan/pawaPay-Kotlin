package io.dala.pawapaykotlin.repository

import io.dala.pawapaykotlin.domain.TransactionType
import io.dala.pawapaykotlin.network.PawaPayApi
import io.dala.pawapaykotlin.network.dto.deposits.*
import io.dala.pawapaykotlin.network.dto.payouts.*
import io.dala.pawapaykotlin.network.dto.shared.*
import io.dala.pawapaykotlin.util.generateUUID
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay

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
                payer = Payer(
                    type = "MMO",
                    accountDetails = AccountDetails(
                        phoneNumber = phoneNumber,
                        provider = provider
                    )
                ),
                customerMessage = "Payment of $amount $currency"
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

    override suspend fun sendPayout(
        payoutId: String,
        amount: String,
        phoneNumber: String,
        currency: String,
        correspondent: String,
        description: String
    ): Result<PayoutResponse> {
        return try {
            val request = PayoutRequest(
                payoutId = payoutId,
                amount = amount,
                currency = currency,
                correspondent = correspondent,
                recipient = PayoutRecipient(
                    address = PayoutAddress(value = phoneNumber)
                ),
                statementDescription = description,
                customerTimestamp = kotlin.time.Clock.System.now().toString()
            )

            val response = api.initiatePayout(request)
            Result.success(response)
        } catch (e: ResponseException) {
            val errorBody = e.response.bodyAsText()
            Result.failure(Exception(errorBody))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTransactionStatus(id: String): Result<StatusResponse> {
        return try {
            val status = api.getStatus(id)
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pollTransactionStatus(
        id: String,
        type: TransactionType
    ): Result<StatusResponse> {
        val maxAttempts = 30
        var attempts = 0

        while (attempts < maxAttempts) {
            val statusResult = getTransactionStatus(id)

            statusResult.onSuccess { response ->
                val actualStatus = response.data?.status ?: response.status

                when (actualStatus) {
                    "COMPLETED" -> return Result.success(response)
                    "FAILED", "REJECTED" -> {
                        val message = response.data?.failureReason?.failureMessage ?: "Transaction failed"
                        return Result.failure(Exception(message))
                    }
                }
            }

            attempts++
            delay(4000)
        }
        return Result.failure(Exception("Transaction timed out after $maxAttempts attempts"))
    }
}