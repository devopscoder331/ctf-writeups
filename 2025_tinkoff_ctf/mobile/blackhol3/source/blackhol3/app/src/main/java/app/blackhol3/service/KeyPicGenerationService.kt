package app.blackhol3.service

import android.graphics.Bitmap
import app.blackhol3.model.KeyPicVizData
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import kotlin.random.Random

interface KeyPicGenerationService {
    fun visualizePublicKey(publicKey: RSAPublicKey): Bitmap

    fun visualizePrivateKey(privateKey: RSAPrivateKey): Bitmap

    fun publicKeyVizData(publicKey: RSAPublicKey): KeyPicVizData

    fun privateKeyVizData(privateKey: RSAPrivateKey): KeyPicVizData

    fun render(data: KeyPicVizData): Bitmap

    fun generateColor(random: Random): Int
}
