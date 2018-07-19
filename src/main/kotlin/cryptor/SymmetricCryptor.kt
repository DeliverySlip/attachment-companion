package cryptor

interface SymmetricCryptor {

    fun encrypt(message:String):ByteArray

    fun encrypt(plaintext:ByteArray):ByteArray

    fun decrypt(cipherText:ByteArray):ByteArray

    fun decrypt(cipherText:String):ByteArray
}