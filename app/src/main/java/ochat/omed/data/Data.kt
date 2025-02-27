package ochat.omed.data

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import ochat.omed.R
import ochat.omed.backend.APIPill
import ochat.omed.ui.screens.IllnessType
import ochat.omed.ui.screens.Pill
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val colors = listOf(
    Color(0xFFF197C0),
    Color(0xFFFEC9A7),
    Color(0xFFA5F8CE),
    Color(0xFFFEFD97),
    Color(0xFFC5EBFE),
    Color(0xFFB49FDC),

    Color(0xFFFFD3E0),
    Color(0xFFFFF5C3),
    Color(0xFFB8E6C1),
    Color(0xFFC6D8FF)
)

enum class TimeArea(
    val startTime: LocalTime,
    val endTime: LocalTime
) {
    JUSTAWAKE(LocalTime.of(6, 0), LocalTime.of(7, 0)),  // Right after waking up
    BEFOREBREAKFAST(LocalTime.of(7, 0), LocalTime.of(8, 0)), // Before breakfast
    AFTERBREAKFAST(LocalTime.of(8, 0), LocalTime.of(10, 30)), // After breakfast
    MIDDAY(LocalTime.of(10, 30), LocalTime.of(12, 30)), // Mid-morning
    BEFORELUNCH(LocalTime.of(12, 30), LocalTime.of(13, 30)), // Before lunch
    AFTERLUNCH(LocalTime.of(13, 30), LocalTime.of(15, 30)), // After lunch
    MIDAFTERNOON(LocalTime.of(15, 30), LocalTime.of(17, 30)), // Mid-afternoon
    BEFOREDINNER(LocalTime.of(17, 30), LocalTime.of(19, 30)), // Before dinner
    AFTERDINNER(LocalTime.of(19, 30), LocalTime.of(21, 30)), // After dinner
    PREVIOUSTOSLEEP(LocalTime.of(21, 30), LocalTime.of(23, 59)),// Before going to sleep
    DEFAULT(LocalTime.of(0, 0), LocalTime.of(0, 0))
}

data class PillGroup(
    val timeArea: TimeArea,
    val pills: List<Pill>?,
    val color: Color
)

val bitmap: Bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

val pills = listOf(
    Pill("Paracetamol", bitmap, 60, 0.15f, 20, LocalDateTime.now(), IllnessType.DIGESTIVE),
    Pill("Ibuprofeno", bitmap, 60, 0.5f, 10, LocalDateTime.now(), IllnessType.BRAIN_RELATED),
    Pill("Omeprazol", bitmap, 15, 0.25f, 15, LocalDateTime.now(), IllnessType.PSYCHOLOGICAL),
    Pill("Nose", bitmap, 30, 0.8f, 50, LocalDateTime.now(), IllnessType.GENERAL_BODY),
    Pill("Rambutan", bitmap, 90, 0.2f,5, LocalDateTime.now(), IllnessType.HEART_RELATED),
    Pill("Terminal", bitmap, 0, 0.1f, 10, LocalDateTime.now(), IllnessType.BRAIN_RELATED)
)

val pillGroups = listOf(
    PillGroup(TimeArea.JUSTAWAKE, pills.shuffled(), colors[0]),
    PillGroup(TimeArea.BEFOREBREAKFAST, pills.shuffled(), colors[1]),
    PillGroup(TimeArea.AFTERBREAKFAST, pills.shuffled(), colors[2]),
    PillGroup(TimeArea.MIDDAY, pills.shuffled(), colors[3]),
    PillGroup(TimeArea.BEFORELUNCH, pills.shuffled(), colors[4]),
    PillGroup(TimeArea.AFTERLUNCH, pills.shuffled(), colors[5]),
    PillGroup(TimeArea.MIDAFTERNOON, pills.shuffled(), colors[6]),
    PillGroup(TimeArea.BEFOREDINNER, pills.shuffled(), colors[7]),
    PillGroup(TimeArea.AFTERDINNER, pills.shuffled(), colors[8]),
    PillGroup(TimeArea.PREVIOUSTOSLEEP, pills.shuffled(), colors[9]),
)

fun base64ToBitmap(base64String: String): Bitmap? {
    return try {
        val decodedBytes: ByteArray = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        null
    }
}

fun parseDateTime(dateString: String): LocalDateTime {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    return LocalDateTime.parse(dateString, formatter)
}

fun parsePill(apiPill: APIPill): Pill {

    val img: Bitmap? = if (!apiPill.image.isNullOrEmpty()) {
        base64ToBitmap(apiPill.image)
    } else {
        null
    }

    val sdate: LocalDateTime? = try {
        parseDateTime(apiPill.startDate)
    } catch (e: Exception) {
        Log.e("parsePill", "Invalid startDate: ${apiPill.startDate}")
        null
    }

    val illnessType: IllnessType = try {
        IllnessType.valueOf(apiPill.illnessType.uppercase())
    } catch (e: IllegalArgumentException) {
        Log.e("parsePill", "Invalid illnessType: ${apiPill.illnessType}")
        IllnessType.GENERAL_BODY
    }

    val quantity = apiPill.quantity ?: 0

    return Pill(
        apiPill.name,
        img,
        apiPill.frequency,
        apiPill.dose,
        quantity,
        sdate!!,
        illnessType
    )
}