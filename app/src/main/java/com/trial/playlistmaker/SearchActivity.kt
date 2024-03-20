package com.trial.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {
    private lateinit var inputEditText: EditText
    private lateinit var iTunesService: ITunesAPI
    private lateinit var retrofit: Retrofit
    private lateinit var iTunesBaseUrl: String
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var placeholder: View
    private lateinit var refreshButton: Button
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderMessage: TextView
    private lateinit var searchHistoryView: View
    private lateinit var searchView: View
    private lateinit var searchHistoryRecyclerView: RecyclerView
    private lateinit var clearHistory: Button
    private lateinit var searchHistory: SearchHistory
    private lateinit var onTrackClickListener: OnTrackClickListener
    private lateinit var trackHistoryAdapter: TrackHistoryAdapter
    private val trackList = ArrayList<Track>()
    private val trackHistoryList: ArrayList<Track> = arrayListOf()
    var requestText = TEXT

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        inputEditText = findViewById(R.id.inputEditText)
        placeholder = findViewById(R.id.placeholderView)
        placeholderMessage = findViewById(R.id.errorText)
        placeholderImage = findViewById(R.id.errorImage)
        refreshButton = findViewById(R.id.refreshButton)
        searchHistoryView = findViewById(R.id.searchHistoryGroupView)
        searchView = findViewById(R.id.searchView)
        searchHistoryRecyclerView = findViewById(R.id.searchHistoryRecyclerView)
        searchHistory = SearchHistory(applicationContext)
        clearHistory = findViewById(R.id.buttonClearTrackHistory)
        trackHistoryAdapter = TrackHistoryAdapter(searchHistory.getHistory())

        val backToMain = findViewById<Button>(R.id.buttonBack)
        backToMain.setOnClickListener {
            finish()
        }

        iTunesBaseUrl = "https://itunes.apple.com"

        retrofit = Retrofit.Builder()
            .baseUrl(iTunesBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        searchHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        searchHistoryRecyclerView.adapter = trackHistoryAdapter
        trackHistoryAdapter.updateTracks(searchHistory.getHistory())

        onTrackClickListener = object : OnTrackClickListener {
            override fun onTrackClick(track: Track) {
                searchHistory.searchHistoryList
                searchHistory.addTrackToHistory(track)
                trackHistoryAdapter.updateTracks(searchHistory.searchHistoryList!!)
                trackHistoryAdapter.run {
                    notifyDataSetChanged()
                }
            }
        }

        if (trackHistoryAdapter.itemCount > 0) {
            searchHistoryView.visibility = View.VISIBLE
        } else {
            searchHistoryView.visibility = View.GONE
        }

        trackAdapter = TrackAdapter(trackList, onTrackClickListener)

        clearHistory.setOnClickListener() {
            searchHistory.clearHistory()
            trackHistoryAdapter.updateTracks(trackHistoryList)
            searchHistoryView.visibility = View.GONE
            trackHistoryAdapter.run { notifyDataSetChanged() }
        }

        val clearButton = findViewById<ImageView>(R.id.clearIcon)
        clearButton.visibility = View.INVISIBLE
        clearButton.setOnClickListener {
            trackList.clear()
            trackAdapter.notifyDataSetChanged()
            inputEditText.setText("")
            placeholder.visibility = View.GONE


            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(clearButton.windowToken, 0)
        }

        inputEditText.setText(requestText)

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = clearButtonVisibility(s)

                if (inputEditText.hasFocus() && s?.isEmpty() == true) {
                    searchView.visibility = View.GONE
                } else {
                    searchView.visibility = View.VISIBLE
                }

                if (inputEditText.hasFocus() && s?.isEmpty() == true && (trackHistoryAdapter.itemCount > 0)) {
                    searchHistoryView.visibility = View.VISIBLE
                } else {
                    searchHistoryView.visibility = View.GONE
                }

                if (s?.isEmpty() == true) trackList.clear()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)

        val recyclerViewTrack = findViewById<RecyclerView>(R.id.searchRecyclerView)
        recyclerViewTrack.layoutManager = LinearLayoutManager(this)
        iTunesService = retrofit.create(ITunesAPI::class.java)
        inputEditText.addTextChangedListener(simpleTextWatcher)
        recyclerViewTrack.adapter = trackAdapter


        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (inputEditText.text.isNotEmpty()) {
                    iTunesService.search(inputEditText.text.toString()).enqueue(/* callback = */
                        object :
                            Callback<TracksResponse> {
                            override fun onResponse(
                                call: Call<TracksResponse>,
                                response: Response<TracksResponse>,
                            ) {
                                val songs = response.body()?.results
                                if (response.isSuccessful) {
                                    trackList.clear()
                                    trackAdapter.notifyDataSetChanged()

                                    if (songs?.isNotEmpty() == true) {
                                        placeholder.visibility = View.GONE
                                        trackList.addAll(songs)
                                        trackAdapter.notifyDataSetChanged()
                                    }
                                    if (trackList.isEmpty()) {
                                        placeholder.visibility = View.VISIBLE
                                        showMessage(getString(R.string.nothing_found), "")
                                        placeholderImage.setImageResource(R.drawable.error_search)
                                        refreshButton.visibility = View.GONE
                                    } else {
                                        showMessage("", "")
                                    }
                                } else {
                                    showMessage(
                                        getString(R.string.something_went_wrong),
                                        response.code().toString()
                                    )
                                }
                            }

                            override fun onFailure(call: Call<TracksResponse>, t: Throwable) {

                                placeholder.visibility = View.VISIBLE
                                placeholderImage.setImageResource(R.drawable.error_internet)
                                refreshButton.visibility = View.VISIBLE

                                showMessage(
                                    getString(R.string.something_went_wrong),
                                    t.message.toString()
                                )
                                refreshButton.setOnClickListener {
                                    iTunesService.search(inputEditText.text.toString())
                                        .enqueue(this)
                                }
                            }

                        })
                }
            }
            false
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showMessage(text: String, additionalMessage: String) {

        if (text.isNotEmpty()) {
            trackList.clear()
            trackAdapter.notifyDataSetChanged()
            placeholderMessage.visibility = View.VISIBLE
            placeholderMessage.text = text
            if (additionalMessage.isNotEmpty()) {
                Toast.makeText(applicationContext, additionalMessage, Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            placeholderMessage.visibility = View.GONE
        }
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

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
        const val TRACK_VALUE = "track"
    }
}