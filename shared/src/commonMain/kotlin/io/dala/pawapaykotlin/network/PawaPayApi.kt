package io.dala.pawapaykotlin.network

import io.dala.pawapaykotlin.models.DepositRequest
import io.dala.pawapaykotlin.models.DepositResponse
import io.dala.pawapaykotlin.models.StatusResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class PawaPayApi(private val client: HttpClient) {
    suspend fun initiateDeposit(request: DepositRequest): DepositResponse {
        return client.post("deposits") {
            setBody(request)
        }.body()
    }

    suspend fun getStatus(depositId: String): StatusResponse {
        return client.get("deposits/$depositId").body()
    }
}