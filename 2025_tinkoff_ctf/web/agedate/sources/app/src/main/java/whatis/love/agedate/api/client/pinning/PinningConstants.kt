package whatis.love.agedate.api.client.pinning

val PinningParametersPublicKey =
    publicKeyFromPem(
        """
        -----BEGIN PUBLIC KEY-----
        MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAx8F7/KfSNgldmKgn6DxG
        1kAuV7lH761obUR2aklWZqjbIenYd8tRm3HkB8pVZ6n5LEA8zXKGg2MHwehISHJU
        39b3VMVgRAmDjHxZktgLjEVjI4Jgg0xNyb+hAAW6S1zmrn5qwMaznfH2OkhAnUl3
        yRwkTT52SezedKhV+l2aXZWS2p07eXzhbLw3Ask3LcbdUgOUpMCBxBH+qMIlDTwh
        pZLuP7nAnu90XeueEVEbqjWLhOY/bgc1sjnxRTV+bLYih9qvElko6OaTgkf7Ivev
        KFh+2WS83rP/hTEzPDMsSfMcYvCnlfKv7Z6WPPY4nLu5Tk/2ikD9pmzdjTm6u9a9
        FHFC3p+M1Ctf7c+mx4g/TztYcKJT8cslW2C2TqVIFiz0xRVKZMs9uyO2bu0bFids
        R+Fa0eCwnhla7RgxVPlyjVGaCP7Xl/FYEE26ZPHBsZpE71OEsjCMmOmXkz32ekWt
        SOKhYhyTg2R1B7OjpkJVoOizMFb5WX7KV1+MSagrwLCl0JEB016aevL2SDjCj0Aw
        CEWzl5Tw+PoLxoqbJ0jyfJX9Jz6Km4ZGckbmMM11nLDBCwtol9Urhn7RAEr12UFw
        jsUUBQybjBL2ZdsZnRWRIGvXEE6n+fllmQsp19zue9CdSrJw7WPzMpHyb9uDXsIO
        ac5VE2h/CMcqYer6ckWBUQ8CAwEAAQ==
        -----END PUBLIC KEY-----
        """.trimIndent(),
    )

const val PinningParametersEndpoint = "http://t-romance-eq0cx4w3.spbctf.org/pinning"
