package app.blackhol3.service

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import app.blackhol3.model.KeyPicVizData
import app.blackhol3.util.fromByteArray
import java.math.BigInteger
import java.security.MessageDigest
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.random.Random

class KeyPicGenerationServiceImpl : KeyPicGenerationService {
    val blockSizes =
        mapOf<Int, Int>(
            1024 to 11,
            2048 to 13,
            3072 to 15,
            4096 to 17,
        )

    override fun visualizePublicKey(publicKey: RSAPublicKey): Bitmap = visualizeModulus(publicKey.modulus)

    override fun visualizePrivateKey(privateKey: RSAPrivateKey): Bitmap = visualizeModulus(privateKey.modulus)

    override fun publicKeyVizData(publicKey: RSAPublicKey): KeyPicVizData = modulusVizData(publicKey.modulus)

    override fun privateKeyVizData(privateKey: RSAPrivateKey): KeyPicVizData = modulusVizData(privateKey.modulus)

    private fun modulusSeededRandom(modulus: BigInteger): Random {
        val modulusBytes = modulus.toByteArray()
        val hash = MessageDigest.getInstance("SHA-256").digest(modulusBytes)
        val seed = hash.copyOfRange(0, 16)
        val longSeed = Long.fromByteArray(seed)
        return Random(longSeed)
    }

    private fun modulusVizData(modulus: BigInteger): KeyPicVizData {
        val keyBytes = modulus.toByteArray().size * 8
        val blocks = blockSizes.minBy { item -> abs(item.key - keyBytes) }.value
        val random = modulusSeededRandom(modulus)
        val color = generateColor(random)
        val pattern = generatePattern(random, blocks)
        return KeyPicVizData(blocks, color, pattern)
    }

    private fun visualizeModulus(modulus: BigInteger): Bitmap = render(modulusVizData(modulus))

    override fun render(data: KeyPicVizData): Bitmap {
        val side = 640
        val bitmap = createBitmap(side, side)
        val canvas = Canvas(bitmap)
        draw(canvas, data)
        return bitmap
    }

    override fun generateColor(random: Random): Int {
        val hue = random.nextFloat() * 360f
        val saturation = 0.85f + (random.nextFloat() * 0.15f)
        val targetBrightness = 0.65f + (random.nextFloat() * 0.15f)

        val isBlueRange = (hue >= 210f && hue <= 270f)
        val adjustedTargetBrightness =
            if (isBlueRange) {
                targetBrightness + 0.15f
            } else {
                targetBrightness
            }

        val tempHsv = floatArrayOf(hue, saturation, 1.0f)
        val tempColor = Color.HSVToColor(tempHsv)

        val r = Color.red(tempColor) / 255f
        val g = Color.green(tempColor) / 255f
        val b = Color.blue(tempColor) / 255f

        val maxPerceivedBrightness = 0.26f * r + 0.67f * g + 0.07f * b

        val valueFactor =
            if (maxPerceivedBrightness > 0) {
                adjustedTargetBrightness / maxPerceivedBrightness
            } else {
                1.0f
            }

        val greenDominance = if (g > r && g > b) g - r.coerceAtLeast(b) else 0f

        val isCyanRange = hue >= 160f && hue <= 200f
        val cyanAdjustment = if (isCyanRange) 0.7f else 1.0f

        val colorAdjustment = (1.0f - (greenDominance * 0.3f)) * cyanAdjustment

        val finalValue = 1.0f.coerceAtMost(0.0f.coerceAtLeast(valueFactor * colorAdjustment))

        val finalHsv = floatArrayOf(hue, saturation, finalValue)
        return Color.HSVToColor(finalHsv)
    }

    private fun generatePattern(
        random: Random,
        blocks: Int,
    ): Array<BooleanArray> {
        val pattern = Array(blocks) { BooleanArray(blocks) }
        val parts = 7
        val partSize = blocks / parts
        for (i in 0 until blocks) {
            val inPart = (i > partSize && i < (parts - 1) * partSize)
            val lower = if (inPart) i % 2 else 3
            val upper = if (inPart) blocks - (i % 2) else blocks - 3
            for (j in lower until upper) {
                val value = random.nextBoolean()
                pattern[i][j] = value
                pattern[i][blocks - 1 - j] = value
            }
        }
        return pattern
    }

    private fun draw(
        canvas: Canvas,
        vizData: KeyPicVizData,
    ) {
        val blocks = vizData.blocks
        val blockSize = ceil(canvas.width / (blocks * 1.0f))
        for (i in 0 until blocks) {
            for (j in 0 until blocks) {
                if (vizData.pattern[i][j]) {
                    val paint = Paint()
                    paint.color = vizData.color
                    canvas.drawRect(
                        j * blockSize,
                        i * blockSize,
                        (j + 1) * blockSize,
                        (i + 1) * blockSize,
                        paint,
                    )
                }
            }
        }
    }
}
