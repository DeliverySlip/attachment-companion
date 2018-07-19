package cryptor

import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import java.nio.charset.Charset
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESSymmetricCryptor(password:String) : SymmetricCryptor {

    private val passwordHash:ByteArray
    private val secretKeySpec:SecretKeySpec

    init{

        //hash the password with SHA256
        passwordHash = DigestUtils.sha256(password)

        secretKeySpec = SecretKeySpec(passwordHash, "AES")

    }

    override fun encrypt(message:String):ByteArray{
        return encrypt(message.toByteArray(Charsets.UTF_8))
    }


    override fun encrypt(plaintext:ByteArray):ByteArray{

        val generator = SecureRandom()
        val buffer = ByteArray(16)
        generator.nextBytes(buffer)
        val iv = IvParameterSpec(buffer)

        //encrypt the message
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv)
        val cipherText = cipher.doFinal(plaintext)


        //append the IV to the end of the message
        val mergedBytes = ByteArray(iv.iv.size + cipherText.size)

        var index = 0
        cipherText.forEach {
            mergedBytes.set(index, it)
            index++
        }

        iv.iv.forEach {
            mergedBytes.set(index, it)
            index++
        }

        val encodedCipherText = Base64.encodeBase64(mergedBytes)
        return encodedCipherText
    }

    override fun decrypt(cipherText:ByteArray):ByteArray{


        val decodedCipherText = Base64.decodeBase64(cipherText)

        //get IV by taking it off the end of the cipherText
        val ivBytes = decodedCipherText.toList().subList(decodedCipherText.size - 16, decodedCipherText.size).toByteArray()
        val cipherTextBytes = decodedCipherText.toList().subList(0, decodedCipherText.size - 16).toByteArray()
        val iv = IvParameterSpec(ivBytes)

        //decrypt the message
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv)

        val plainText = cipher.doFinal(cipherTextBytes)

        return plainText
    }

    override fun decrypt(cipherText:String):ByteArray{
        return decrypt(cipherText.toByteArray(Charsets.UTF_8))
    }

}