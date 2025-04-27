package app.blackhol3.repository

import app.blackhol3.data.local.contract.ConfigContract
import app.blackhol3.data.local.dao.ConfigDao
import app.blackhol3.data.local.dao.PrivateKeyDao
import app.blackhol3.model.PrivateKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class PrivateKeyRepositoryImpl(
    val dao: PrivateKeyDao,
    val configDao: ConfigDao,
) : PrivateKeyRepository {
    private val keys = MutableStateFlow<List<PrivateKey>>(emptyList())
    private val current = MutableStateFlow<PrivateKey?>(null)

    init {
        refreshPrivateKeys()
        current.value = getCurrentPrivateKey()
    }

    override fun currentPrivateKey(): Flow<PrivateKey?> = current

    override fun getCurrentPrivateKey(): PrivateKey? {
        val currentPrivateKeyId =
            configDao.getString(ConfigContract.KEY_CURRENT_PRIVKEY_ID) ?: return null
        return getPrivateKey(currentPrivateKeyId)
    }

    override fun setCurrentPrivateKey(id: String) {
        val persistedKey = getPrivateKey(id)
        if (persistedKey != null) {
            configDao.putString(ConfigContract.KEY_CURRENT_PRIVKEY_ID, id)
        } else {
            configDao.remove(ConfigContract.KEY_CURRENT_PRIVKEY_ID)
        }
        current.value = persistedKey
    }

    override fun privateKeys(): Flow<List<PrivateKey>> = keys

    private fun refreshPrivateKeys() {
        keys.value = dao.listPrivateKeys()
    }

    override fun getPrivateKey(id: String): PrivateKey? = dao.getPrivateKey(id)

    override fun insertPrivateKey(privateKey: PrivateKey): Long {
        val result = dao.insertPrivateKey(privateKey)
        refreshPrivateKeys()
        return result
    }

    override fun replacePrivateKey(
        id: String,
        privateKey: PrivateKey,
    ): PrivateKey {
        val existing = getPrivateKey(id)!!
        val alteredKey =
            PrivateKey(existing.id, privateKey.privateKeyBytes, privateKey.keyPic)
        dao.insertPrivateKey(alteredKey)
        refreshPrivateKeys()
        return alteredKey
    }

    override fun deletePrivateKey(id: String) {
        dao.deletePrivateKey(id)
        refreshPrivateKeys()
    }
}
