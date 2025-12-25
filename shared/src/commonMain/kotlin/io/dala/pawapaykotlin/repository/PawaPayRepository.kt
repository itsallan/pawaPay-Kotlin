package io.dala.pawapaykotlin.repository

import io.dala.pawapaykotlin.domain.TransactionType
import io.dala.pawapaykotlin.network.dto.deposits.DepositResponse
import io.dala.pawapaykotlin.network.dto.payouts.PayoutResponse
import io.dala.pawapaykotlin.network.dto.shared.StatusResponse

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

    suspend fun getTransactionStatus(id: String): Result<StatusResponse>

    suspend fun pollTransactionStatus(
        id: String,
        type: TransactionType
    ): Result<StatusResponse>
}