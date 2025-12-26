package io.dala.pawapaykotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.dala.pawapaykotlin.di.initKoin
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (GlobalContext.getOrNull() == null) {
            initKoin(
                baseUrl = if (PawaPayConfig.IS_SANDBOX)
                    "https://api.sandbox.pawapay.io/v2/"
                else
                    "https://api.pawapay.io/v2/",
                apiToken = PawaPayConfig.API_TOKEN
            )
        }

        setContent {
            App()
        }
    }
}