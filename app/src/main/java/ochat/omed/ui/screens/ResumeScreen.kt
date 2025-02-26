package ochat.omed.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ochat.omed.R
import ochat.omed.data.PillGroup
import ochat.omed.data.TimeArea
import ochat.omed.data.pillGroups
import ochat.omed.ui.theme.OMedAppTheme
import java.time.LocalDateTime
import java.time.LocalTime

data class Pill(
    val name: String,
    val image: Bitmap?,
    val frequency: Int,
    val dose: Float,
    val quantity: Int,
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ResumeScreen() {

    val thisTimeArea = getCurrentTimeArea()

    val listState = rememberLazyListState()
    val startIndex = pillGroups.indexOfFirst { it.timeArea == thisTimeArea }.takeIf { it >= 0 } ?: 0

    LaunchedEffect(Unit) {
        Log.d("q", startIndex.toString())
        delay(300)
        listState.scrollToItem(startIndex)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
            )
            {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ){
                    Text(
                        text = "My Pill Box",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,

                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    )
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top)
                ) {
                    itemsIndexed(pillGroups) { _, group ->
                        if (!group.pills.isNullOrEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding( horizontal = if (group.timeArea != thisTimeArea) 16.dp else 0.dp, vertical = 8.dp)
                            ){
                                PillGroupCard(group)
                                Spacer(modifier = Modifier.size(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PillGroupCard(group: PillGroup){
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Color.Black, RoundedCornerShape(12.dp)),
        colors = CardDefaults.elevatedCardColors(
            containerColor = group.color
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
            ) {
                Text(
                    text = group.timeArea.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(group.pills!!) { index, pill ->
                Box(
                    modifier = Modifier
                        .size(width = 150.dp, height = 150.dp)
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
                bitmap = pill.image!!.asImageBitmap(),
                contentDescription = "Pill type icon",
            )

            Text(
                text = pill.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}