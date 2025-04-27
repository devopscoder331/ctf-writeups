package whatis.love.agedate.profile.ui

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class MicrostatusShape(
    private val cornerRadius: Float = 12f,
    private val arrowPosition: ArrowPosition = ArrowPosition.BOTTOM_END,
    private val arrowWidth: Float = 24f,
    private val arrowHeight: Float = 14f,
) : Shape {
    enum class ArrowPosition {
        TOP_START,
        TOP_END,
        BOTTOM_START,
        BOTTOM_END,
    }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline =
        Outline.Generic(
            path = drawBubblePath(size, layoutDirection),
        )

    private fun drawBubblePath(
        size: Size,
        layoutDirection: LayoutDirection,
    ): Path {
        val effectiveArrowPosition =
            when {
                layoutDirection == LayoutDirection.Rtl && arrowPosition == ArrowPosition.TOP_START -> ArrowPosition.TOP_END
                layoutDirection == LayoutDirection.Rtl && arrowPosition == ArrowPosition.TOP_END -> ArrowPosition.TOP_START
                layoutDirection == LayoutDirection.Rtl && arrowPosition == ArrowPosition.BOTTOM_START -> ArrowPosition.BOTTOM_END
                layoutDirection == LayoutDirection.Rtl && arrowPosition == ArrowPosition.BOTTOM_END -> ArrowPosition.BOTTOM_START
                else -> arrowPosition
            }

        return Path().apply {
            val startX: Float
            val startY: Float

            when (effectiveArrowPosition) {
                ArrowPosition.TOP_START -> {
                    startX = 0f
                    startY = 0f
                }

                ArrowPosition.TOP_END -> {
                    startX = cornerRadius
                    startY = 0f
                }

                ArrowPosition.BOTTOM_START -> {
                    startX = cornerRadius
                    startY = 0f
                }

                ArrowPosition.BOTTOM_END -> {
                    startX = cornerRadius
                    startY = 0f
                }
            }
            moveTo(startX, startY)
            if (effectiveArrowPosition == ArrowPosition.TOP_START) {
                lineTo(arrowWidth, 0f)
                lineTo(0f, -arrowHeight)
                lineTo(0f, 0f)
            }
            lineTo(size.width - cornerRadius, 0f)
            if (effectiveArrowPosition == ArrowPosition.TOP_END) {
                lineTo(size.width, 0f)
                lineTo(size.width + arrowHeight, 0f)
                lineTo(size.width, arrowWidth)
            } else {
                arcTo(
                    rect =
                        Rect(
                            left = size.width - 2 * cornerRadius,
                            top = 0f,
                            right = size.width,
                            bottom = 2 * cornerRadius,
                        ),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )
            }
            lineTo(size.width, size.height - cornerRadius)
            if (effectiveArrowPosition == ArrowPosition.BOTTOM_END) {
                lineTo(size.width, size.height)
                lineTo(size.width, size.height + arrowHeight)
                lineTo(size.width - arrowWidth, size.height)
            } else {
                arcTo(
                    rect =
                        Rect(
                            left = size.width - 2 * cornerRadius,
                            top = size.height - 2 * cornerRadius,
                            right = size.width,
                            bottom = size.height,
                        ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )
            }
            lineTo(cornerRadius, size.height)
            if (effectiveArrowPosition == ArrowPosition.BOTTOM_START) {
                lineTo(0f, size.height)
                lineTo(-arrowHeight, size.height)
                lineTo(0f, size.height - arrowWidth)
            } else {
                arcTo(
                    rect =
                        Rect(
                            left = 0f,
                            top = size.height - 2 * cornerRadius,
                            right = 2 * cornerRadius,
                            bottom = size.height,
                        ),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )
            }
            lineTo(0f, cornerRadius)

            if (effectiveArrowPosition != ArrowPosition.TOP_START) {
                arcTo(
                    rect =
                        Rect(
                            left = 0f,
                            top = 0f,
                            right = 2 * cornerRadius,
                            bottom = 2 * cornerRadius,
                        ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false,
                )
            } else {
                lineTo(0f, 0f)
            }

            close()
        }
    }
}
