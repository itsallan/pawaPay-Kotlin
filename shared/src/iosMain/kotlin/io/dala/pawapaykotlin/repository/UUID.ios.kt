package io.dala.pawapaykotlin.repository

import platform.Foundation.NSUUID
actual fun generateUUID(): String = NSUUID.UUID().UUIDString()