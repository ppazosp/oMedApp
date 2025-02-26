package ochat.omed.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ochat.omed.R
import ochat.omed.backend.getNewPill
import ochat.omed.ui.theme.OMedAppTheme
import java.io.File

@Preview
@Composable
fun CameraPreview(){
    OMedAppTheme {
        CameraScreen()
    }
}

@Composable
fun CameraScreen() {
    var showRecordingScreen by remember { mutableStateOf(false) }
    var imageFile by remember { mutableStateOf<File?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var apiCalled by remember { mutableStateOf(false) }  // Flag to prevent multiple API calls
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        if (showRecordingScreen) {
            RecordingScreen(
                onRecordingFinished = { recordedFile ->
                    audioFile = recordedFile
                    showRecordingScreen = false

                    if (imageFile != null && !apiCalled) {
                        apiCalled = true
                        sendToAPI(imageFile!!, audioFile!!, context)
                    } else {
                        if(imageFile == null)
                            Log.e("CameraScreen", "Error: imageFile or audioFile is null")
                    }
                }
            )
        } else {
            CameraContent(
                onPhotoSelected = { file ->
                    imageFile = file
                    showRecordingScreen = true
                }
            )
        }
    }
}

fun sendToAPI(imageFile: File, audioFile: File, context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            getNewPill(imageFile, audioFile)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Data sent successfully!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error occurred", Toast.LENGTH_LONG).show()
            }

            Log.e("ERROR", e.message.toString())
        }
    }
}

@Composable
fun RecordingScreen(onRecordingFinished: (File) -> Unit) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var hasMicPermission by remember { mutableStateOf(false) }
    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasMicPermission = granted }
    )

    LaunchedEffect(Unit) {
        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    DisposableEffect(isRecording) {
        if (isRecording && hasMicPermission) {
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "recorded_audio.m4a"
            )
            audioFile = file

            mediaRecorder = MediaRecorder().apply {
                try {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setOutputFile(file.absolutePath)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    prepare()
                    start()
                } catch (e: Exception) {
                    e.printStackTrace()
                    isRecording = false
                }
            }
        }

        onDispose {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                audioFile?.let { onRecordingFinished(it) }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(R.drawable.mic),
                contentDescription = "Mic",
                modifier = Modifier
                    .size(200.dp)
                    .scale(if (isRecording) scale else 1f)
                    .clickable {
                        if (hasMicPermission) {
                            isRecording = !isRecording
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                tint = if (isRecording) Color.Red else Color.Black
            )
        }
    }
}

@Composable
fun CameraContent(onPhotoSelected: (File) -> Unit) {
    var showCamera by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { onPhotoSelected(uriToFile(context, it)) }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (showCamera) {
            CameraView(
                onPhotoTaken = { file ->
                    showCamera = false
                    onPhotoSelected(file)
                }
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.width(180.dp).height(60.dp)
                    ) {
                        Text(
                            text = "Elegir Foto",
                            fontStyle = FontStyle.Normal,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(Modifier.size(32.dp))

                    Button(
                        onClick = { showCamera = true },
                        shape = RoundedCornerShape(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.width(180.dp).height(60.dp),
                    ) {
                        Text(
                            text = "Tomar Foto",
                            fontStyle = FontStyle.Normal,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CameraView(onPhotoTaken: (File) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = androidx.camera.core.Preview.Builder().build()
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    val imageCaptureConfig = ImageCapture.Builder().build()
                    imageCapture = imageCaptureConfig

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCaptureConfig
                    )

                    preview.surfaceProvider = previewView.surfaceProvider
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, shape = CircleShape)
                        .clickable {
                            takePhoto(context, imageCapture!!, onPhotoTaken)
                        }
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }
}

fun takePhoto(context: Context, imageCapture: ImageCapture, onPhotoTaken: (File) -> Unit) {
    Toast.makeText(context, "Foto tomada", Toast.LENGTH_LONG).show()

    val photoFile = File.createTempFile("photo_", ".jpg", context.cacheDir)
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onPhotoTaken(photoFile)
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context, "Error saving photo: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    )
}

fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val tempFile = File.createTempFile("selected_", ".jpg", context.cacheDir)
    inputStream?.use { input ->
        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return tempFile
}

