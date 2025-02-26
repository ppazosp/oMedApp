package ochat.omed.backend

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ochat.omed.R
import ochat.omed.data.parsePill
import ochat.omed.ui.screens.Pill
import java.io.File

@Serializable
enum class APIIllnessType(val icon: Int) {
    HEART_RELATED(R.drawable.heart),
    DIGESTIVE(R.drawable.digestive),
    GENERAL_BODY(R.drawable.body),
    BRAIN_RELATED(R.drawable.brain),
    PSYCHOLOGICAL(R.drawable.psychological)
}

@Serializable
enum class APIPillType(val icon: Int){
    PILL(R.drawable.pill),
    TABLET(R.drawable.tablet),
    INHALER(R.drawable.inhaler)
}

@Serializable
data class APIPill(
    @SerialName("nombre_del_medicamento")val name: String,
    @SerialName("cropped_image")val image: String,
    @SerialName("frecuencia")val frequency: Int,
    @SerialName("cantidad_por_dosis")val dose: Float,
    @SerialName("numero_de_comprimidos")val quantity: Int,
    @SerialName("primera_ingestion")val startDate: String,
    @SerialName("parte_afectada")val illnessType: String,
)

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }

    engine {
        requestTimeout = 300_000L
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 300_000
        connectTimeoutMillis = 300_000
        socketTimeoutMillis = 300_000
    }
}

suspend fun getNewPill(imageFile: File, audioFile: File): Pill {

    val response: HttpResponse = client.post("http://10.20.1.57:5000/transcribe") {
        headers {
            append(HttpHeaders.ContentType, "multipart/form-data; boundary=boundary")
        }
        setBody(
            MultiPartFormDataContent(
                formData {
                    append("audio", audioFile.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "audio/mpeg")
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"audio\"; filename=\"audio.mp3\"")
                    })
                    append("photo", imageFile.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "photo/jpeg")
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"photo\"; filename=\"photo.jpg\"")
                    })
                }
            )
        )
    }

    val responseBody = response.bodyAsText()
    Log.d("Response Body", responseBody)

    if (response.status == HttpStatusCode.OK) {
        val apiPill: APIPill = Json.decodeFromString(responseBody)
        return parsePill(apiPill)
    } else {
        Log.e("ERROR", responseBody)
        throw Exception("Server error: $responseBody")
    }
}