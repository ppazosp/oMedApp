package ochat.omed.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import ochat.omed.ui.screens.CameraScreen
import ochat.omed.ui.screens.ResumeScreen
import ochat.omed.ui.screens.Screens

@Composable
fun NavigationGraph() {
    val screens = listOf(Screens.Resume, Screens.Camera)
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { screens.size }
    )
    val coroutineScope = rememberCoroutineScope()

    var currentPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(pagerState.currentPage) {
        currentPage = pagerState.currentPage
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedIndex = currentPage,
                onItemSelected = { page ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(page)
                    }
                }
            )
        }
    ) { innerpadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerpadding)
        ) { page ->
            when (screens[page]) {
                Screens.Resume -> ResumeScreen()
                Screens.Camera -> CameraScreen()
            }
        }
    }
}