package ochat.omed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ochat.omed.ui.theme.OMedAppTheme
import ochat.omed.ui.NavigationGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OMedAppTheme {
                NavigationGraph()
            }
        }
    }
}
