package io.dala.pawapaykotlin

import io.dala.pawapaykotlin.domain.TransactionType
import io.dala.pawapaykotlin.network.dto.deposits.*
import io.dala.pawapaykotlin.network.dto.payouts.*
import io.dala.pawapaykotlin.network.dto.refund.*
import io.dala.pawapaykotlin.network.dto.shared.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.test.*

/**
 * Comprehensive tests for DTO serialization/deserialization and domain models
 */
class SharedCommonTest {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
        encodeDefaults = true
    }

    // ========== DepositRequest Tests ==========
    
    @Test
    fun testDepositRequestSerialization() {
        val request = DepositRequest(
            depositId = "test-deposit-123",
            amount = "1000",
            currency = "UGX",
            payer = Payer(
                type = "MMO",
                accountDetails = AccountDetails(
                    phoneNumber = "+256700000000",
                    provider = "MTN_MOMO_UGA"
                )
            ),
            customerMessage = "Payment of 1000 UGX"
        )

        val jsonString = json.encodeToString(request)
        assertContains(jsonString, "test-deposit-123")
        assertContains(jsonString, "1000")
        assertContains(jsonString, "UGX")
        assertContains(jsonString, "+256700000000")
        assertContains(jsonString, "MTN_MOMO_UGA")
    }

    @Test
    fun testDepositRequestDeserialization() {
        val jsonString = """
            {
                "depositId": "test-deposit-456",
                "amount": "5000",
                "currency": "KES",
                "payer": {
                    "type": "MMO",
                    "accountDetails": {
                        "phoneNumber": "+254700000000",
                        "provider": "SAFARICOM_KEN"
                    }
                },
                "customerMessage": "Test payment"
            }
        """.trimIndent()

        val request = json.decodeFromString<DepositRequest>(jsonString)
        assertEquals("test-deposit-456", request.depositId)
        assertEquals("5000", request.amount)
        assertEquals("KES", request.currency)
        assertEquals("+254700000000", request.payer.accountDetails.phoneNumber)
    }

    @Test
    fun testDepositRequestWithDifferentProviders() {
        val providers = listOf(
            "MTN_MOMO_UGA",
            "SAFARICOM_KEN",
            "AIRTEL_UGA",
            "VODACOM_TZA"
        )

        providers.forEach { provider ->
            val request = DepositRequest(
                depositId = "deposit-$provider",
                amount = "1000",
                currency = "UGX",
                payer = Payer(
                    type = "MMO",
                    accountDetails = AccountDetails(
                        phoneNumber = "+256700000000",
                        provider = provider
                    )
                ),
                customerMessage = "Test"
            )
            assertEquals(provider, request.payer.accountDetails.provider)
        }
    }

    // ========== DepositResponse Tests ==========

    @Test
    fun testDepositResponseSerialization() {
        val response = DepositResponse(
            depositId = "test-deposit-789",
            status = "ACCEPTED",
            reason = null
        )

        val jsonString = json.encodeToString(response)
        assertContains(jsonString, "test-deposit-789")
        assertContains(jsonString, "ACCEPTED")
    }

    @Test
    fun testDepositResponseWithReason() {
        val response = DepositResponse(
            depositId = "test-deposit-999",
            status = "REJECTED",
            reason = "Insufficient funds"
        )

        val jsonString = json.encodeToString(response)
        assertContains(jsonString, "REJECTED")
        assertContains(jsonString, "Insufficient funds")
    }

    @Test
    fun testDepositResponseDeserialization() {
        val jsonString = """
            {
                "depositId": "resp-123",
                "status": "COMPLETED"
            }
        """.trimIndent()

        val response = json.decodeFromString<DepositResponse>(jsonString)
        assertEquals("resp-123", response.depositId)
        assertEquals("COMPLETED", response.status)
        assertNull(response.reason)
    }

    // ========== PayoutRequest Tests ==========

    @Test
    fun testPayoutRequestSerialization() {
        val request = PayoutRequest(
            payoutId = "payout-123",
            amount = "2000",
            currency = "UGX",
            recipient = Recipient(
                type = "MMO",
                accountDetails = AccountDetails(
                    phoneNumber = "+256700111111",
                    provider = "MTN_MOMO_UGA"
                )
            )
        )

        val jsonString = json.encodeToString(request)
        assertContains(jsonString, "payout-123")
        assertContains(jsonString, "2000")
        assertContains(jsonString, "+256700111111")
    }

    @Test
    fun testPayoutRequestDeserialization() {
        val jsonString = """
            {
                "payoutId": "payout-456",
                "amount": "3000",
                "currency": "KES",
                "recipient": {
                    "type": "MMO",
                    "accountDetails": {
                        "phoneNumber": "+254700111111",
                        "provider": "SAFARICOM_KEN"
                    }
                }
            }
        """.trimIndent()

        val request = json.decodeFromString<PayoutRequest>(jsonString)
        assertEquals("payout-456", request.payoutId)
        assertEquals("3000", request.amount)
        assertEquals("KES", request.currency)
    }

    @Test
    fun testPayoutRequestWithDefaultRecipientType() {
        val recipient = Recipient(
            accountDetails = AccountDetails(
                phoneNumber = "+256700000000",
                provider = "MTN_MOMO_UGA"
            )
        )
        assertEquals("MMO", recipient.type)
    }

    // ========== PayoutResponse Tests ==========

    @Test
    fun testPayoutResponseSerialization() {
        val response = PayoutResponse(
            payoutId = "payout-789",
            status = "ACCEPTED",
            acceptanceDateTime = "2024-01-15T10:30:00Z"
        )

        val jsonString = json.encodeToString(response)
        assertContains(jsonString, "payout-789")
        assertContains(jsonString, "ACCEPTED")
        assertContains(jsonString, "2024-01-15T10:30:00Z")
    }

    @Test
    fun testPayoutResponseDeserialization() {
        val jsonString = """
            {
                "payoutId": "payout-999",
                "status": "COMPLETED"
            }
        """.trimIndent()

        val response = json.decodeFromString<PayoutResponse>(jsonString)
        assertEquals("payout-999", response.payoutId)
        assertEquals("COMPLETED", response.status)
        assertNull(response.acceptanceDateTime)
    }

    // ========== RefundRequest Tests ==========

    @Test
    fun testRefundRequestSerialization() {
        val request = RefundRequest(
            refundId = "refund-123",
            depositId = "deposit-456",
            amount = "1500",
            currency = "UGX"
        )

        val jsonString = json.encodeToString(request)
        assertContains(jsonString, "refund-123")
        assertContains(jsonString, "deposit-456")
        assertContains(jsonString, "1500")
        assertContains(jsonString, "UGX")
    }

    @Test
    fun testRefundRequestDeserialization() {
        val jsonString = """
            {
                "refundId": "refund-789",
                "depositId": "deposit-999",
                "amount": "2500",
                "currency": "KES"
            }
        """.trimIndent()

        val request = json.decodeFromString<RefundRequest>(jsonString)
        assertEquals("refund-789", request.refundId)
        assertEquals("deposit-999", request.depositId)
        assertEquals("2500", request.amount)
        assertEquals("KES", request.currency)
    }

    // ========== RefundResponse Tests ==========

    @Test
    fun testRefundResponseSerialization() {
        val response = RefundResponse(
            refundId = "refund-111",
            status = "ACCEPTED",
            created = "2024-01-15T11:00:00Z"
        )

        val jsonString = json.encodeToString(response)
        assertContains(jsonString, "refund-111")
        assertContains(jsonString, "ACCEPTED")
    }

    @Test
    fun testRefundResponseDeserialization() {
        val jsonString = """
            {
                "refundId": "refund-222",
                "status": "COMPLETED"
            }
        """.trimIndent()

        val response = json.decodeFromString<RefundResponse>(jsonString)
        assertEquals("refund-222", response.refundId)
        assertEquals("COMPLETED", response.status)
        assertNull(response.created)
    }

    // ========== StatusResponse Tests ==========

    @Test
    fun testStatusResponseWithoutData() {
        val response = StatusResponse(
            status = "NOT_FOUND",
            data = null
        )

        val jsonString = json.encodeToString(response)
        assertContains(jsonString, "NOT_FOUND")
    }

    @Test
    fun testStatusResponseWithData() {
        val response = StatusResponse(
            status = "OK",
            data = StatusData(
                depositId = "deposit-123",
                status = "COMPLETED",
                amount = "1000",
                currency = "UGX",
                country = "UGA",
                created = "2024-01-15T12:00:00Z",
                providerTransactionId = "provider-txn-123",
                recipient = null,
                failureReason = null
            )
        )

        val jsonString = json.encodeToString(response)
        assertContains(jsonString, "deposit-123")
        assertContains(jsonString, "COMPLETED")
        assertContains(jsonString, "1000")
    }

    @Test
    fun testStatusResponseDeserialization() {
        val jsonString = """
            {
                "status": "OK",
                "data": {
                    "payoutId": "payout-123",
                    "status": "COMPLETED",
                    "amount": "2000",
                    "currency": "KES"
                }
            }
        """.trimIndent()

        val response = json.decodeFromString<StatusResponse>(jsonString)
        assertEquals("OK", response.status)
        assertNotNull(response.data)
        assertEquals("payout-123", response.data?.payoutId)
        assertEquals("COMPLETED", response.data?.status)
    }

    // ========== StatusData with FailureReason Tests ==========

    @Test
    fun testStatusDataWithFailureReason() {
        val statusData = StatusData(
            depositId = "deposit-failed",
            status = "FAILED",
            amount = "1000",
            currency = "UGX",
            failureReason = FailureReason(
                failureCode = "INSUFFICIENT_FUNDS",
                failureMessage = "The customer does not have sufficient funds"
            )
        )

        val jsonString = json.encodeToString(statusData)
        assertContains(jsonString, "INSUFFICIENT_FUNDS")
        assertContains(jsonString, "sufficient funds")
    }

    @Test
    fun testFailureReasonDeserialization() {
        val jsonString = """
            {
                "failureCode": "INVALID_MSISDN",
                "failureMessage": "The phone number is invalid"
            }
        """.trimIndent()

        val failureReason = json.decodeFromString<FailureReason>(jsonString)
        assertEquals("INVALID_MSISDN", failureReason.failureCode)
        assertEquals("The phone number is invalid", failureReason.failureMessage)
    }

    // ========== AccountDetails Tests ==========

    @Test
    fun testAccountDetailsSerialization() {
        val accountDetails = AccountDetails(
            phoneNumber = "+256700123456",
            provider = "MTN_MOMO_UGA"
        )

        val jsonString = json.encodeToString(accountDetails)
        assertContains(jsonString, "+256700123456")
        assertContains(jsonString, "MTN_MOMO_UGA")
    }

    @Test
    fun testAccountDetailsWithInternationalFormat() {
        val phoneNumbers = listOf(
            "+256700000000",  // Uganda
            "+254700000000",  // Kenya
            "+255700000000",  // Tanzania
            "+250700000000"   // Rwanda
        )

        phoneNumbers.forEach { number ->
            val details = AccountDetails(
                phoneNumber = number,
                provider = "TEST_PROVIDER"
            )
            assertEquals(number, details.phoneNumber)
            assertTrue(details.phoneNumber.startsWith("+"))
        }
    }

    // ========== TransactionType Tests ==========

    @Test
    fun testTransactionTypeValues() {
        val types = TransactionType.entries
        assertEquals(3, types.size)
        assertTrue(types.contains(TransactionType.DEPOSIT))
        assertTrue(types.contains(TransactionType.PAYOUT))
        assertTrue(types.contains(TransactionType.REFUND))
    }

    @Test
    fun testTransactionTypeEnumNames() {
        assertEquals("DEPOSIT", TransactionType.DEPOSIT.name)
        assertEquals("PAYOUT", TransactionType.PAYOUT.name)
        assertEquals("REFUND", TransactionType.REFUND.name)
    }

    @Test
    fun testTransactionTypeFromString() {
        assertEquals(TransactionType.DEPOSIT, TransactionType.valueOf("DEPOSIT"))
        assertEquals(TransactionType.PAYOUT, TransactionType.valueOf("PAYOUT"))
        assertEquals(TransactionType.REFUND, TransactionType.valueOf("REFUND"))
    }

    @Test
    fun testInvalidTransactionTypeThrowsException() {
        assertFailsWith<IllegalArgumentException> {
            TransactionType.valueOf("INVALID")
        }
    }

    // ========== Payer Tests ==========

    @Test
    fun testPayerSerialization() {
        val payer = Payer(
            type = "MMO",
            accountDetails = AccountDetails(
                phoneNumber = "+256700111111",
                provider = "MTN_MOMO_UGA"
            )
        )

        val jsonString = json.encodeToString(payer)
        assertContains(jsonString, "MMO")
        assertContains(jsonString, "+256700111111")
    }

    @Test
    fun testPayerDeserialization() {
        val jsonString = """
            {
                "type": "MMO",
                "accountDetails": {
                    "phoneNumber": "+254700222222",
                    "provider": "SAFARICOM_KEN"
                }
            }
        """.trimIndent()

        val payer = json.decodeFromString<Payer>(jsonString)
        assertEquals("MMO", payer.type)
        assertEquals("+254700222222", payer.accountDetails.phoneNumber)
    }

    // ========== Recipient Tests ==========

    @Test
    fun testRecipientWithExplicitType() {
        val recipient = Recipient(
            type = "BANK",
            accountDetails = AccountDetails(
                phoneNumber = "+256700333333",
                provider = "BANK_UGA"
            )
        )

        assertEquals("BANK", recipient.type)
    }

    @Test
    fun testRecipientSerialization() {
        val recipient = Recipient(
            accountDetails = AccountDetails(
                phoneNumber = "+256700444444",
                provider = "MTN_MOMO_UGA"
            )
        )

        val jsonString = json.encodeToString(recipient)
        assertContains(jsonString, "MMO")
        assertContains(jsonString, "+256700444444")
    }

    // ========== Edge Cases and Validation Tests ==========

    @Test
    fun testEmptyStringAmounts() {
        val request = DepositRequest(
            depositId = "test",
            amount = "",
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
        assertEquals("", request.amount)
    }

    @Test
    fun testLargeAmountValues() {
        val largeAmount = "999999999999"
        val request = PayoutRequest(
            payoutId = "payout-large",
            amount = largeAmount,
            currency = "UGX",
            recipient = Recipient(
                accountDetails = AccountDetails(
                    phoneNumber = "+256700000000",
                    provider = "MTN_MOMO_UGA"
                )
            )
        )
        assertEquals(largeAmount, request.amount)
    }

    @Test
    fun testDecimalAmounts() {
        val decimalAmount = "1000.50"
        val request = RefundRequest(
            refundId = "refund-decimal",
            depositId = "deposit-123",
            amount = decimalAmount,
            currency = "USD"
        )
        assertEquals(decimalAmount, request.amount)
    }

    @Test
    fun testMultipleCurrencies() {
        val currencies = listOf("UGX", "KES", "TZS", "RWF", "USD", "EUR")
        currencies.forEach { currency ->
            val request = DepositRequest(
                depositId = "test-$currency",
                amount = "1000",
                currency = currency,
                payer = Payer(
                    type = "MMO",
                    accountDetails = AccountDetails(
                        phoneNumber = "+256700000000",
                        provider = "MTN_MOMO_UGA"
                    )
                ),
                customerMessage = "Test"
            )
            assertEquals(currency, request.currency)
        }
    }

    @Test
    fun testSpecialCharactersInCustomerMessage() {
        val specialMessage = "Payment with special chars: @#\$%^&*()_+-=[]{}|;:',.<>?/~`"
        val request = DepositRequest(
            depositId = "test-special",
            amount = "1000",
            currency = "UGX",
            payer = Payer(
                type = "MMO",
                accountDetails = AccountDetails(
                    phoneNumber = "+256700000000",
                    provider = "MTN_MOMO_UGA"
                )
            ),
            customerMessage = specialMessage
        )
        assertEquals(specialMessage, request.customerMessage)
    }

    @Test
    fun testUnicodeCharactersInMessage() {
        val unicodeMessage = "Payment: 支付 оплата دفع भुगतान"
        val request = DepositRequest(
            depositId = "test-unicode",
            amount = "1000",
            currency = "UGX",
            payer = Payer(
                type = "MMO",
                accountDetails = AccountDetails(
                    phoneNumber = "+256700000000",
                    provider = "MTN_MOMO_UGA"
                )
            ),
            customerMessage = unicodeMessage
        )
        assertEquals(unicodeMessage, request.customerMessage)
    }

    @Test
    fun testStatusResponseWithPartialData() {
        val jsonString = """
            {
                "status": "OK",
                "data": {
                    "depositId": "deposit-123",
                    "status": "ENQUEUED"
                }
            }
        """.trimIndent()

        val response = json.decodeFromString<StatusResponse>(jsonString)
        assertEquals("OK", response.status)
        assertNotNull(response.data)
        assertEquals("deposit-123", response.data?.depositId)
        assertEquals("ENQUEUED", response.data?.status)
        assertNull(response.data?.amount)
        assertNull(response.data?.currency)
    }

    @Test
    fun testStatusResponseIgnoresUnknownFields() {
        val jsonString = """
            {
                "status": "OK",
                "unknownField": "should be ignored",
                "data": {
                    "depositId": "deposit-123",
                    "status": "COMPLETED",
                    "newField": "also ignored"
                }
            }
        """.trimIndent()

        val response = json.decodeFromString<StatusResponse>(jsonString)
        assertEquals("OK", response.status)
        assertNotNull(response.data)
        assertEquals("deposit-123", response.data?.depositId)
    }
}