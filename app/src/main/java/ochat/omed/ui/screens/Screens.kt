package ochat.omed.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screens(val title: String, val icon: ImageVector) {
    //Chatbot("Chatbot", Icons.Filled.Face),
    Resume("Resume", Icons.Filled.Menu),
    Camera("Camera", Icons.Filled.AddCircle)
}