package io.dala.pawapaykotlin.repository

import io.dala.pawapaykotlin.models.DepositResponse
import io.dala.pawapaykotlin.models.StatusResponse

interface PawaPayRepository {
    suspend fun pay(
        amount: String,
        phoneNumber: String,
        currency: String = "UGX",
        provider: String = "MTN_MOMO_UGA"
    ): Result<DepositResponse>

    suspend fun getTransactionStatus(depositId: String): Result<StatusResponse>
    suspend fun pollDepositStatus(depositId: String): Result<StatusResponse>
}