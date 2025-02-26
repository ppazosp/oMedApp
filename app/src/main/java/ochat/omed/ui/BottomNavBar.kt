package ochat.omed.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.exyte.animatednavbar.AnimatedNavigationBar
import ochat.omed.ui.screens.Screens

@Composable
fun BottomNavigationBar(selectedIndex: Int, onItemSelected: (Int) -> Unit) {
    val screens = listOf(Screens.Resume, Screens.Camera)

    AnimatedNavigationBar(
        modifier = Modifier.fillMaxWidth(),
        selectedIndex = selectedIndex,
        barColor = Color.Black
    ) {
        screens.forEachIndexed { index, screen ->
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clickable { onItemSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = screen.icon,
                    contentDescription = screen.title,
                    tint = if (selectedIndex == index) Color.White else Color.LightGray
                )
            }
        }
    }
}