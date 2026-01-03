package io.dala.pawapaykotlin.repository

import io.dala.pawapaykotlin.domain.TransactionType
import io.dala.pawapaykotlin.network.dto.deposits.DepositResponse
import io.dala.pawapaykotlin.network.dto.payouts.PayoutResponse
import io.dala.pawapaykotlin.network.dto.refund.RefundResponse
import io.dala.pawapaykotlin.network.dto.shared.StatusResponse
import io.dala.pawapaykotlin.network.dto.toolkit.PredictProviderResponse
import io.dala.pawapaykotlin.network.dto.wallet.WalletBalanceResponse
import io.dala.pawapaykotlin.util.generateUUID

interface PawaPayRepository {
    suspend fun pay(
        amount: String,
        phoneNumber: String,
        currency: String = "UGX",
        provider: String = "MTN_MOMO_UGA"
    ): Result<DepositResponse>

    suspend fun sendPayout(
        payoutId: String,
        amount: String,
        phoneNumber: String,
        currency: String,
        correspondent: String,
        description: String
    ): Result<PayoutResponse>

    suspend fun refund(
        depositId: String,
        amount: String,
        currency: String,
        refundId: String = generateUUID()
    ): Result<RefundResponse>

    suspend fun getWalletBalances(country: String? = null): Result<WalletBalanceResponse>

    suspend fun predictProvider(phoneNumber: String): Result<PredictProviderResponse>

    suspend fun getTransactionStatus(id: String, type: TransactionType): Result<StatusResponse>

    suspend fun pollTransactionStatus(
        id: String,
        type: TransactionType
    ): Result<StatusResponse>
}