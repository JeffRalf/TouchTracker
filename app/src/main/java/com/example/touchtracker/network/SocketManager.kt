package com.example.touchtracker.network

import java.io.IOException
import java.io.OutputStream
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.util.TimerTask
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class SocketManager(private val serverAddress: String, private val serverPort: Int) {
    private var socket: Socket? = null
    private var connected: Boolean = false
    private var scheduler: ScheduledExecutorService? = null
    private var timeout: Int = 5000

    fun connect() {
        try {
            socket?.connect(InetSocketAddress(serverAddress, serverPort), timeout)
            // La connexion a réussi
            println("Connexion réussie")
        } catch (e: IOException) {
            // La connexion a échoué, gérer l'exception ici
            println("Erreur de connexion : ${e.message}")
        } finally {
            // Assurez-vous de fermer le socket, que la connexion ait réussi ou échoué
            socket?.close()
        }
        connected = true
    }

    fun sendCoordinates(x: Float, y: Float) {
        println("pouet")
        try {
            socket?.connect(InetSocketAddress(serverAddress, serverPort), timeout)
            // La connexion a réussi
            println("Connexion réussie")
        } catch (e: IOException) {
            // La connexion a échoué, gérer l'exception ici
            println("Erreur de connexion : ${e.message}")
        } finally {
            // Assurez-vous de fermer le socket, que la connexion ait réussi ou échoué
            socket?.close()
        }
        socket?.let { socket ->
            println("plouf")
            val outputStream: OutputStream = socket.getOutputStream()
            val printWriter = PrintWriter(outputStream, false)
            printWriter.println("$x,$y")
            printWriter.flush()
            println("plouf2")
            if (printWriter.checkError()) {
                println("Erreur lors de l'écriture, probable perte de connexion")
                connected = false
                reconnect()
            }
        }
    }

    fun disconnect() {
        socket?.close()
        connected = false
    }

    private fun reconnect() {
        while (!connected) {
            println("Aled")
            connect()
            Thread.sleep(1000) // Ajoutez un délai entre les tentatives (par exemple, 1 seconde)
        }
    }


    private fun startHeartbeat() {
        val heartbeatInterval = 5000 // Interval en millisecondes
        val heartbeatMessage = "heartbeat"

        scheduler = Executors.newSingleThreadScheduledExecutor()
        scheduler?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (isConnected()) {
                    sendHeartbeat(heartbeatMessage)
                }
            }
        }, 0, heartbeatInterval.toLong(), TimeUnit.MILLISECONDS)
    }

    private fun sendHeartbeat(message: String) {
        try {
            socket?.let { socket ->
                println("patate")
                // Envoyez le message au serveur
                val outputStream: OutputStream = socket.getOutputStream()
                val printWriter = PrintWriter(outputStream, false)
                printWriter.println(message)
                printWriter.flush()
                if (printWriter.checkError()) {
                    println("Erreur lors de l'écriture, probable perte de connexion")
                    connected = false
                    reconnect()
                }
            }
        } catch (e: Exception) {
            // Gérez les erreurs d'envoi
            connected = false
            reconnect()
            e.printStackTrace()
        }
    }




    fun isConnected(): Boolean {
        return connected
    }
}

