package app.blackhol3.model

data class KeyPicVizData(
    val blocks: Int,
    val color: Int,
    val pattern: Array<BooleanArray>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KeyPicVizData

        if (color != other.color) return false
        if (!pattern.contentDeepEquals(other.pattern)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = color
        result = 31 * result + pattern.contentDeepHashCode()
        return result
    }
}
