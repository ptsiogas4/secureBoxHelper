package ptsiogas.gr.securebox

import android.content.Context
import android.util.Log
import java.io.IOException
import java.io.ObjectInputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.io.ObjectOutputStream


class SecureBoxHelper {
    companion object {
        private var secureBoxHelper: SecureBoxHelper? = null

        val instance: SecureBoxHelper
            get() {
                if (secureBoxHelper == null) {
                    secureBoxHelper = SecureBoxHelper()
                }
                return secureBoxHelper!!
            }
    }

    private var context: Context? = null

    // only call from App!
    fun init(appContext: Context) {
        this.context = appContext
    }

    private fun checkInit(): Boolean {
        if (this.context == null) {
            Log.e("SecureBoxHelper", "You should first call init method before using the helper!!!")
            return false
        }
        return true
    }

    fun encryptString(variableName: String, plainText: String, passwordString: String): Boolean {
        if (!checkInit()) {
            return false
        }
        try {
            val map = encryptString(plainText.toByteArray(), passwordString)
            val fos = context!!.openFileOutput(variableName + ".dat", Context.MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(map)
            oos.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("SecureBoxHelper", e.message)
        }
        return false
    }

    fun decryptString(variableName: String, passwordString: String): String? {
        if (!checkInit()) {
            return null
        }
        try {
            val fileInputStream = context!!.openFileInput(variableName + ".dat")
            val objectInputStream = ObjectInputStream(fileInputStream)
            val map = objectInputStream.readObject() as HashMap<String, ByteArray>
            val decryptedByteArray = decryptString(map, passwordString)
            if (decryptedByteArray != null) {
                return String(decryptedByteArray)
            } else {
                Log.e("SecureBoxHelper", "Something went wrong! Either variable name or password incorrect")
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            Log.e("SecureBoxHelper", e.message)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("SecureBoxHelper", e.message)
        } catch (e: ClassCastException) {
            e.printStackTrace()
            Log.e("SecureBoxHelper", e.message)
        }
        return null
    }

    private fun encryptString(plainTextBytes: ByteArray, passwordString: String): HashMap<String, ByteArray> {
        val map = HashMap<String, ByteArray>()

        try {
            //Random salt for next step
            val random = SecureRandom()
            val salt = ByteArray(256)
            random.nextBytes(salt)

            //PBKDF2 - derive the key from the password, don't use passwords directly
            val passwordChar = passwordString.toCharArray() //Turn password into char[] array
            val pbKeySpec = PBEKeySpec(passwordChar, salt, 1324, 256) //1324 iterations
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).getEncoded()
            val keySpec = SecretKeySpec(keyBytes, "AES")

            //Create initialization vector for AES
            val ivRandom = SecureRandom() //not caching previous seeded instance of SecureRandom
            val iv = ByteArray(16)
            ivRandom.nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)

            //Encrypt
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encrypted = cipher.doFinal(plainTextBytes)

            map["salt"] = salt
            map["iv"] = iv
            map["encrypted"] = encrypted
        } catch (e: Exception) {
            Log.e("MYAPP", "encryption exception", e)
        }

        return map
    }

    private fun decryptString(map: HashMap<String, ByteArray>, passwordString: String): ByteArray? {
        var decrypted: ByteArray? = null
        try {
            val salt = map["salt"]
            val iv = map["iv"]
            val encrypted = map["encrypted"]

            //regenerate key from password
            val passwordChar = passwordString.toCharArray()
            val pbKeySpec = PBEKeySpec(passwordChar, salt, 1324, 256)
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
            val keySpec = SecretKeySpec(keyBytes, "AES")

            //Decrypt
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            decrypted = cipher.doFinal(encrypted)
        } catch (e: Exception) {
            Log.e("MYAPP", "decryption exception", e)
        }

        return decrypted
    }
}