package ptsiogas.gr.securestorage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import ptsiogas.gr.securebox.SecureBoxHelper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        encryptButton.setOnClickListener {
            val text = editText?.text?.toString() ?: return@setOnClickListener
            SecureBoxHelper.instance.encryptString("testVar", text)
            resultTextView.text = "encrypted succesfully"
        }

        testButton.setOnClickListener {
            resultTextView.text = SecureBoxHelper.instance.decryptString("testVar")
        }

        wrongPassTestButton.setOnClickListener {
            resultTextView.text = SecureBoxHelper.instance.decryptString("testVar", "wrongPass")
        }

        deleteTestButton.setOnClickListener {
            if (SecureBoxHelper.instance.deleteString("testVar")) {
                resultTextView.text = "deleted succesfully"
            } else {
                resultTextView.text = "error!"
            }
        }
    }


}
