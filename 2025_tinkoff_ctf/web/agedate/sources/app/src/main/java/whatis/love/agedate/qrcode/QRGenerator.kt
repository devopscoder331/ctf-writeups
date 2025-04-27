package whatis.love.agedate.qrcode

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import whatis.love.agedate.R
import whatis.love.agedate.api.model.Profile

fun generateQRCode(
    context: Context,
    qrData: String,
    appLogoResId: Int,
    codeGradientStartColor: Int = "#FF5722".toColorInt(),
    codeGradientEndColor: Int = "#9C27B0".toColorInt(),
    logoGradientStartColor: Int = "#FFD0B5".toColorInt(),
    logoGradientEndColor: Int = "#FFAF8C".toColorInt(),
): Bitmap? {
    try {
        val hints =
            mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8",
            )

        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 512, 512, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val finalQR = createBitmap(width, height)
        val canvas = Canvas(finalQR)
        val paint =
            android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
            }
        paint.color = Color.White.toArgb()
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        val roundPaint =
            android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
            }
        val cornerRadius = 8f

        for (y in 0 until height) {
            for (x in 0 until width) {
                if (!bitMatrix[x, y]) continue
                val leftFilled = x > 0 && bitMatrix[x - 1, y]
                val rightFilled = x < width - 1 && bitMatrix[x + 1, y]
                val topFilled = y > 0 && bitMatrix[x, y - 1]
                val bottomFilled = y < height - 1 && bitMatrix[x, y + 1]
                val rect =
                    android.graphics.RectF(
                        x.toFloat(),
                        y.toFloat(),
                        (x + 1).toFloat(),
                        (y + 1).toFloat(),
                    )
                val topLeftRadius = if (!leftFilled && !topFilled) cornerRadius else 0f
                val topRightRadius = if (!rightFilled && !topFilled) cornerRadius else 0f
                val bottomLeftRadius = if (!leftFilled && !bottomFilled) cornerRadius else 0f
                val bottomRightRadius = if (!rightFilled && !bottomFilled) cornerRadius else 0f
                val gradientProgress = (x.toFloat() + y.toFloat()) / (width + height).toFloat()
                val gradientColor =
                    interpolateColor(
                        codeGradientStartColor,
                        codeGradientEndColor,
                        gradientProgress,
                    )
                roundPaint.color = gradientColor
                val path = android.graphics.Path()
                path.addRoundRect(
                    rect,
                    floatArrayOf(
                        topLeftRadius,
                        topLeftRadius,
                        topRightRadius,
                        topRightRadius,
                        bottomRightRadius,
                        bottomRightRadius,
                        bottomLeftRadius,
                        bottomLeftRadius,
                    ),
                    android.graphics.Path.Direction.CW,
                )
                canvas.drawPath(path, roundPaint)
            }
        }
        val centerRegion = width / 4
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = centerRegion / 2f
        val radialGradient =
            android.graphics.RadialGradient(
                centerX,
                centerY,
                radius,
                intArrayOf(
                    Color.White.toArgb(),
                    Color.White.toArgb(),
                    Color.White.copy(alpha = 0.8f).toArgb(),
                ),
                floatArrayOf(0f, 0.7f, 1f),
                android.graphics.Shader.TileMode.CLAMP,
            )
        paint.shader = radialGradient
        canvas.drawCircle(centerX, centerY, radius, paint)
        paint.shader = null
        try {
            val logoDrawable = ContextCompat.getDrawable(context, appLogoResId)?.mutate()
            val logoSize = (centerRegion * 0.7f).toInt()
            val logoLeft = (width - logoSize) / 2
            val logoTop = (height - logoSize) / 2
            logoDrawable?.setBounds(
                logoLeft,
                logoTop,
                logoLeft + logoSize,
                logoTop + logoSize,
            )
            val logoBitmap = createBitmap(logoSize, logoSize)
            val logoCanvas = Canvas(logoBitmap)
            val logoPaint =
                android.graphics.Paint().apply {
                    isAntiAlias = true
                    style = android.graphics.Paint.Style.FILL
                    shader =
                        android.graphics.LinearGradient(
                            0f,
                            0f,
                            0f,
                            logoSize.toFloat(),
                            logoGradientStartColor,
                            logoGradientEndColor,
                            android.graphics.Shader.TileMode.CLAMP,
                        )
                }
            logoCanvas.drawCircle(
                logoSize / 2f,
                logoSize / 2f,
                logoSize / 2f,
                logoPaint,
            )
            logoDrawable?.let {
                it.setBounds(0, 0, logoSize, logoSize)
                it.draw(logoCanvas)
            }
            val alphaPaint =
                android.graphics.Paint().apply {
                    isAntiAlias = true
                    alpha = 230
                }
            canvas.drawBitmap(logoBitmap, logoLeft.toFloat(), logoTop.toFloat(), alphaPaint)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return finalQR
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

private fun interpolateColor(
    startColor: Int,
    endColor: Int,
    fraction: Float,
): Int {
    val startA = android.graphics.Color.alpha(startColor)
    val startR = android.graphics.Color.red(startColor)
    val startG = android.graphics.Color.green(startColor)
    val startB = android.graphics.Color.blue(startColor)

    val endA = android.graphics.Color.alpha(endColor)
    val endR = android.graphics.Color.red(endColor)
    val endG = android.graphics.Color.green(endColor)
    val endB = android.graphics.Color.blue(endColor)

    val a = (startA + (endA - startA) * fraction).toInt()
    val r = (startR + (endR - startR) * fraction).toInt()
    val g = (startG + (endG - startG) * fraction).toInt()
    val b = (startB + (endB - startB) * fraction).toInt()

    return android.graphics.Color.argb(a, r, g, b)
}

fun generateProfileQRCode(
    context: Context,
    profile: Profile,
    applyGradient: Boolean = true,
): Bitmap? =
    generateQRCode(
        context,
        "agedate://profile/${profile.id}",
        R.drawable.ic_launcher_foreground,
        codeGradientStartColor = (if (applyGradient) "#FF5722" else "#000000").toColorInt(),
        codeGradientEndColor = (if (applyGradient) "#9C27B0" else "#000000").toColorInt(),
        logoGradientStartColor = (if (applyGradient) "#FFD0B5" else "#000000").toColorInt(),
        logoGradientEndColor = (if (applyGradient) "#FFAF8C" else "#000000").toColorInt(),
    )
