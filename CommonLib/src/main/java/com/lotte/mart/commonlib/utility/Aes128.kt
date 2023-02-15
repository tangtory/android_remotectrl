package com.lotte.mart.commonlib.utility

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

//var aes128 = Aes128()
//var result = aes128.decrypt(ftpId)
class Aes128 {
    var key = "lMd5ZfHhBqCJF5ohNvqSVdYkE0ogdyb2"
    /**
     * 암호화
     *
     * @param input
     * @return
     */
    fun encrypt(input: String): String {
        var crypted: ByteArray? = null
        try {
            val skey =
                SecretKeySpec(key.toByteArray(), "AES")
            val cipher =
                Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, skey)
            crypted = cipher.doFinal(input.toByteArray())
        } catch (e: Exception) {
            println(e.toString())
        }
//        val encoder = BASE64Encoder()
        val str: String = Base64.encodeToString(crypted, Base64.NO_WRAP)
        return str
    }

    /**
     * 복호화
     *
     * @param input
     * @param key
     * @return
     */
    fun decrypt(input: String?): String {
        var output: ByteArray? = null
        try {
            val skey =
                SecretKeySpec(key.toByteArray(), "AES")
            val cipher =
                Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, skey)
            //decoder.decodeBuffer(input)
            //Charset.defaultCharset()
            output = cipher.doFinal(Base64.decode(input!!.toByteArray(),0))
        } catch (e: Exception) {
            println(e.toString())
        }
        return String(output!!)
    }

}