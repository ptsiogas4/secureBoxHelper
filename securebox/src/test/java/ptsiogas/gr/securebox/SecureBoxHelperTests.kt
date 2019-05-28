package ptsiogas.gr.securebox

import android.content.Context
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SecureBoxHelperTests {

    lateinit var instrumentationContext: Context

    @Before
    fun setup() {
        instrumentationContext = instrumentationContext.applicationContext
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
}