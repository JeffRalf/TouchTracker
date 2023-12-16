package com.example.touchtracker.file

import java.util.LinkedList

class File() {
    private val floatPairQueue = LinkedList<Pair<Float, Float>>()

    // Méthode pour ajouter un couple de floats à la file
    fun enqueue(x: Float, y: Float) {
        val floatPair = Pair(x, y)
        floatPairQueue.addLast(floatPair)
    }

    // Méthode pour retirer et renvoyer le premier couple de floats de la file
    fun dequeue(): Pair<Float, Float>? {
        return if (floatPairQueue.isNotEmpty()) {
            floatPairQueue.removeFirst()
        } else {
            null
        }
    }

    // Méthode pour obtenir la taille de la file
    fun size(): Int {
        return floatPairQueue.size
    }

    // Méthode pour vérifier si la file est vide
    fun isEmpty(): Boolean {
        return floatPairQueue.isEmpty()
    }

    // Méthode pour obtenir le contenu de la file sous forme de liste
    fun toList(): List<Pair<Float, Float>> {
        return floatPairQueue.toList()
    }

}

