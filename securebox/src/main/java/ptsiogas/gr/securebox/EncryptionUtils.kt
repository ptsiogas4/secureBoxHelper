package ptsiogas.gr.securebox

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class EncryptionUtils {
    companion object {
        private fun getCipher(): Cipher {
            return Cipher.getInstance("AES/CBC/PKCS7Padding")
        }

        private fun getCipherArray(
            inputArray: ByteArray?, optMode: Int, keySpec: SecretKeySpec,
            ivSpec: IvParameterSpec
        ): ByteArray {
            val cipher = getCipher()
            cipher.init(optMode, keySpec, ivSpec)
            return cipher.doFinal(inputArray)
        }

        private fun getSecretKeyFactory(): SecretKeyFactory {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        }

        fun getRandomByteArray(arraySize: Int): ByteArray {
            val random = SecureRandom()
            val salt = ByteArray(arraySize)
            random.nextBytes(salt)
            return salt
        }

        fun encryptByteArray(
            inputArray: ByteArray,
            keySpec: SecretKeySpec,
            ivSpec: IvParameterSpec
        ): ByteArray {
            return getCipherArray(
                inputArray = inputArray,
                optMode = Cipher.ENCRYPT_MODE,
                keySpec = keySpec,
                ivSpec = ivSpec
            )
        }

        fun decryptByteArray(
            inputArray: ByteArray?,
            keySpec: SecretKeySpec,
            ivSpec: IvParameterSpec
        ): ByteArray {
            return getCipherArray(
                inputArray = inputArray,
                optMode = Cipher.DECRYPT_MODE,
                keySpec = keySpec,
                ivSpec = ivSpec
            )

        }

        fun getKeySpec(passwordString: String, salt: ByteArray?): SecretKeySpec {
            val passwordChar = passwordString.toCharArray() //Turn password into char[] array
            val pbKeySpec = PBEKeySpec(passwordChar, salt, 1324, 256) //1324 iterations
            val keyBytes = getSecretKeyFactory().generateSecret(pbKeySpec).encoded
            return SecretKeySpec(keyBytes, "AES")
        }

        @SuppressLint("HardwareIds")
        fun getSecureId(context: Context?): String {
            val secureAndroidId = Settings.Secure.getString(
                context?.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            var androidId = "UNKNOWN"
            if (secureAndroidId.isNotEmpty()) {
                androidId = secureAndroidId
            }
            return androidId
        }
    }
}