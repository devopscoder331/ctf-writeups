package app.blackhol3.ui.pubkey

import app.blackhol3.ui.common.TransitionMode

data class AnimationState(
    val pattern: Array<BooleanArray> = Array(15) { BooleanArray(15) { false } },
    val color: Int = 0,
    val transitionMode: TransitionMode = TransitionMode.NORMAL,
    val animationTrigger: Int = 0,
    val isAnimating: Boolean = false,
    val inverted: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnimationState

        if (!pattern.contentDeepEquals(other.pattern)) return false
        if (color != other.color) return false
        if (transitionMode != other.transitionMode) return false
        if (animationTrigger != other.animationTrigger) return false
        if (isAnimating != other.isAnimating) return false
        if (inverted != other.inverted) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pattern.contentDeepHashCode()
        result = 31 * result + color
        result = 31 * result + transitionMode.hashCode()
        result = 31 * result + animationTrigger
        result = 31 * result + isAnimating.hashCode()
        result = 31 * result + inverted.hashCode()
        return result
    }
}
