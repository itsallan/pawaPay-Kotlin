package io.dala.pawapaykotlin

import kotlin.test.*

/**
 * Tests for PawaPayConfig
 */
class PawaPayConfigTest {

    @Test
    fun testApiTokenExists() {
        // API_TOKEN should be accessible (even if empty in test environment)
        assertNotNull(PawaPayConfig.API_TOKEN)
    }

    @Test
    fun testIsSandboxFlag() {
        // IS_SANDBOX should be a boolean value
        val isSandbox = PawaPayConfig.IS_SANDBOX
        assertTrue(isSandbox is Boolean)
    }

    @Test
    fun testConfigValuesAreConsistent() {
        // Multiple accesses should return the same values
        val token1 = PawaPayConfig.API_TOKEN
        val token2 = PawaPayConfig.API_TOKEN
        assertEquals(token1, token2)

        val sandbox1 = PawaPayConfig.IS_SANDBOX
        val sandbox2 = PawaPayConfig.IS_SANDBOX
        assertEquals(sandbox1, sandbox2)
    }
}