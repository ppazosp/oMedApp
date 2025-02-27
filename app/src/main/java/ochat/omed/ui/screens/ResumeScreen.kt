package ochat.omed.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ochat.omed.R
import ochat.omed.backend.ResumeResponse
import ochat.omed.backend.getPills
import ochat.omed.backend.getResume
import ochat.omed.data.TimeArea
import ochat.omed.data.colors
import ochat.omed.ui.theme.OMedAppTheme
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.time.LocalDateTime
import java.time.LocalTime

data class Pill(
    val name: String,
    val image: Bitmap?,
    val frequency: Int,
    val dose: Float,
    val quantity: Int?,
    val startDate: LocalDateTime,
    val illnessType: IllnessType
)

enum class IllnessType(val icon: Int) {
    HEART_RELATED(R.drawable.heart),
    DIGESTIVE(R.drawable.digestive),
    GENERAL_BODY(R.drawable.body),
    BRAIN_RELATED(R.drawable.brain),
    PSYCHOLOGICAL(R.drawable.psychological)
}

fun playAudioFromBase64(base64String: String, mediaPlayer: MediaPlayer) {
    try {
        val audioBytes = Base64.decode(base64String, Base64.DEFAULT)

        val tempFile = File.createTempFile("audio", ".mp3", null)
        tempFile.deleteOnExit()

        val fos = FileOutputStream(tempFile)
        fos.write(audioBytes)
        fos.close()

        mediaPlayer.setDataSource(tempFile.absolutePath)
        mediaPlayer.prepare()

        mediaPlayer.start()

        mediaPlayer.setOnCompletionListener {
            it.release()
        }

    } catch (e: IOException) {
        e.printStackTrace()
    }
}

@Preview
@Composable
fun ResumePreview(){
    OMedAppTheme {
        ResumeScreen()
    }
}


fun getCurrentTimeArea(): TimeArea? {
    val currentTime = LocalTime.now()

    return TimeArea.entries.find { area ->
        currentTime.isAfter(area.startTime) && currentTime.isBefore(area.endTime)
    }?: TimeArea.DEFAULT
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "MutableCollectionMutableState")
@Composable
fun ResumeScreen() {
    var pillsMap by remember { mutableStateOf<Map<TimeArea, List<Pill>>?>(null) }
    var isResumeVisible by remember { mutableStateOf(false) }
    var isLoadingVisible by remember { mutableStateOf(true) }
    var resumeText by remember { mutableStateOf("") }
    var resumeAudio by remember { mutableStateOf("") }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }  // MediaPlayer reference

    // Transition for animated card
    val transition = updateTransition(targetState = isResumeVisible, label = "Loading Card Transition")
    val cardOffsetY by transition.animateDp(
        label = "Card Slide",
        transitionSpec = {
            if (isResumeVisible) tween(durationMillis = 600) else tween(durationMillis = 600)
        }
    ) { if (it) 0.dp else 300.dp }

    // Fetch pills data on screen launch
    LaunchedEffect(Unit) {
        try {
            Log.d("LAUNCHED", "LAUNCHED")

            val unsortedPillsMap = getPills()
            pillsMap = unsortedPillsMap.toSortedMap()

            Log.d("LOG", pillsMap.toString())

        } catch (e: Exception) {
            Log.e("ResumeScreen", "Error fetching pills: ${e.printStackTrace()}")
        }
    }

    // Fetch resume when isResumeVisible is true
    LaunchedEffect(isResumeVisible) {
        if (isResumeVisible) {
            try {
                // Assuming getResume is the function that fetches the resume
                val resume: ResumeResponse = getResume()
                resumeText = resume.summary
                resumeAudio = resume.audioBase64
            } catch (e: Exception) {
                Log.e("ResumeScreen", "Error fetching resume: ${e.message}")
            } finally {
                isLoadingVisible = false
            }
        } else {
            mediaPlayer?.apply {
                stop()
                release()
            }
            mediaPlayer = null
        }
    }

    val thisTimeArea = getCurrentTimeArea()
    val listState = rememberLazyListState()

    // Show loading state when pillsMap is not yet available
    if (pillsMap == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = Color.Black,
                strokeWidth = 5.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Loading...", fontSize = 18.sp)
        }
        return
    }

    // Start scrolling to the correct time area
    LaunchedEffect(pillsMap) {
        delay(300)
        listState.scrollToItem(pillsMap!!.keys.indexOfFirst { it == thisTimeArea }.takeIf { it >= 0 } ?: 0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "My Pill Box",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // LazyColumn to show pills
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top)
                ) {
                    itemsIndexed(pillsMap!!.entries.toList()) { index, group ->
                        if (group.value.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        horizontal = if (group.key != thisTimeArea) 16.dp else 0.dp,
                                        vertical = 8.dp
                                    )
                            ) {
                                PillGroupCard(index, group.key, group.value)
                                Spacer(modifier = Modifier.size(8.dp))
                            }
                        }
                    }
                }
            }

            // Floating Action Button (FAB) to toggle resume visibility
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = { isResumeVisible = !isResumeVisible },
                    modifier = Modifier
                        .padding(0.dp)
                        .size(64.dp),
                    containerColor = Color.Black
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Info",
                        tint = Color.White
                    )
                }
            }

            // Animated loading card that appears and disappears
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = cardOffsetY)
                    .padding(16.dp)
                    .clickable { isResumeVisible = false }
            ) {
                if (isResumeVisible) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.elevatedCardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (isLoadingVisible) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(64.dp),
                                    color = Color.Black,
                                    strokeWidth = 5.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = "Loading...", fontSize = 18.sp)
                            } else {
                                LazyColumn {
                                    item {
                                        Text(
                                            text = "Resumen del día",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = resumeText,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if(mediaPlayer == null) {
                                    mediaPlayer = MediaPlayer()
                                    playAudioFromBase64(resumeAudio, mediaPlayer!!)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun PillGroupCard(index: Int, timeArea: TimeArea, group: List<Pill>){
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Color.Black, RoundedCornerShape(12.dp)),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colors[index]
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
            ) {
                Text(
                    text = timeArea.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(group) { index, pill ->
                Box(
                    modifier = Modifier
                        .size(width = 168.dp, height = 168.dp)
                )
                {
                    PillCard(pill)
                }
                }
            }
        }

    }
}

@Composable
fun PillCard(pill: Pill){
    ElevatedCard(
        modifier = Modifier
            .fillMaxSize()
            .border(1.5.dp, Color.Black, RoundedCornerShape(8.dp)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Start),
                painter = painterResource(pill.illnessType.icon),
                contentDescription = "Illness type icon",
                tint = Color.Unspecified,
            )

            Image(
                modifier = Modifier
                    .size(64.dp),
                painter = painterResource(R.drawable.pill),
                contentDescription = "Pill type icon",
            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = pill.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}