package ptsiogas.gr.securestorage

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import ptsiogas.gr.securebox.SecureBoxHelper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SecureBoxHelper.instance.init(this)
        val sampleText = "This is a properly decrypted text."
        testTextView.text = sampleText
        SecureBoxHelper.instance.encryptString("testVar", sampleText, "mySecurePassword")

        testButton.setOnClickListener {
            resultTextView.text = SecureBoxHelper.instance.decryptString("testVar", "mySecurePassword")
        }

        wrongPassTestButton.setOnClickListener {
            resultTextView.text = SecureBoxHelper.instance.decryptString("testVar", "wrongPass")
        }
    }


}
