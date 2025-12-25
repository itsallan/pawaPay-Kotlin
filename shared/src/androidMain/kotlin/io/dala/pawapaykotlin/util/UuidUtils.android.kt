package io.dala.pawapaykotlin.util

import java.util.UUID

actual fun generateUUID(): String {
    return UUID.randomUUID().toString()
}