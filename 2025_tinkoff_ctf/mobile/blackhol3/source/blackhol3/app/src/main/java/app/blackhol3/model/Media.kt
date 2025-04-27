package app.blackhol3.model

import kotlinx.serialization.Serializable

@Serializable
data class Media(
    val id: String,
    val mimeType: String,
    val sizeBytes: Long,
    val content: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Media

        if (sizeBytes != other.sizeBytes) return false
        if (id != other.id) return false
        if (mimeType != other.mimeType) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sizeBytes.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + (content?.contentHashCode() ?: 0)
        return result
    }
}
