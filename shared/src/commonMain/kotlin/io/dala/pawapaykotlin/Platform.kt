package io.dala.pawapaykotlin

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform