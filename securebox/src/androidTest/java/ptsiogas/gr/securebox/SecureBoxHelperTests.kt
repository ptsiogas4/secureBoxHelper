package ptsiogas.gr.securebox

import android.content.Context
import android.support.test.InstrumentationRegistry
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SecureBoxHelperTests {

    lateinit var instrumentationContext: Context

    @Before
    fun setup() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        SecureBoxHelper.instance.init(instrumentationContext)
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