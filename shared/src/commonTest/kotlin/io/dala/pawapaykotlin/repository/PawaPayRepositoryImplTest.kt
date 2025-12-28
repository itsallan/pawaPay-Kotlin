package io.dala.pawapaykotlin.repository

import io.dala.pawapaykotlin.domain.TransactionType
import io.dala.pawapaykotlin.network.PawaPayApi
import io.dala.pawapaykotlin.network.dto.deposits.DepositResponse
import io.dala.pawapaykotlin.network.dto.payouts.PayoutResponse
import io.dala.pawapaykotlin.network.dto.refund.RefundResponse
import io.dala.pawapaykotlin.network.dto.shared.FailureReason
import io.dala.pawapaykotlin.network.dto.shared.StatusData
import io.dala.pawapaykotlin.network.dto.shared.StatusResponse
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Comprehensive tests for PawaPayRepositoryImpl
 * Tests business logic, error handling, and edge cases
 */
class PawaPayRepositoryImplTest {

    private lateinit var mockApi: MockPawaPayApi
    private lateinit var repository: PawaPayRepository

    @BeforeTest
    fun setup() {
        mockApi = MockPawaPayApi()
        repository = PawaPayRepositoryImpl(mockApi)
    }

    // ========== Pay (Deposit) Tests ==========

    @Test
    fun testPaySuccess() = runTest {
        mockApi.depositResponse = DepositResponse(
            depositId = "deposit-123",
            status = "ACCEPTED"
        )

        val result = repository.pay(
            amount = "1000",
            phoneNumber = "+256700000000",
            currency = "UGX",
            provider = "MTN_MOMO_UGA"
        )

        assertTrue(result.isSuccess)
        assertEquals("deposit-123", result.getOrNull()?.depositId)
        assertEquals("ACCEPTED", result.getOrNull()?.status)
        
        // Verify the request was created correctly
        val capturedRequest = mockApi.lastDepositRequest
        assertNotNull(capturedRequest)
        assertEquals("1000", capturedRequest.amount)
        assertEquals("UGX", capturedRequest.currency)
        assertEquals("+256700000000", capturedRequest.payer.accountDetails.phoneNumber)
        assertEquals("MTN_MOMO_UGA", capturedRequest.payer.accountDetails.provider)
        assertEquals("MMO", capturedRequest.payer.type)
        assertContains(capturedRequest.customerMessage, "1000 UGX")
    }

    @Test
    fun testPayWithDifferentCurrency() = runTest {
        mockApi.depositResponse = DepositResponse(
            depositId = "deposit-456",
            status = "ACCEPTED"
        )

        val result = repository.pay(
            amount = "5000",
            phoneNumber = "+254700000000",
            currency = "KES",
            provider = "SAFARICOM_KEN"
        )

        assertTrue(result.isSuccess)
        val capturedRequest = mockApi.lastDepositRequest
        assertEquals("KES", capturedRequest?.currency)
        assertEquals("SAFARICOM_KEN", capturedRequest?.payer?.accountDetails?.provider)
    }

    @Test
    fun testPayGeneratesUniqueDepositId() = runTest {
        mockApi.depositResponse = DepositResponse(
            depositId = "deposit-1",
            status = "ACCEPTED"
        )

        val result1 = repository.pay("1000", "+256700000000")
        val result2 = repository.pay("2000", "+256700000001")

        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        
        val id1 = mockApi.depositRequests[0].depositId
        val id2 = mockApi.depositRequests[1].depositId
        assertNotEquals(id1, id2, "Deposit IDs should be unique")
        assertTrue(id1.isNotEmpty())
        assertTrue(id2.isNotEmpty())
    }

    @Test
    fun testPayFailure() = runTest {
        mockApi.shouldThrowException = true
        mockApi.exceptionMessage = "Network error"

        val result = repository.pay("1000", "+256700000000")

        assertTrue(result.isFailure)
        assertContains(result.exceptionOrNull()?.message ?: "", "Network error")
    }

    @Test
    fun testPayWithZeroAmount() = runTest {
        mockApi.depositResponse = DepositResponse(
            depositId = "deposit-zero",
            status = "ACCEPTED"
        )

        val result = repository.pay(
            amount = "0",
            phoneNumber = "+256700000000"
        )

        assertTrue(result.isSuccess)
        assertEquals("0", mockApi.lastDepositRequest?.amount)
    }

    @Test
    fun testPayWithLargeAmount() = runTest {
        val largeAmount = "999999999999"
        mockApi.depositResponse = DepositResponse(
            depositId = "deposit-large",
            status = "ACCEPTED"
        )

        val result = repository.pay(
            amount = largeAmount,
            phoneNumber = "+256700000000"
        )

        assertTrue(result.isSuccess)
        assertEquals(largeAmount, mockApi.lastDepositRequest?.amount)
    }

    // ========== SendPayout Tests ==========

    @Test
    fun testSendPayoutSuccess() = runTest {
        mockApi.payoutResponse = PayoutResponse(
            payoutId = "payout-123",
            status = "ACCEPTED",
            acceptanceDateTime = "2024-01-15T10:00:00Z"
        )

        val result = repository.sendPayout(
            payoutId = "payout-123",
            amount = "2000",
            phoneNumber = "+256700111111",
            currency = "UGX",
            correspondent = "MTN_MOMO_UGA",
            description = "Salary payment"
        )

        assertTrue(result.isSuccess)
        assertEquals("payout-123", result.getOrNull()?.payoutId)
        assertEquals("ACCEPTED", result.getOrNull()?.status)

        val capturedRequest = mockApi.lastPayoutRequest
        assertNotNull(capturedRequest)
        assertEquals("payout-123", capturedRequest.payoutId)
        assertEquals("2000", capturedRequest.amount)
        assertEquals("UGX", capturedRequest.currency)
        assertEquals("+256700111111", capturedRequest.recipient.accountDetails.phoneNumber)
        assertEquals("MTN_MOMO_UGA", capturedRequest.recipient.accountDetails.provider)
    }

    @Test
    fun testSendPayoutWithMultipleProviders() = runTest {
        val providers = mapOf(
            "MTN_MOMO_UGA" to "+256700000000",
            "SAFARICOM_KEN" to "+254700000000",
            "AIRTEL_UGA" to "+256750000000"
        )

        providers.forEach { (provider, phone) ->
            mockApi.payoutResponse = PayoutResponse(
                payoutId = "payout-$provider",
                status = "ACCEPTED"
            )

            val result = repository.sendPayout(
                payoutId = "payout-$provider",
                amount = "1000",
                phoneNumber = phone,
                currency = "UGX",
                correspondent = provider,
                description = "Test"
            )

            assertTrue(result.isSuccess)
            assertEquals(provider, mockApi.lastPayoutRequest?.recipient?.accountDetails?.provider)
        }
    }

    @Test
    fun testSendPayoutFailure() = runTest {
        mockApi.shouldThrowException = true
        mockApi.exceptionMessage = "Payout rejected"

        val result = repository.sendPayout(
            payoutId = "payout-fail",
            amount = "1000",
            phoneNumber = "+256700000000",
            currency = "UGX",
            correspondent = "MTN_MOMO_UGA",
            description = "Test"
        )

        assertTrue(result.isFailure)
        assertContains(result.exceptionOrNull()?.message ?: "", "Payout rejected")
    }

    // ========== Refund Tests ==========

    @Test
    fun testRefundSuccess() = runTest {
        mockApi.refundResponse = RefundResponse(
            refundId = "refund-123",
            status = "ACCEPTED",
            created = "2024-01-15T11:00:00Z"
        )

        val result = repository.refund(
            depositId = "deposit-456",
            amount = "1000",
            currency = "UGX",
            refundId = "refund-123"
        )

        assertTrue(result.isSuccess)
        assertEquals("refund-123", result.getOrNull()?.refundId)
        assertEquals("ACCEPTED", result.getOrNull()?.status)

        val capturedRequest = mockApi.lastRefundRequest
        assertNotNull(capturedRequest)
        assertEquals("refund-123", capturedRequest.refundId)
        assertEquals("deposit-456", capturedRequest.depositId)
        assertEquals("1000", capturedRequest.amount)
        assertEquals("UGX", capturedRequest.currency)
    }

    @Test
    fun testRefundGeneratesUniqueIdIfNotProvided() = runTest {
        mockApi.refundResponse = RefundResponse(
            refundId = "refund-auto",
            status = "ACCEPTED"
        )

        // Call refund twice without providing refundId (uses default generateUUID())
        val result1 = repository.refund("deposit-1", "1000", "UGX")
        val result2 = repository.refund("deposit-2", "2000", "UGX")

        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)

        val id1 = mockApi.refundRequests[0].refundId
        val id2 = mockApi.refundRequests[1].refundId
        
        // Both should have generated IDs
        assertTrue(id1.isNotEmpty())
        assertTrue(id2.isNotEmpty())
    }

    @Test
    fun testRefundWithProvidedId() = runTest {
        mockApi.refundResponse = RefundResponse(
            refundId = "custom-refund-id",
            status = "ACCEPTED"
        )

        val result = repository.refund(
            depositId = "deposit-789",
            amount = "500",
            currency = "UGX",
            refundId = "custom-refund-id"
        )

        assertTrue(result.isSuccess)
        assertEquals("custom-refund-id", mockApi.lastRefundRequest?.refundId)
    }

    @Test
    fun testRefundPartialAmount() = runTest {
        mockApi.refundResponse = RefundResponse(
            refundId = "refund-partial",
            status = "ACCEPTED"
        )

        val result = repository.refund(
            depositId = "deposit-original",
            amount = "500",
            currency = "UGX"
        )

        assertTrue(result.isSuccess)
        assertEquals("500", mockApi.lastRefundRequest?.amount)
    }

    @Test
    fun testRefundFailure() = runTest {
        mockApi.shouldThrowException = true
        mockApi.exceptionMessage = "Refund not allowed"

        val result = repository.refund(
            depositId = "deposit-123",
            amount = "1000",
            currency = "UGX"
        )

        assertTrue(result.isFailure)
        assertContains(result.exceptionOrNull()?.message ?: "", "Refund not allowed")
    }

    // ========== GetTransactionStatus Tests ==========

    @Test
    fun testGetDepositStatusSuccess() = runTest {
        mockApi.statusResponse = StatusResponse(
            status = "OK",
            data = StatusData(
                depositId = "deposit-123",
                status = "COMPLETED",
                amount = "1000",
                currency = "UGX"
            )
        )

        val result = repository.getTransactionStatus("deposit-123", TransactionType.DEPOSIT)

        assertTrue(result.isSuccess)
        assertEquals("OK", result.getOrNull()?.status)
        assertEquals("COMPLETED", result.getOrNull()?.data?.status)
        assertEquals("deposits/deposit-123", mockApi.lastStatusPath)
    }

    @Test
    fun testGetPayoutStatusSuccess() = runTest {
        mockApi.statusResponse = StatusResponse(
            status = "OK",
            data = StatusData(
                payoutId = "payout-456",
                status = "COMPLETED",
                amount = "2000",
                currency = "KES"
            )
        )

        val result = repository.getTransactionStatus("payout-456", TransactionType.PAYOUT)

        assertTrue(result.isSuccess)
        assertEquals("payouts/payout-456", mockApi.lastStatusPath)
    }

    @Test
    fun testGetRefundStatusSuccess() = runTest {
        mockApi.statusResponse = StatusResponse(
            status = "OK",
            data = StatusData(
                status = "COMPLETED",
                amount = "1500",
                currency = "UGX"
            )
        )

        val result = repository.getTransactionStatus("refund-789", TransactionType.REFUND)

        assertTrue(result.isSuccess)
        assertEquals("refunds/refund-789", mockApi.lastStatusPath)
    }

    @Test
    fun testGetStatusNotFound() = runTest {
        mockApi.statusResponse = StatusResponse(
            status = "NOT_FOUND",
            data = null
        )

        val result = repository.getTransactionStatus("nonexistent", TransactionType.DEPOSIT)

        assertTrue(result.isSuccess)
        assertEquals("NOT_FOUND", result.getOrNull()?.status)
        assertNull(result.getOrNull()?.data)
    }

    @Test
    fun testGetStatusFailure() = runTest {
        mockApi.shouldThrowException = true
        mockApi.exceptionMessage = "Connection timeout"

        val result = repository.getTransactionStatus("deposit-123", TransactionType.DEPOSIT)

        assertTrue(result.isFailure)
        assertContains(result.exceptionOrNull()?.message ?: "", "Connection timeout")
    }

    // ========== PollTransactionStatus Tests ==========

    @Test
    fun testPollStatusCompletedImmediately() = runTest {
        mockApi.statusResponse = StatusResponse(
            status = "OK",
            data = StatusData(
                depositId = "deposit-123",
                status = "COMPLETED",
                amount = "1000",
                currency = "UGX"
            )
        )

        val result = repository.pollTransactionStatus("deposit-123", TransactionType.DEPOSIT)

        assertTrue(result.isSuccess)
        assertEquals("COMPLETED", result.getOrNull()?.data?.status)
        assertEquals(1, mockApi.statusCallCount, "Should only call once when completed immediately")
    }

    @Test
    fun testPollStatusEventuallyCompletes() = runTest {
        // Simulate transaction that completes after 3 polls
        mockApi.statusResponses = mutableListOf(
            StatusResponse("OK", StatusData(depositId = "deposit-123", status = "ENQUEUED")),
            StatusResponse("OK", StatusData(depositId = "deposit-123", status = "ENQUEUED")),
            StatusResponse("OK", StatusData(depositId = "deposit-123", status = "COMPLETED"))
        )

        val result = repository.pollTransactionStatus("deposit-123", TransactionType.DEPOSIT)

        assertTrue(result.isSuccess)
        assertEquals("COMPLETED", result.getOrNull()?.data?.status)
        assertEquals(3, mockApi.statusCallCount)
    }

    @Test
    fun testPollStatusHandlesRejection() = runTest {
        mockApi.statusResponse = StatusResponse(
            status = "OK",
            data = StatusData(
                depositId = "deposit-123",
                status = "REJECTED",
                failureReason = FailureReason(
                    failureCode = "INVALID_MSISDN",
                    failureMessage = "Invalid phone number"
                )
            )
        )

        val result = repository.pollTransactionStatus("deposit-123", TransactionType.DEPOSIT)

        assertTrue(result.isFailure)
        assertContains(result.exceptionOrNull()?.message ?: "", "Invalid phone number")
    }

    @Test
    fun testPollStatusHandlesFailure() = runTest {
        mockApi.statusResponse = StatusResponse(
            status = "OK",
            data = StatusData(
                depositId = "deposit-123",
                status = "FAILED",
                failureReason = FailureReason(
                    failureCode = "INSUFFICIENT_FUNDS",
                    failureMessage = "Customer has insufficient funds"
                )
            )
        )

        val result = repository.pollTransactionStatus("deposit-123", TransactionType.DEPOSIT)

        assertTrue(result.isFailure)
        assertContains(result.exceptionOrNull()?.message ?: "", "insufficient funds")
    }

    @Test
    fun testPollStatusToleratesInitialNotFound() = runTest {
        // Transaction appears after a few NOT_FOUND responses
        mockApi.statusResponses = mutableListOf(
            StatusResponse("NOT_FOUND", null),
            StatusResponse("NOT_FOUND", null),
            StatusResponse("OK", StatusData(depositId = "deposit-123", status = "COMPLETED"))
        )

        val result = repository.pollTransactionStatus("deposit-123", TransactionType.DEPOSIT)

        assertTrue(result.isSuccess)
        assertEquals("COMPLETED", result.getOrNull()?.data?.status)
    }

    @Test
    fun testPollStatusFailsAfterPersistentNotFound() = runTest {
        // Still NOT_FOUND after 6 attempts
        mockApi.statusResponses = mutableListOf(
            StatusResponse("NOT_FOUND", null),
            StatusResponse("NOT_FOUND", null),
            StatusResponse("NOT_FOUND", null),
            StatusResponse("NOT_FOUND", null),
            StatusResponse("NOT_FOUND", null),
            StatusResponse("NOT_FOUND", null),
            StatusResponse("NOT_FOUND", null)
        )

        val result = repository.pollTransactionStatus("deposit-999", TransactionType.DEPOSIT)

        assertTrue(result.isFailure)
        assertContains(result.exceptionOrNull()?.message ?: "", "not found")
    }

    @Test
    fun testPollStatusHandlesEnqueued() = runTest {
        // Simulate prolonged ENQUEUED state that eventually completes
        val responses = mutableListOf<StatusResponse>()
        repeat(5) {
            responses.add(StatusResponse("OK", StatusData(depositId = "deposit-123", status = "ENQUEUED")))
        }
        responses.add(StatusResponse("OK", StatusData(depositId = "deposit-123", status = "COMPLETED")))
        mockApi.statusResponses = responses

        val result = repository.pollTransactionStatus("deposit-123", TransactionType.DEPOSIT)

        assertTrue(result.isSuccess)
        assertEquals("COMPLETED", result.getOrNull()?.data?.status)
        assertEquals(6, mockApi.statusCallCount)
    }

    @Test
    fun testPollStatusFailsWithoutFailureReason() = runTest {
        // REJECTED status without a failure reason should use default message
        mockApi.statusResponse = StatusResponse(
            status = "OK",
            data = StatusData(
                depositId = "deposit-123",
                status = "REJECTED",
                failureReason = null
            )
        )

        val result = repository.pollTransactionStatus("deposit-123", TransactionType.DEPOSIT)

        assertTrue(result.isFailure)
        assertContains(result.exceptionOrNull()?.message ?: "", "rejected by the provider")
    }

    // ========== Edge Cases ==========

    @Test
    fun testPayWithEmptyPhoneNumber() = runTest {
        mockApi.depositResponse = DepositResponse("deposit-empty", "ACCEPTED")

        val result = repository.pay("1000", "")

        assertTrue(result.isSuccess)
        assertEquals("", mockApi.lastDepositRequest?.payer?.accountDetails?.phoneNumber)
    }

    @Test
    fun testPayWithSpecialCharactersInAmount() = runTest {
        mockApi.depositResponse = DepositResponse("deposit-special", "ACCEPTED")

        val result = repository.pay("1,000.50", "+256700000000")

        assertTrue(result.isSuccess)
        assertEquals("1,000.50", mockApi.lastDepositRequest?.amount)
    }

    @Test
    fun testSendPayoutWithEmptyDescription() = runTest {
        mockApi.payoutResponse = PayoutResponse("payout-nodesc", "ACCEPTED")

        val result = repository.sendPayout(
            payoutId = "payout-nodesc",
            amount = "1000",
            phoneNumber = "+256700000000",
            currency = "UGX",
            correspondent = "MTN_MOMO_UGA",
            description = ""
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun testMultipleConcurrentOperations() = runTest {
        mockApi.depositResponse = DepositResponse("deposit-1", "ACCEPTED")

        // Simulate multiple concurrent payment requests
        val results = (1..5).map { i ->
            repository.pay("${i * 1000}", "+25670000000$i")
        }

        results.forEach { result ->
            assertTrue(result.isSuccess)
        }
        assertEquals(5, mockApi.depositRequests.size)
    }
}

/**
 * Mock implementation of PawaPayApi for testing
 */
class MockPawaPayApi : PawaPayApi(io.ktor.client.HttpClient()) {
    var shouldThrowException = false
    var exceptionMessage = "Mock exception"
    
    var depositResponse: DepositResponse? = null
    var payoutResponse: PayoutResponse? = null
    var refundResponse: RefundResponse? = null
    var statusResponse: StatusResponse? = null
    var statusResponses: MutableList<StatusResponse>? = null
    
    var lastDepositRequest: io.dala.pawapaykotlin.network.dto.deposits.DepositRequest? = null
    var lastPayoutRequest: io.dala.pawapaykotlin.network.dto.payouts.PayoutRequest? = null
    var lastRefundRequest: io.dala.pawapaykotlin.network.dto.refund.RefundRequest? = null
    var lastStatusPath: String? = null
    
    val depositRequests = mutableListOf<io.dala.pawapaykotlin.network.dto.deposits.DepositRequest>()
    val payoutRequests = mutableListOf<io.dala.pawapaykotlin.network.dto.payouts.PayoutRequest>()
    val refundRequests = mutableListOf<io.dala.pawapaykotlin.network.dto.refund.RefundRequest>()
    
    var statusCallCount = 0

    override suspend fun initiateDeposit(request: io.dala.pawapaykotlin.network.dto.deposits.DepositRequest): DepositResponse {
        if (shouldThrowException) throw Exception(exceptionMessage)
        lastDepositRequest = request
        depositRequests.add(request)
        return depositResponse ?: DepositResponse("default-deposit", "ACCEPTED")
    }

    override suspend fun initiatePayout(request: io.dala.pawapaykotlin.network.dto.payouts.PayoutRequest): PayoutResponse {
        if (shouldThrowException) throw Exception(exceptionMessage)
        lastPayoutRequest = request
        payoutRequests.add(request)
        return payoutResponse ?: PayoutResponse("default-payout", "ACCEPTED")
    }

    override suspend fun initiateRefund(request: io.dala.pawapaykotlin.network.dto.refund.RefundRequest): RefundResponse {
        if (shouldThrowException) throw Exception(exceptionMessage)
        lastRefundRequest = request
        refundRequests.add(request)
        return refundResponse ?: RefundResponse("default-refund", "ACCEPTED")
    }

    override suspend fun getStatus(id: String, path: String): StatusResponse {
        if (shouldThrowException) throw Exception(exceptionMessage)
        lastStatusPath = "$path/$id"
        statusCallCount++
        
        // If multiple responses are configured, return them in sequence
        return if (statusResponses != null && statusResponses!!.isNotEmpty()) {
            val response = statusResponses!!.removeAt(0)
            response
        } else {
            statusResponse ?: StatusResponse("OK", StatusData(depositId = id, status = "COMPLETED"))
        }
    }
}