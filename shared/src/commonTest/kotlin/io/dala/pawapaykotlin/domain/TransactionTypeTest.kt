package io.dala.pawapaykotlin.domain

import kotlin.test.*

/**
 * Additional comprehensive tests for TransactionType enum
 */
class TransactionTypeTest {

    @Test
    fun testAllTransactionTypes() {
        val types = TransactionType.entries
        assertEquals(3, types.size)
        
        val typeNames = types.map { it.name }.toSet()
        assertTrue(typeNames.contains("DEPOSIT"))
        assertTrue(typeNames.contains("PAYOUT"))
        assertTrue(typeNames.contains("REFUND"))
    }

    @Test
    fun testTransactionTypeOrdinals() {
        // Verify ordinal values (order matters for enum)
        assertTrue(TransactionType.DEPOSIT.ordinal >= 0)
        assertTrue(TransactionType.PAYOUT.ordinal >= 0)
        assertTrue(TransactionType.REFUND.ordinal >= 0)
        
        // All ordinals should be unique
        val ordinals = TransactionType.entries.map { it.ordinal }.toSet()
        assertEquals(3, ordinals.size)
    }

    @Test
    fun testTransactionTypeEquality() {
        assertEquals(TransactionType.DEPOSIT, TransactionType.valueOf("DEPOSIT"))
        assertEquals(TransactionType.PAYOUT, TransactionType.valueOf("PAYOUT"))
        assertEquals(TransactionType.REFUND, TransactionType.valueOf("REFUND"))
        
        assertNotEquals(TransactionType.DEPOSIT, TransactionType.PAYOUT)
        assertNotEquals(TransactionType.PAYOUT, TransactionType.REFUND)
        assertNotEquals(TransactionType.DEPOSIT, TransactionType.REFUND)
    }

    @Test
    fun testTransactionTypeToString() {
        assertEquals("DEPOSIT", TransactionType.DEPOSIT.toString())
        assertEquals("PAYOUT", TransactionType.PAYOUT.toString())
        assertEquals("REFUND", TransactionType.REFUND.toString())
    }

    @Test
    fun testTransactionTypeInCollections() {
        val typeSet = setOf(TransactionType.DEPOSIT, TransactionType.PAYOUT, TransactionType.REFUND)
        assertEquals(3, typeSet.size)
        
        val typeList = listOf(TransactionType.DEPOSIT, TransactionType.DEPOSIT, TransactionType.PAYOUT)
        assertEquals(3, typeList.size)
        assertEquals(2, typeList.count { it == TransactionType.DEPOSIT })
    }

    @Test
    fun testTransactionTypeInWhenExpression() {
        fun getDescription(type: TransactionType): String = when (type) {
            TransactionType.DEPOSIT -> "Collect money from customer"
            TransactionType.PAYOUT -> "Send money to recipient"
            TransactionType.REFUND -> "Return money to customer"
        }

        assertEquals("Collect money from customer", getDescription(TransactionType.DEPOSIT))
        assertEquals("Send money to recipient", getDescription(TransactionType.PAYOUT))
        assertEquals("Return money to customer", getDescription(TransactionType.REFUND))
    }

    @Test
    fun testTransactionTypeEnumValues() {
        val values = TransactionType.entries.toTypedArray()
        assertTrue(values.contains(TransactionType.DEPOSIT))
        assertTrue(values.contains(TransactionType.PAYOUT))
        assertTrue(values.contains(TransactionType.REFUND))
    }

    @Test
    fun testInvalidTransactionTypeThrows() {
        assertFailsWith<IllegalArgumentException> {
            TransactionType.valueOf("INVALID_TYPE")
        }
        
        assertFailsWith<IllegalArgumentException> {
            TransactionType.valueOf("deposit")  // lowercase
        }
        
        assertFailsWith<IllegalArgumentException> {
            TransactionType.valueOf("")
        }
    }

    @Test
    fun testTransactionTypeCanBeUsedAsMapKey() {
        val transactionDescriptions = mapOf(
            TransactionType.DEPOSIT to "deposits",
            TransactionType.PAYOUT to "payouts",
            TransactionType.REFUND to "refunds"
        )

        assertEquals("deposits", transactionDescriptions[TransactionType.DEPOSIT])
        assertEquals("payouts", transactionDescriptions[TransactionType.PAYOUT])
        assertEquals("refunds", transactionDescriptions[TransactionType.REFUND])
    }
}