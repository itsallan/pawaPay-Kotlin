package io.dala.pawapaykotlin

/**
 * Public wrapper to exposing BuildKonfig
 */
object PawaPayConfig {
    val API_TOKEN: String = BuildKonfig.API_TOKEN
    val IS_SANDBOX: Boolean = BuildKonfig.IS_SANDBOX
}