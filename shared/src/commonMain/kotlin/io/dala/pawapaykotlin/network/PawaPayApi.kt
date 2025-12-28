package io.dala.pawapaykotlin.network

import io.dala.pawapaykotlin.network.dto.deposits.DepositRequest
import io.dala.pawapaykotlin.network.dto.deposits.DepositResponse
import io.dala.pawapaykotlin.network.dto.payouts.PayoutRequest
import io.dala.pawapaykotlin.network.dto.payouts.PayoutResponse
import io.dala.pawapaykotlin.network.dto.refund.RefundRequest
import io.dala.pawapaykotlin.network.dto.refund.RefundResponse
import io.dala.pawapaykotlin.network.dto.shared.StatusResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class PawaPayApi(private val client: HttpClient) {

    /**
     * Initiates a deposit (Collection).
     * POST https://api.sandbox.pawapay.io/v2/deposits
     */
    suspend fun initiateDeposit(request: DepositRequest): DepositResponse {
        return client.post("deposits") {
            setBody(request)
        }.body()
    }

    /**
     * Initiates a payout (Disbursement).
     * POST https://api.sandbox.pawapay.io/v2/payouts
     */
    suspend fun initiatePayout(request: PayoutRequest): PayoutResponse {
        return client.post("payouts") {
            setBody(request)
        }.body()
    }

    suspend fun initiateRefund(request: RefundRequest): RefundResponse {
        return client.post("refunds") {
            setBody(request)
        }.body()
    }

    /**
     * Fetches the current status of any transaction.
     * GET https://api.sandbox.pawapay.io/v2/{path}/{id}
     */
    suspend fun getStatus(id: String, path: String): StatusResponse {
        return client.get("$path/$id").body()
    }
}