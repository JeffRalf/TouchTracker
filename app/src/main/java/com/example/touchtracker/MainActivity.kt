package com.example.touchtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.lifecycleScope
import com.example.touchtracker.network.SocketManager
import com.example.touchtracker.ui.theme.TouchTrackerTheme
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

class MainActivity : ComponentActivity() {
    private val socketManager = SocketManager("192.168.1.40", 5000)
    private var connected by mutableStateOf(false)
    private var pointerPosition by mutableStateOf(Offset.Zero)


    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("Caught an exception: $exception")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TouchTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                    content = {
                        Greeting("Android", modifier = Modifier.pointerInput(Unit) {
                            coroutineScope {
                                launch (exceptionHandler) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            if (event.changes.any { it.position != null }) {
                                                if (!connected) {
                                                    connectToServer()
                                                    println("I'm called once")
                                                }
                                                pointerPosition = event.changes.first { it.position != null }.position
                                                sendCoordinates(pointerPosition)
                                            }
                                        }
                                    }
                                }
                            }
                        })
                    }
                )
            }
        }
    }

    private fun connectToServer() {
        lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
            socketManager.connect()
            connected = true
        }
    }

    private fun sendCoordinates(position: Offset) {
        println("wesh")
        lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
            println("Weesh")
            socketManager.sendCoordinates(position.x, position.y)
        }
    }

    override fun onDestroy() {
        lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
            socketManager.disconnect()
            connected = false
        }
        super.onDestroy()
    }
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val density = LocalDensity.current.density
    val screenSize = LocalConfiguration.current.run {
        Size(
            screenWidthDp * density,
            screenHeightDp * density
        )
    }

    var position by remember { mutableStateOf(Offset.Zero) }

    Text(
        text = "Hello $name! Pointer position: $position",
        modifier = modifier.pointerInput(Unit) {
            coroutineScope {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.position != null }) {
                            val newPosition = event.changes.firstOrNull()?.position ?: Offset.Zero
                            position = newPosition//limitPosition(newPosition, screenSize)
                        }
                    }
                }
            }
        }
    )
}

/*fun limitPosition(position: Offset, screenSize: Size): Offset {
    val maxWidth = screenSize.width
    val maxHeight = (maxWidth * 9 / 16).coerceAtMost(screenSize.height)

    val x = position.x.coerceIn(0f, maxWidth)
    val y = position.y.coerceIn(0f, maxHeight)

    return Offset(x, y)
}*/
