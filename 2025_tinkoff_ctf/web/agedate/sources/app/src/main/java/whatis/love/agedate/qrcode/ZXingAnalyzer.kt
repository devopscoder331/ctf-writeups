package whatis.love.agedate.qrcode

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class ZXingAnalyzer(
    private val validCodePredicate: (String) -> Boolean = { true },
    private val onQRCodeScanned: (String) -> Unit,
) : ImageAnalysis.Analyzer {
    private val reader =
        MultiFormatReader().apply {
            val hints =
                mapOf(
                    DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE),
                    DecodeHintType.ALSO_INVERTED to true,
                )
            setHints(hints)
        }

    override fun analyze(image: ImageProxy) {
        val buffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        val width = image.width
        val height = image.height
        val source =
            PlanarYUVLuminanceSource(
                data,
                width,
                height,
                0,
                0,
                width,
                height,
                false,
            )

        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        val result =
            try {
                reader.decode(binaryBitmap)
            } catch (e: NotFoundException) {
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                image.close()
            }

        result?.let {
            if (validCodePredicate(it.text)) {
                onQRCodeScanned(it.text)
            }
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }
}
