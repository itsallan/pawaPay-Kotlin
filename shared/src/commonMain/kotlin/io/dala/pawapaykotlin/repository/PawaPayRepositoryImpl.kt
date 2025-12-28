package io.dala.pawapaykotlin.repository

import io.dala.pawapaykotlin.domain.TransactionType
import io.dala.pawapaykotlin.network.PawaPayApi
import io.dala.pawapaykotlin.network.dto.deposits.DepositRequest
import io.dala.pawapaykotlin.network.dto.deposits.DepositResponse
import io.dala.pawapaykotlin.network.dto.deposits.Payer
import io.dala.pawapaykotlin.network.dto.payouts.PayoutRequest
import io.dala.pawapaykotlin.network.dto.payouts.PayoutResponse
import io.dala.pawapaykotlin.network.dto.payouts.Recipient
import io.dala.pawapaykotlin.network.dto.refund.RefundRequest
import io.dala.pawapaykotlin.network.dto.refund.RefundResponse
import io.dala.pawapaykotlin.network.dto.shared.AccountDetails
import io.dala.pawapaykotlin.network.dto.shared.StatusResponse
import io.dala.pawapaykotlin.util.generateUUID
import kotlinx.coroutines.delay

class PawaPayRepositoryImpl(
    private val api: PawaPayApi
) : PawaPayRepository {

    override suspend fun pay(
        amount: String,
        phoneNumber: String,
        currency: String,
        provider: String
    ): Result<DepositResponse> = runCatching {
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
        api.initiateDeposit(request)
    }

    override suspend fun sendPayout(
        payoutId: String,
        amount: String,
        phoneNumber: String,
        currency: String,
        correspondent: String,
        description: String
    ): Result<PayoutResponse> = runCatching {
        val request = PayoutRequest(
            payoutId = payoutId,
            amount = amount,
            currency = currency,
            recipient = Recipient(
                type = "MMO",
                accountDetails = AccountDetails(
                    phoneNumber = phoneNumber,
                    provider = correspondent
                )
            ),
        )
        api.initiatePayout(request)
    }

    override suspend fun refund(
        depositId: String,
        amount: String,
        currency: String,
        refundId: String
    ): Result<RefundResponse> = runCatching {
        api.initiateRefund(
            RefundRequest(
                refundId = refundId,
                depositId = depositId,
                amount = amount,
                currency = currency
            )
        )
    }

    override suspend fun getTransactionStatus(id: String, type: TransactionType): Result<StatusResponse> = runCatching {
        api.getStatus(id, type.toPath())
    }

    override suspend fun pollTransactionStatus(id: String, type: TransactionType): Result<StatusResponse> {
        val path = type.toPath()
        val maxAttempts = 30
        val interval = 5000L

        repeat(maxAttempts) { attempt ->
            getTransactionStatus(id, type).onSuccess { response ->
                if (response.status == "NOT_FOUND") {
                    if (attempt >= 5) {
                        return Result.failure(Exception("Transaction $id not found in $path system."))
                    }
                } else {
                    val currentStatus = response.data?.status ?: response.status

                    when (currentStatus) {
                        "COMPLETED" -> return Result.success(response)

                        "FAILED", "REJECTED" -> {
                            val error = response.data?.failureReason?.failureMessage
                                ?: "Transaction was rejected by the provider."
                            return Result.failure(Exception(error))
                        }

                        "ENQUEUED" -> { /* Provider is temporarily down, pawaPay will retry automatically */ }
                    }
                }
            }
            delay(interval)
        }

        return Result.failure(Exception("Timed out waiting for $id to reach a final status."))
    }

    private fun TransactionType.toPath() = when (this) {
        TransactionType.DEPOSIT -> "deposits"
        TransactionType.PAYOUT -> "payouts"
        TransactionType.REFUND -> "refunds"
    }
}