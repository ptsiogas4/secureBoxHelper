package ptsiogas.gr.securebox

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import javax.crypto.spec.IvParameterSpec

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
            Log.e(ErrorMessage.errorTitle.message, ErrorMessage.initError.message)
            return false
        }
        return true
    }

    fun encryptString(variableName: String, plainText: String): Boolean {
        try {
            return encryptString(variableName, plainText, EncryptionUtils.getSecureId(context))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        ErrorMessage.logGeneralError()
        return false
    }

    fun encryptString(variableName: String, plainText: String, passwordString: String): Boolean {
        if (!checkInit()) {
            return false
        }
        try {
            val map = encryptString(plainText.toByteArray(), passwordString)
            val fos = context!!.openFileOutput("$variableName.dat", Context.MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(map)
            oos.close()
            StoreUtils.storeVarName(variableName, context)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun decryptString(variableName: String): String? {
        try {
            return decryptString(variableName, EncryptionUtils.getSecureId(context))
        } catch (e: Exception) {
            ErrorMessage.logGeneralError()
        }
        return null
    }

    fun decryptString(variableName: String, passwordString: String): String? {
        if (!checkInit()) {
            return null
        }
        try {
            val fileInputStream = context?.openFileInput(variableName + ".dat")
            fileInputStream?.use { fileInputStream ->
                val objectInputStream = ObjectInputStream(fileInputStream)
                val map = objectInputStream.readObject() as HashMap<String, ByteArray>
                val decryptedByteArray = decryptString(map, passwordString)
                if (decryptedByteArray != null) {
                    return String(decryptedByteArray)
                } else {
                    ErrorMessage.logGeneralError()
                }
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
        val file = File(dir, "$variableName.dat")
        if (file.delete()) {
            Log.e(ErrorMessage.errorTitle.message, ErrorMessage.deletionError.message)
            return true
        }
        return false
    }

    fun deleteAllStrings(): Boolean {
        if (!checkInit()) {
            return false
        }
        val map = StoreUtils.loadVarNames(context)
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

    private fun encryptString(
        plainTextBytes: ByteArray,
        passwordString: String
    ): HashMap<String, ByteArray> {
        val map = HashMap<String, ByteArray>()

        try {
            //Random salt for next step
            val salt = EncryptionUtils.getRandomByteArray(arraySize = 256)

            //PBKDF2 - derive the key from the password, don't use passwords directly
            val keySpec = EncryptionUtils.getKeySpec(passwordString, salt)

            //Create initialization vector for AES
            val iv = EncryptionUtils.getRandomByteArray(arraySize = 16)
            val ivSpec = IvParameterSpec(iv)

            //Encrypt
            val encrypted = EncryptionUtils.encryptByteArray(plainTextBytes, keySpec, ivSpec)

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
            val keySpec = EncryptionUtils.getKeySpec(passwordString, salt)

            //Decrypt
            val ivSpec = IvParameterSpec(iv)
            decrypted = EncryptionUtils.decryptByteArray(encrypted, keySpec, ivSpec)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return decrypted
    }
}