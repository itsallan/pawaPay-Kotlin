package io.dala.pawapaykotlin.util

import java.util.UUID

actual fun generateUUID(): String = UUID.randomUUID().toString()