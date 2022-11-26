package io.github.copecone.audiogen.util

import kotlin.random.Random

@Suppress("unused")
object WaveUtil {

    fun convertFrequency(frequency: Double) = frequency * MathConstants.TAU
    fun convertFrequency(frequency: Int) = frequency * MathConstants.TAU

    fun noise(scale: Double = 1.0) = if (scale != 0.0) { Random.nextDouble(-scale, scale) } else { 0.0 }

}