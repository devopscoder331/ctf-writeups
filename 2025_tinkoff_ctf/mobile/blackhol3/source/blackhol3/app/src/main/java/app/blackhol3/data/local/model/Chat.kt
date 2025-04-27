package app.blackhol3.data.local.model

import app.blackhol3.model.PublicKey

data class Chat(
    val id: String,
    val privKeyId: String,
    val name: String?,
    val pubKey: PublicKey,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chat

        if (id != other.id) return false
        if (privKeyId != other.privKeyId) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + privKeyId.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }
}
