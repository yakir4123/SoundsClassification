import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

fun convertPcmToWav(pcmFile: File, wavFile: File, sampleRate: Int, channels: Int, bitDepth: Int) {
    val byteRate = sampleRate * channels * (bitDepth / 8)
    val pcmData = ByteArray(pcmFile.length().toInt())

    try {
        FileInputStream(pcmFile).use { pcmInputStream ->
            pcmInputStream.read(pcmData)
        }

        FileOutputStream(wavFile).use { wavOutputStream ->
            // Write the WAV header
            wavOutputStream.write(createWavHeader(pcmData.size, sampleRate, channels, byteRate, bitDepth))

            // Write the PCM data
            wavOutputStream.write(pcmData)
        }

    } catch (e: IOException) {
        e.printStackTrace()
    }
}

// Create WAV header
fun createWavHeader(dataSize: Int, sampleRate: Int, channels: Int, byteRate: Int, bitDepth: Int): ByteArray {
    val header = ByteArray(44)

    // ChunkID "RIFF"
    header[0] = 'R'.code.toByte()
    header[1] = 'I'.code.toByte()
    header[2] = 'F'.code.toByte()
    header[3] = 'F'.code.toByte()

    // ChunkSize: 36 + dataSize
    val chunkSize = 36 + dataSize
    header[4] = (chunkSize and 0xff).toByte()
    header[5] = ((chunkSize shr 8) and 0xff).toByte()
    header[6] = ((chunkSize shr 16) and 0xff).toByte()
    header[7] = ((chunkSize shr 24) and 0xff).toByte()

    // Format "WAVE"
    header[8] = 'W'.code.toByte()
    header[9] = 'A'.code.toByte()
    header[10] = 'V'.code.toByte()
    header[11] = 'E'.code.toByte()

    // Subchunk1ID "fmt "
    header[12] = 'f'.code.toByte()
    header[13] = 'm'.code.toByte()
    header[14] = 't'.code.toByte()
    header[15] = ' '.code.toByte()

    // Subchunk1Size (16 for PCM)
    header[16] = 16
    header[17] = 0
    header[18] = 0
    header[19] = 0

    // AudioFormat (1 for PCM)
    header[20] = 1
    header[21] = 0

    // NumChannels
    header[22] = channels.toByte()
    header[23] = 0

    // SampleRate
    header[24] = (sampleRate and 0xff).toByte()
    header[25] = ((sampleRate shr 8) and 0xff).toByte()
    header[26] = ((sampleRate shr 16) and 0xff).toByte()
    header[27] = ((sampleRate shr 24) and 0xff).toByte()

    // ByteRate (SampleRate * NumChannels * BitsPerSample/8)
    header[28] = (byteRate and 0xff).toByte()
    header[29] = ((byteRate shr 8) and 0xff).toByte()
    header[30] = ((byteRate shr 16) and 0xff).toByte()
    header[31] = ((byteRate shr 24) and 0xff).toByte()

    // BlockAlign (NumChannels * BitsPerSample/8)
    val blockAlign = channels * (bitDepth / 8)
    header[32] = blockAlign.toByte()
    header[33] = 0

    // BitsPerSample
    header[34] = bitDepth.toByte()
    header[35] = 0

    // Subchunk2ID "data"
    header[36] = 'd'.code.toByte()
    header[37] = 'a'.code.toByte()
    header[38] = 't'.code.toByte()
    header[39] = 'a'.code.toByte()

    // Subchunk2Size (data size)
    header[40] = (dataSize and 0xff).toByte()
    header[41] = ((dataSize shr 8) and 0xff).toByte()
    header[42] = ((dataSize shr 16) and 0xff).toByte()
    header[43] = ((dataSize shr 24) and 0xff).toByte()

    return header
}
