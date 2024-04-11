package ua.polodarb.network.impl.suggestedFlags

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.serialization.json.Json
import ua.polodarb.network.impl.BuildConfig
import ua.polodarb.network.suggestedFlags.SuggestedFlagsApiService
import ua.polodarb.network.suggestedFlags.model.SuggestedFlagTypes
import ua.polodarb.network.Resource
import ua.polodarb.network.setConfig

private const val BASE_URL = "https://raw.githubusercontent.com/polodarb/GMS-Flags/"
private const val ASSETS_PATH = "/app/src/main/assets/"
private const val LOG_TAG = "FlagsApiService"

class SuggestedFlagsApiServiceImpl(
    engine: HttpClientEngine
): SuggestedFlagsApiService {
    private val client = HttpClient(engine) {
        this.setConfig(LOG_TAG)
        defaultRequest {
            url(BASE_URL + (if (BuildConfig.DEBUG) "develop" else "master") + ASSETS_PATH)
        }
    }

    override suspend fun getSuggestedFlags(): Resource<SuggestedFlagTypes> {
        return try {
            val url = if (BuildConfig.VERSION_NAME.contains("beta")) {
                "suggestedFlags_2.0_for_beta.json"
            } else {
                "suggestedFlags_2.0.json"
            }

            val response: String = client.get { url(url) }.body()
            Resource.Success(Json.decodeFromString(response))
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}