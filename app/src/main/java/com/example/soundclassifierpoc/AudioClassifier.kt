import android.content.Context
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioClassifier(context: Context) {

    private val model: Module

    init {
        // Load the model from the assets folder
        model = Module.load(assetFilePath(context, "model.pt"))
    }

    // Function to classify PCM raw data
    fun classify(pcmData: ShortArray): Int {
        // Preprocess PCM data to float array (adjust depending on model's input)
        val floatArray = FloatArray(pcmData.size) { i ->
            pcmData[i] / 32768.0f  // Normalize PCM 16-bit data to [-1, 1] range
        }

        // Create input tensor with correct shape (e.g., [1, dataLength])
        val inputTensor = Tensor.fromBlob(floatArray, longArrayOf(1, pcmData.size.toLong()))

        // Run inference
        val outputTensor = model.forward(IValue.from(inputTensor)).toTensor()

        // Retrieve output, apply threshold to get binary classification
        val outputData = outputTensor.dataAsFloatArray
        val label = if (outputData[0] > 0.5) 1 else 0  // Assuming binary output

        return label
    }

    // Helper function to load model from assets
    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (!file.exists()) {
            context.assets.open(assetName).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        return file.absolutePath
    }
}
