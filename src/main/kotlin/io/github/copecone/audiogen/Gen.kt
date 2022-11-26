package io.github.copecone.audiogen

import io.github.copecone.audiogen.util.MathConstants
import io.github.copecone.audiogen.util.WaveUtil
import io.github.copecone.audiogen.util.WaveUtil.noise
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.ByteBuffer
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

lateinit var audioThread: AudioThread

const val SAMPLE_RATE = 48000f
val signedAudioFormat = AudioFormat(SAMPLE_RATE, 16, 1, true, true)
fun main() {
    var time = 0.0
    //var audioFrame = 0

    val audioBuffer = ByteBuffer.allocate(AudioThread.bufferSize * 2)
    val audioData = ArrayList<Byte>()

    audioThread = AudioThread(SAMPLE_RATE) {
        val chunkBuffer = ShortArray(AudioThread.bufferSize)

        println("New Chunk at ${time.format(5)}")
        for (i in 0 until AudioThread.bufferSize) {
            val signal = getSignal(time)
            if (signal == Double.MIN_VALUE) { closeThis(); break }
            chunkBuffer[i] = (signal * Short.MAX_VALUE).toInt().toShort()
            audioBuffer.putShort(chunkBuffer[i])
            time += 1.0 / SAMPLE_RATE
        }

        audioData.addAll(audioBuffer.array().asList())
        audioBuffer.clear()
        chunkBuffer
    }

    audioThread.triggerPlayback()
    while (!audioThread.closed) { Thread.sleep(1) }

    val file = File("output.wav")
    val byteArrayInputStream = ByteArrayInputStream(audioData.toByteArray())
    AudioSystem.write(
        AudioInputStream(
            byteArrayInputStream, signedAudioFormat, audioData.size.toLong()
        ), AudioFileFormat.Type.WAVE, file
    )
}

fun Double.format(digit: Int) = "%.${digit}f".format(this)

fun closeThis() {
    audioThread.close()
}

fun getSignal(time: Double) = if (time < 10) {
    val hertz = 200 * cos(time * MathConstants.TAU * 2.5) / (time.pow(1.2) * 2)
    val data = sin(
        WaveUtil.convertFrequency(hertz) * time
    ) * 0.6 + noise(time * 0.005)
    //val data = noise(0.002)

    data
} else {
    Double.MIN_VALUE
}

/*fun generateAudio(
    sampleRate: Float = 44100.0f, channel: Int = 1,
    bufferSize: Int = 8
): ShortArray {
    val audioData = ArrayList<Short>()
    val audioBytes = ArrayList<Byte>()
    val signedAudioFormat = AudioFormat(sampleRate, 16, channel, true, true)
    //val clip = AudioSystem.getClip()
    val sdl = AudioSystem.getSourceDataLine(signedAudioFormat)

    val sampleSize = 2

    sdl.open()
    sdl.start()

    var time = 0.0
    var chunkID = 0
    var bufferCursor = 0
    val audioBuffer = ByteBuffer.allocate(bufferSize)
    do {
        if (bufferCursor == 0) {
            if (chunkID != 0) {
                while (sdl.bufferSize - sdl.available() > bufferSize) { Thread.sleep(1) }
            }

            audioBuffer.clear()
        }

        val currData = genSignal(time)
        val calculatedData = (currData * Short.MAX_VALUE).toInt().toShort()
        audioBuffer.putShort(calculatedData)
        audioData.add(calculatedData)
        time += 1.0 / sampleRate

        bufferCursor++
        if (bufferCursor >= bufferSize / sampleSize) {
            audioBytes.addAll(audioBuffer.array().asList())
            sdl.write(audioBuffer.array(), 0, audioBuffer.position())
            audioBuffer.clear()

            bufferCursor = 0
            chunkID++
        }

        //if (sdl.bufferSize == sdl.available()) { println("*") }
    } while (currData != Double.MIN_VALUE)

    //println(audioData.size)
    //clip.open(signedAudioFormat, audioData.toByteArray(), 0, audioData.size)
    //clip.start()

    //Thread.sleep((audioData.size / sampleRate * 1000).toLong())
    //clip.drain()
    sdl.drain()
    sdl.stop()

    val file = File("output.wav")
    val byteArrayInputStream = ByteArrayInputStream(audioBytes.toByteArray())
    AudioSystem.write(
        AudioInputStream(
            byteArrayInputStream, signedAudioFormat, audioData.size.toLong()
        ), AudioFileFormat.Type.WAVE, file
    )
    sdl.close()

    return audioData.toShortArray()
}*/