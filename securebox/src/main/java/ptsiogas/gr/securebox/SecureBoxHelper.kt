package ptsiogas.gr.securebox

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import java.io.File
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
    fun init(appContext: Context?) {
        this.context = appContext
    }

    private fun checkInit(): Boolean {
        if (this.context == null) {
            Log.e("SecureBoxHelper", "You should first call init method before using the helper!!!")
            return false
        }
        return true
    }

    fun encryptString(variableName: String, plainText: String): Boolean {
        try {
            return encryptString(variableName, plainText, getSecureId())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.e("SecureBoxHelper", "Something went wrong! Either variable name or password incorrect")
        return false
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
            storeVarName(variableName)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun decryptString(variableName: String): String? {
        try {
            return decryptString(variableName, getSecureId())
        } catch (e: Exception) {
            Log.e("SecureBoxHelper", "Something went wrong! Either variable name or password incorrect")
        }
        return null
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
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassCastException) {
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun deleteString(variableName: String): Boolean {
        if (!checkInit()) {
            return false
        }
        val dir = this.context?.filesDir
        val file = File(dir, variableName + ".dat")
        if (file.delete()) {
            Log.e("SecureBoxHelper", "variable deleted successfully")
            return true
        }
        return false
    }

    fun deleteAllStrings(): Boolean {
        if (!checkInit()) {
            return false
        }
        val map = loadVarNames()
        for (entry in map.entries) {
            if (!deleteString(entry.key)) {
                return false
            }
        }
        try {
            val deletedMap = HashMap<String, Boolean>()
            val fos = context!!.openFileOutput("varNames_secureHelper.dat", Context.MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(deletedMap)
            oos.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
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
            e.printStackTrace()
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
            e.printStackTrace()
        }

        return decrypted
    }

    private fun storeVarName(variableName: String) {
        try {
            val map = loadVarNames()
            map[variableName] = true
            val fos = context!!.openFileOutput("varNames_secureHelper.dat", Context.MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(map)
            oos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadVarNames(): HashMap<String, Boolean> {
        try {
            val fileInputStream = context!!.openFileInput("varNames_secureHelper.dat")
            val objectInputStream = ObjectInputStream(fileInputStream)
            return objectInputStream.readObject() as HashMap<String, Boolean>
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return HashMap<String, Boolean>()
    }

    @SuppressLint("HardwareIds")
    private fun getSecureId(): String {
        val secureAndroidId = Settings.Secure.getString(this.context?.getContentResolver(), Settings.Secure.ANDROID_ID)
        var androidId = "UNKNOWN"
        if (secureAndroidId.isNotEmpty()) {
            androidId = secureAndroidId
        }
        return androidId
    }
}