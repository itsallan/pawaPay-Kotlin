package io.dala.pawapaykotlin.di

import io.dala.pawapaykotlin.network.PawaPayApi
import io.dala.pawapaykotlin.repository.PawaPayRepository
import io.dala.pawapaykotlin.repository.PawaPayRepositoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun appModule(baseUrl: String, apiToken: String) = module {
    single {
        HttpClient {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            defaultRequest {
                url(baseUrl)
                header("Authorization", "Bearer $apiToken")
                header("Content-Type", "application/json")
            }
        }
    }
    single { PawaPayApi(client = get()) }
    single<PawaPayRepository> { PawaPayRepositoryImpl(api = get()) }
}

fun initKoin(
    baseUrl: String,
    apiToken: String,
    appDeclaration: KoinAppDeclaration = {}
) = startKoin {
    appDeclaration()
    modules(appModule(baseUrl, apiToken))
}