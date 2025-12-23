package io.dala.pawapaykotlin.repository

import java.util.UUID

actual fun generateUUID(): String = UUID.randomUUID().toString()