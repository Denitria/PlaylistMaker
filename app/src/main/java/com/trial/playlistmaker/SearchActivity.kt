package com.trial.playlistmaker

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView

class SearchActivity : AppCompatActivity() {
    private lateinit var inputEditText: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val backToMain = findViewById<Button>(R.id.buttonBack)
        backToMain.setOnClickListener {
            finish()
        }
        inputEditText = findViewById<EditText>(R.id.inputEditText)
        val clearButton = findViewById<ImageView>(R.id.clearIcon)
        clearButton.visibility = View.INVISIBLE
    clearButton.setOnClickListener{
        inputEditText.text.clear()
        hideKeyboard()
    }
        inputEditText.setText(requestText)
    val simpleTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            clearButton.visibility = clearButtonVisibility(s)
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }
        inputEditText.addTextChangedListener(simpleTextWatcher)

    }
    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    private var requestText: String = TEXT
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY, requestText)
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        requestText = savedInstanceState.getString(KEY, TEXT)
    }
    companion object {
        const val KEY = "SEARCH"
        const val TEXT = ""
    }

}




