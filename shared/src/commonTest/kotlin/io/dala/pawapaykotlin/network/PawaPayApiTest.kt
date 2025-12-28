package io.dala.pawapaykotlin.network

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
import io.dala.pawapaykotlin.network.dto.shared.StatusData
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlin.test.*

/**
 * Tests for PawaPayApi - verifies HTTP client interactions and response handling
 */
class PawaPayApiTest {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
        encodeDefaults = true
    }

    // ========== Deposit API Tests ==========

    @Test
    fun testInitiateDepositSuccess() = runTest {
        val expectedResponse = DepositResponse(
            depositId = "deposit-123",
            status = "ACCEPTED",
            reason = null
        )

        val mockEngine = MockEngine { request ->
            assertEquals("/deposits", request.url.encodedPath)
            assertEquals("POST", request.method.value)
            
            respond(
                content = json.encodeToString(expectedResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(mockEngine)
        val api = PawaPayApi(client)

        val request = DepositRequest(
            depositId = "deposit-123",
            amount = "1000",
            currency = "UGX",
            payer = Payer(
                type = "MMO",
                accountDetails = AccountDetails(
                    phoneNumber = "+256700000000",
                    provider = "MTN_MOMO_UGA"
                )
            ),
            customerMessage = "Test payment"
        )

        val response = api.initiateDeposit(request)

        assertEquals("deposit-123", response.depositId)
        assertEquals("ACCEPTED", response.status)
        assertNull(response.reason)
    }

    @Test
    fun testInitiateDepositWithRejection() = runTest {
        val expectedResponse = DepositResponse(
            depositId = "deposit-456",
            status = "REJECTED",
            reason = "Insufficient funds"
        )

        val mockEngine = MockEngine { _ ->
            respond(
                content = json.encodeToString(expectedResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(mockEngine)
        val api = PawaPayApi(client)

        val request = DepositRequest(
            depositId = "deposit-456",
            amount = "10000",
            currency = "UGX",
            payer = Payer(
                type = "MMO",
                accountDetails = AccountDetails(
                    phoneNumber = "+256700000000",
                    provider = "MTN_MOMO_UGA"
                )
            ),
            customerMessage = "Test"
        )

        val response = api.initiateDeposit(request)

        assertEquals("REJECTED", response.status)
        assertEquals("Insufficient funds", response.reason)
    }

    // ========== Payout API Tests ==========

    @Test
    fun testInitiatePayoutSuccess() = runTest {
        val expectedResponse = PayoutResponse(
            payoutId = "payout-789",
            status = "ACCEPTED",
            acceptanceDateTime = "2024-01-15T10:00:00Z"
        )

        val mockEngine = MockEngine { request ->
            assertEquals("/payouts", request.url.encodedPath)
            assertEquals("POST", request.method.value)
            
            respond(
                content = json.encodeToString(expectedResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(mockEngine)
        val api = PawaPayApi(client)

        val request = PayoutRequest(
            payoutId = "payout-789",
            amount = "5000",
            currency = "UGX",
            recipient = Recipient(
                accountDetails = AccountDetails(
                    phoneNumber = "+256700111111",
                    provider = "MTN_MOMO_UGA"
                )
            )
        )

        val response = api.initiatePayout(request)

        assertEquals("payout-789", response.payoutId)
        assertEquals("ACCEPTED", response.status)
        assertEquals("2024-01-15T10:00:00Z", response.acceptanceDateTime)
    }

    @Test
    fun testInitiatePayoutWithoutDateTime() = runTest {
        val expectedResponse = PayoutResponse(
            payoutId = "payout-999",
            status = "ACCEPTED",
            acceptanceDateTime = null
        )

        val mockEngine = MockEngine { _ ->
            respond(
                content = json.encodeToString(expectedResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(mockEngine)
        val api = PawaPayApi(client)

        val request = PayoutRequest(
            payoutId = "payout-999",
            amount = "2000",
            currency = "KES",
            recipient = Recipient(
                accountDetails = AccountDetails(
                    phoneNumber = "+254700000000",
                    provider = "SAFARICOM_KEN"
                )
            )
        )

        val response = api.initiatePayout(request)

        assertEquals("payout-999", response.payoutId)
        assertNull(response.acceptanceDateTime)
    }

    // ========== Refund API Tests ==========

    @Test
    fun testInitiateRefundSuccess() = runTest {
        val expectedResponse = RefundResponse(
            refundId = "refund-111",
            status = "ACCEPTED",
            created = "2024-01-15T11:00:00Z"
        )

        val mockEngine = MockEngine { request ->
            assertEquals("/refunds", request.url.encodedPath)
            assertEquals("POST", request.method.value)
            
            respond(
                content = json.encodeToString(expectedResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(mockEngine)
        val api = PawaPayApi(client)

        val request = RefundRequest(
            refundId = "refund-111",
            depositId = "deposit-222",
            amount = "1500",
            currency = "UGX"
        )

        val response = api.initiateRefund(request)

        assertEquals("refund-111", response.refundId)
        assertEquals("ACCEPTED", response.status)
        assertEquals("2024-01-15T11:00:00Z", response.created)
    }

    @Test
    fun testInitiateRefundWithoutCreatedDate() = runTest {
        val expectedResponse = RefundResponse(
            refundId = "refund-333",
            status = "COMPLETED",
            created = null
        )

        val mockEngine = MockEngine { _ ->
            respond(
                content = json.encodeToString(expectedResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(mockEngine)
        val api = PawaPayApi(client)

        val request = RefundRequest(
            refundId = "refund-333",
            depositId = "deposit-444",
            amount = "2500",
            currency = "KES"
        )

        val response = api.initiateRefund(request)

        assertEquals("refund-333", response.refundId)
        assertNull(response.created)
    }

    // ========== Status API Tests ==========

    @Test
    fun testGetDepositStatus() = runTest {
        val expectedResponse = StatusResponse(
            status = "OK",
            data = StatusData(
                depositId = "deposit-555",
                status = "COMPLETED",
                amount = "3000",
                currency = "UGX"
            )
        )

        val mockEngine = MockEngine { request ->
            assertEquals("/deposits/deposit-555", request.url.encodedPath)
            assertEquals("GET", request.method.value)
            
            respond(
                content = json.encodeToString(expectedResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(mockEngine)
        val api = PawaPayApi(client)

        val response = api.getStatus("deposit-555", "deposits")

        assertEquals("OK", response.status)
        assertNotNull(response.data)
        assertEquals("deposit-555", response.data?.depositId)
        assertEquals("COMPLETED", response.data?.status)
    }

    @Test
    fun testGetPayoutStatus() = runTest {
        val expectedResponse = StatusResponse(
            status = "OK",
            data = StatusData(
                payoutId = "payout-666",
                status = "ENQUEUED",
                amount = "4000",
                currency = "KES"
            )
        )

        val mockEngine = MockEngine { request ->
            assertEquals("/payouts/payout-666", request.url.encodedPath)
            
            respond(
                content = json.encodeToString(expectedResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(mockEngine)
        val api = PawaPayApi(client)

        val response = api.getStatus("payout-666", "payouts")

        assertEquals("OK", response.status)
        assertEquals("payout-666", response.data?.payoutId)
        assertEquals("ENQUEUED", response.data?.status)
    }

    @Test
    fun testGetRefundStatus() = runTest {
        val expectedResponse = StatusResponse(
            status = "OK",
            data = StatusData(
                status = "FAILED",
                amount = "2000",
                currency = "UGX"
            )
        )

        val mockEngine = MockEngine { request ->
            assertEquals("/refunds/refund-777", request.url.encodedPath)
            
            respond(
                content = json.encodeToString(expectedResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(mockEngine)
        val api = PawaPayApi(client)

        val response = api.getStatus("refund-777", "refunds")

        assertEquals("OK", response.status)
        assertEquals("FAILED", response.data?.status)
    }

    @Test
    fun testGetStatusNotFound() = runTest {
        val expectedResponse = StatusResponse(
            status = "NOT_FOUND",
            data = null
        )

        val mockEngine = MockEngine { _ ->
            respond(
                content = json.encodeToString(expectedResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(mockEngine)
        val api = PawaPayApi(client)

        val response = api.getStatus("nonexistent", "deposits")

        assertEquals("NOT_FOUND", response.status)
        assertNull(response.data)
    }

    // ========== Error Handling Tests ==========

    @Test
    fun testDepositWithServerError() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"error": "Internal server error"}""",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(mockEngine)
        val api = PawaPayApi(client)

        val request = DepositRequest(
            depositId = "deposit-error",
            amount = "1000",
            currency = "UGX",
            payer = Payer(
                type = "MMO",
                accountDetails = AccountDetails(
                    phoneNumber = "+256700000000",
                    provider = "MTN_MOMO_UGA"
                )
            ),
            customerMessage = "Test"
        )

        assertFailsWith<Exception> {
            api.initiateDeposit(request)
        }
    }

    @Test
    fun testPayoutWithUnauthorized() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"error": "Unauthorized"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(mockEngine)
        val api = PawaPayApi(client)

        val request = PayoutRequest(
            payoutId = "payout-unauth",
            amount = "1000",
            currency = "UGX",
            recipient = Recipient(
                accountDetails = AccountDetails(
                    phoneNumber = "+256700000000",
                    provider = "MTN_MOMO_UGA"
                )
            )
        )

        assertFailsWith<Exception> {
            api.initiatePayout(request)
        }
    }

    @Test
    fun testStatusWithBadRequest() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"error": "Bad request"}""",
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = createTestClient(mockEngine)
        val api = PawaPayApi(client)

        assertFailsWith<Exception> {
            api.getStatus("invalid-id", "deposits")
        }
    }

    // ========== Helper Functions ==========

    private fun createTestClient(mockEngine: MockEngine): HttpClient {
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }
    }
}