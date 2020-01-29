package ptsiogas.gr.securebox

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.Assert.assertEquals
import org.junit.*


class SecureBoxHelperTests {

    var instrumentationContext: Context? = null

    @Before
    fun setup() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        instrumentationContext?.let {
            SecureBoxHelper.instance.init(it)
        }
    }

    @After
    fun tearDown() {
        instrumentationContext = null
    }

    @Test
    fun encrypt_isCorrect() {
        assert(SecureBoxHelper.instance.encryptString("testVar", "This is a test", "testPassword"))
    }

    @Test
    fun decrypt_isCorrect() {
        val varName = "testVar"
        val message = "This is a test"
        val password = "testPassword"
        SecureBoxHelper.instance.encryptString(varName, message, password)
        assertEquals(message, SecureBoxHelper.instance.decryptString(varName, password))
    }

    @Test
    fun delete_string_isCorrect() {
        val varName = "testVar"
        val message = "This is a test"
        val password = "testPassword"
        SecureBoxHelper.instance.encryptString(varName, message, password)
        assert(SecureBoxHelper.instance.deleteString(varName))
    }

    @Test
    fun delete_all_strings_isCorrect() {
        val varName = "testVar"
        val varName2 = "testVar2"
        val message = "This is a test"
        val password = "testPassword"
        SecureBoxHelper.instance.encryptString(varName, message, password)
        SecureBoxHelper.instance.encryptString(varName2, message, password)
        assert(SecureBoxHelper.instance.deleteAllStrings())
    }
}