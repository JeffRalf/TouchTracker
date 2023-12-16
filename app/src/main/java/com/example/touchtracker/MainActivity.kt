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
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.lifecycleScope

import com.example.touchtracker.network.SocketManager
import com.example.touchtracker.ui.theme.TouchTrackerTheme
import com.example.touchtracker.file.File

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter

import java.net.InetSocketAddress
import java.net.Socket

class MainActivity : ComponentActivity() {
    private var socket: Socket = Socket()
    private var file: File = File()
    private var connected: Boolean = false
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
                                launch (Dispatchers.IO + exceptionHandler) {
                                    serverSender()
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            if (event.changes.any { it.position != null }) {
                                                pointerPosition = event.changes.first { it.position != null }.position
                                                file.enqueue(pointerPosition.x, pointerPosition.y)
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

    private fun establishConnection(message: String?) {
        if (message == "Broken pipe") {
            socket = Socket()
        }
        while (!connected) {
            try {
                socket.connect(InetSocketAddress("192.168.1.40", 5000), 5000)
                connected = true
                println("Connexion réussie")
            } catch (e: IOException) {
                println("Erreur de connexion : ${e.message}")
                // Attendez un moment avant de réessayer
                if (e.message == "already connected") {
                    connected = true
                }
                else if (e.message == "Socket closed") {
                    socket = Socket()
                }
                else {
                    Thread.sleep(1000)
                }
            }
        }
    }

    private fun serverSender() {
        lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
            while (true) {
                if (!connected) {
                    establishConnection(null)
                }
                file.dequeue()?.let {couple ->
                    println("ikusooooo")
                    sendCoordinates(couple.first, couple.second)
                }
            }
        }
    }
    private fun sendCoordinates(x: Float, y: Float) {
        socket?.let { socket ->
            val outputStream: OutputStream = socket.getOutputStream()
            val writer = BufferedWriter(OutputStreamWriter(outputStream))

            try {
                writer.write("$x,$y\n")
                writer.flush()
            } catch (e: IOException) {
                connected = false
                println("Erreur lors de l'envoi des coordonnées : ${e.message}")
                establishConnection(e.message)
            }
        }
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
