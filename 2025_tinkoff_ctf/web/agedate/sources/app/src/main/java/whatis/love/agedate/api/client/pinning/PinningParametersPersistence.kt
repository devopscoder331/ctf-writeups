package whatis.love.agedate.api.client.pinning

interface PinningParametersPersistence {
    fun getPinningParameters(): PinningParameters

    fun setPinningParameters(params: PinningParameters)
}
