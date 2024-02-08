package com.trial.playlistmaker

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backToMain = findViewById<Button>(R.id.buttonBack)
        backToMain.setOnClickListener {
            finish()
        }

        val buttonShare = findViewById<TextView>(R.id.buttonShare)
        buttonShare.setOnClickListener {
            val intent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                val buttonShareMessage = resources.getString(R.string.button_share_message)
                putExtra(Intent.EXTRA_TEXT, buttonShareMessage)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(intent, null)
            startActivity(shareIntent)
        }

        val mailAddress = getString(R.string.mail_address)
        val subjectMail = getString(R.string.subject_mail)
        val textMail = getString(R.string.text_mail)
        val buttonSupport = findViewById<TextView>(R.id.buttonSupport)

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(mailAddress))
                putExtra(Intent.EXTRA_SUBJECT, subjectMail)
                putExtra(Intent.EXTRA_TEXT, textMail)
            }
            buttonSupport.setOnClickListener {
                startActivity(Intent.createChooser(intent, null))
            }

        val buttonTerms= findViewById<TextView>(R.id.termsOfUse)
        buttonTerms.setOnClickListener {
            val linkTerms = getString(R.string.terms_of_use)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(linkTerms)
            startActivity(intent)
        }

    }
}