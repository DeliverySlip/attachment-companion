package cryptor

import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class DESSymmetricCryptor(password:String):SymmetricCryptor {

    private val passwordHash:ByteArray
    private val secretKeySpec: SecretKeySpec


    init {
        passwordHash = DigestUtils.sha256(password)

        val passBytes = ByteArray(8)
        for (i in 0..7){
            passBytes[i] = passwordHash[i]
        }

        secretKeySpec = SecretKeySpec(passBytes, "DES")
    }

    override fun encrypt(message: String): ByteArray {
        return encrypt(message.toByteArray(Charsets.UTF_8))
    }

    override fun encrypt(plaintext: ByteArray): ByteArray {

        //encrypt the message
        val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        val cipherText = cipher.doFinal(plaintext)

        val encodedCipherText = Base64.encodeBase64(cipherText)
        return encodedCipherText

    }

    override fun decrypt(cipherText: ByteArray): ByteArray {

        val decodedCipherText = Base64.decodeBase64(cipherText)

        val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)

        val plainText = cipher.doFinal(decodedCipherText)

        return plainText
    }

    override fun decrypt(cipherText: String): ByteArray {
        return decrypt(cipherText.toByteArray(Charsets.UTF_8))
    }


}