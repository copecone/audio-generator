package io.github.copecone.audiogen

import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10.*
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10.*
import java.util.function.Supplier

class AudioThread(private val SAMPLE_RATE: Float, private val bufferSupplier: Supplier<ShortArray>) : Thread() {
    companion object {
        const val bufferSize = 1024
    }

    private val bufferCount = 4

    private var buffers = IntArray(bufferCount)
    private var device = alcOpenDevice(alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER))
    private var context = alcCreateContext(device, IntArray(1))

    private var bufferIndex = 0
    private var source: Int

    var closed: Boolean = false
    private var running: Boolean = false

    init {
        alcMakeContextCurrent(context)
        AL.createCapabilities(ALC.createCapabilities(device))
        source = alGenSources()

        for (i in 0 until bufferCount) {
            bufferSamples(ShortArray(0))
        }

        alSourcePlay(source)
        catchInternalException()
        start()
    }

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @Synchronized override fun run() {
        while (!closed) {
            while (!running) { (this as Object).wait() }
            val processedBuffers = alGetSourcei(source, AL_BUFFERS_PROCESSED)

            for (i in 0 until processedBuffers) {
                val samples = bufferSupplier.get()

                alDeleteBuffers(alSourceUnqueueBuffers(source))
                buffers[bufferIndex] = alGenBuffers()
                bufferSamples(samples)
            }

            if (alGetSourcei(source, AL_SOURCE_STATE) != AL_PLAYING) {
                alSourcePlay(source)
            }
            catchInternalException()
        }

        alDeleteSources(source)
        alDeleteBuffers(buffers)
        alcDestroyContext(context)
        alcCloseDevice(device)
    }

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @Synchronized fun triggerPlayback() {
        running = true
        (this as Object).notify()
    }

    fun close() {
        closed = true
        triggerPlayback()
    }

    private fun bufferSamples(samples: ShortArray) {
        val buf = buffers[bufferIndex++]
        alBufferData(buf, AL_FORMAT_MONO16, samples, SAMPLE_RATE.toInt())
        alSourceQueueBuffers(source, buf)

        bufferIndex %= bufferCount
    }

    private fun catchInternalException() {
        val err = alcGetError(device)
        if (err != ALC_NO_ERROR) {
            throw OpenALException(err)
        }
    }

    class OpenALException(errorCode: Int) : RuntimeException(
        "OpenAL Error: ${
            when (errorCode) {
                AL_INVALID_NAME -> "Invalid Name"
                AL_INVALID_ENUM -> "Invalid Enum"
                AL_INVALID_VALUE -> "Invalid Value"
                AL_INVALID_OPERATION -> "Invalid Operation"
                else -> "Unknown"
            }
        }"
    )
}