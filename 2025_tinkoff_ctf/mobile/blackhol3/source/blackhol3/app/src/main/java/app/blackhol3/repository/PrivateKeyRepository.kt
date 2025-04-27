package app.blackhol3.repository

import app.blackhol3.model.PrivateKey
import kotlinx.coroutines.flow.Flow

interface PrivateKeyRepository {
    fun privateKeys(): Flow<List<PrivateKey>>

    fun getPrivateKey(id: String): PrivateKey?

    fun currentPrivateKey(): Flow<PrivateKey?>

    fun getCurrentPrivateKey(): PrivateKey?

    fun setCurrentPrivateKey(id: String)

    fun insertPrivateKey(privateKey: PrivateKey): Long

    fun replacePrivateKey(
        id: String,
        privateKey: PrivateKey,
    ): PrivateKey

    fun deletePrivateKey(id: String)
}
