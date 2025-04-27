package whatis.love.agedate.kv

import kotlinx.serialization.json.Json
import whatis.love.agedate.api.client.pinning.PinningParameters
import whatis.love.agedate.api.client.pinning.PinningParametersPersistence
import javax.inject.Inject

private const val KV_TYPE_CONFIG = "config"
private const val KV_KEY_CONFIG_PINNING = "pinning"

class KVPinningPersistence
    @Inject
    constructor(
        val kv: KVStorage,
    ) : PinningParametersPersistence {
        override fun getPinningParameters(): PinningParameters {
            return kv
                .getString(KV_TYPE_CONFIG, KV_KEY_CONFIG_PINNING, 600)
                ?.let { return Json.decodeFromString<PinningParameters>(it) } ?: PinningParameters(
                null,
                null,
            )
        }

        override fun setPinningParameters(params: PinningParameters) {
            kv.setString(
                KV_TYPE_CONFIG,
                KV_KEY_CONFIG_PINNING,
                Json.encodeToString(params),
                ActionOnConflict.REPLACE,
            )
        }
    }
