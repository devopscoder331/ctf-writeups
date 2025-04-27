package app.blackhol3.data.local.dao

import app.blackhol3.model.PrivateKey

interface PrivateKeyDao {
    fun listPrivateKeys(): List<PrivateKey>

    fun getPrivateKey(id: String): PrivateKey?

    fun insertPrivateKey(privateKey: PrivateKey): Long

    fun deletePrivateKey(id: String)
}
