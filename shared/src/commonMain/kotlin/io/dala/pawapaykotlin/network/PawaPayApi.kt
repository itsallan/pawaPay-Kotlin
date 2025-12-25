package io.dala.pawapaykotlin.network

import io.dala.pawapaykotlin.network.dto.deposits.DepositRequest
import io.dala.pawapaykotlin.network.dto.deposits.DepositResponse
import io.dala.pawapaykotlin.network.dto.payouts.PayoutRequest
import io.dala.pawapaykotlin.network.dto.payouts.PayoutResponse
import io.dala.pawapaykotlin.network.dto.shared.StatusResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class PawaPayApi(private val client: HttpClient) {

    /**
     * Initiate a Mobile Money Deposit (Collection)
     */
    suspend fun initiateDeposit(request: DepositRequest): DepositResponse {
        return client.post("deposits") {
            setBody(request)
        }.body()
    }

    /**
     * Initiate a Mobile Money Payout (Disbursement)
     */
    suspend fun initiatePayout(request: PayoutRequest): PayoutResponse {
        return client.post("payouts") {
            setBody(request)
        }.body()
    }

    /**
     * Fetch status for any transaction type using a dynamic path
     */
    suspend fun getStatus(id: String, path: String = "deposits"): StatusResponse {
        return client.get("$path/$id").body()
    }
}