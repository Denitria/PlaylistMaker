package com.trial.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import com.trial.playlistmaker.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var iTunesService: ITunesAPI
    private lateinit var retrofit: Retrofit
    private lateinit var iTunesBaseUrl: String
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var searchHistory: SearchHistory
    private lateinit var onTrackClickListener: OnTrackClickListener
    private lateinit var onHistoryClickListener: OnTrackClickListener
    private lateinit var trackHistoryAdapter: TrackHistoryAdapter
    private val trackList = ArrayList<Track>()
    private val trackHistoryList: ArrayList<Track> = arrayListOf()
    private val searchRunnable = Runnable { search() }
    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper())
    var requestText = TEXT

    @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        searchHistory = SearchHistory(applicationContext)

        binding.buttonBack.setOnClickListener {
            finish()
        }

        iTunesBaseUrl = "https://itunes.apple.com"

        onHistoryClickListener = object : OnTrackClickListener {
            override fun onTrackClick(track: Track) {
                if (clickDebounce()) {
                    val audioIntent = Intent(this@SearchActivity, AudioPlayerActivity::class.java)
                    val gson = Gson()
                    val json = gson.toJson(track)
                    startActivity(audioIntent.putExtra(TRACK_VALUE, json))
                }
            }
        }
        trackHistoryAdapter = TrackHistoryAdapter(onHistoryClickListener)

        retrofit = Retrofit.Builder()
            .baseUrl(iTunesBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        binding.searchHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchHistoryRecyclerView.adapter = trackHistoryAdapter
        trackHistoryAdapter.updateTracks(searchHistory.getHistory())


        onTrackClickListener = object : OnTrackClickListener {
            override fun onTrackClick(track: Track) {
                if (clickDebounce()) {
                    searchHistory.searchHistoryList
                    searchHistory.addTrackToHistory(track)
                    trackHistoryAdapter.updateTracks(searchHistory.searchHistoryList!!)
                    trackHistoryAdapter.notifyDataSetChanged()

                    val audioPlayerIntent =
                        Intent(this@SearchActivity, AudioPlayerActivity::class.java)
                    val gson = Gson()
                    val json = gson.toJson(track)
                    startActivity(audioPlayerIntent.putExtra(TRACK_VALUE, json))
                }
            }
        }


        if (trackHistoryAdapter.itemCount > 0) {
            binding.searchView.visibility = View.GONE
            binding.searchHistoryGroupView.visibility = View.VISIBLE
        } else {
            binding.searchView.visibility = View.VISIBLE
            binding.searchHistoryGroupView.visibility = View.GONE
        }

        trackAdapter = TrackAdapter(trackList, onTrackClickListener)

        binding.buttonClearTrackHistory.setOnClickListener {
            searchHistory.clearHistory()
            trackHistoryAdapter.updateTracks(trackHistoryList)
            binding.searchHistoryGroupView.visibility = View.GONE
            trackHistoryAdapter.notifyDataSetChanged()
        }

        binding.clearButton.visibility = View.INVISIBLE
        binding.clearButton.setOnClickListener {
            trackList.clear()
            trackAdapter.notifyDataSetChanged()
            binding.inputEditText.setText("")
            binding.placeholderView.visibility = View.GONE


            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(binding.clearButton.windowToken, 0)
        }

        binding.inputEditText.setText(requestText)

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearButton.visibility = clearButtonVisibility(s)

                if (binding.inputEditText.hasFocus() && s?.isEmpty() == true) {
                    binding.searchView.visibility = View.GONE
                } else {
                    binding.searchView.visibility = View.VISIBLE
                }

                if (binding.inputEditText.hasFocus() && s?.isEmpty() == true && (trackHistoryAdapter.itemCount > 0)) {
                    binding.searchHistoryGroupView.visibility = View.VISIBLE
                } else {
                    binding.searchHistoryGroupView.visibility = View.GONE
                }

                if (s?.isEmpty() == true) {
                    trackList.clear()
                    binding.progressBar.visibility = View.GONE
                    handler.removeCallbacks(searchRunnable)
                } else searchDebounce()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        binding.inputEditText.addTextChangedListener(simpleTextWatcher)

        val recyclerViewTrack = findViewById<RecyclerView>(R.id.searchRecyclerView)
        recyclerViewTrack.layoutManager = LinearLayoutManager(this)
        iTunesService = retrofit.create(ITunesAPI::class.java)
        binding.inputEditText.addTextChangedListener(simpleTextWatcher)
        recyclerViewTrack.adapter = trackAdapter

        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.inputEditText.setOnClickListener {
                }
                searchDebounce()
            }
            false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showMessage(text: String, additionalMessage: String) {

        if (text.isNotEmpty()) {
            trackList.clear()
            trackAdapter.notifyDataSetChanged()
            binding.errorText.visibility = View.VISIBLE
            binding.errorText.text = text
            if (additionalMessage.isNotEmpty()) {
                Toast.makeText(applicationContext, additionalMessage, Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            binding.errorText.visibility = View.GONE
        }
    }

    private fun search() {

        if (binding.inputEditText.text.isNotEmpty()) {
            binding.placeholderView.visibility = View.GONE
            binding.searchHistoryGroupView.visibility = View.GONE
            binding.searchView.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            iTunesService.search(binding.inputEditText.text.toString()).enqueue(/* callback = */
                object :
                    Callback<TracksResponse> {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onResponse(
                        call: Call<TracksResponse>,
                        response: Response<TracksResponse>,
                    ) {
                        val songs = response.body()?.results
                        if (response.isSuccessful) {
                            binding.progressBar.visibility = View.GONE
                            binding.searchView.visibility = View.VISIBLE

                            trackList.clear()
                            trackAdapter.notifyDataSetChanged()

                            if (songs?.isNotEmpty() == true) {
                                binding.placeholderView.visibility = View.GONE
                                trackList.addAll(songs)
                                trackAdapter.notifyDataSetChanged()
                            }
                            if (trackList.isEmpty()) {
                                binding.placeholderView.visibility = View.VISIBLE
                                showMessage(getString(R.string.nothing_found), "")
                                binding.errorImage.setImageResource(R.drawable.error_search)
                                binding.refreshButton.visibility = View.GONE
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
                        binding.progressBar.visibility = View.GONE
                        binding.placeholderView.visibility = View.VISIBLE
                        binding.errorImage.setImageResource(R.drawable.error_internet)
                        binding.refreshButton.visibility = View.VISIBLE

                        showMessage(
                            getString(R.string.something_went_wrong),
                            t.message.toString()
                        )
                        binding.refreshButton.setOnClickListener {
                            iTunesService.search(binding.inputEditText.text.toString())
                                .enqueue(this)
                        }
                    }

                })
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

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    companion object {
        const val KEY = "SEARCH"
        const val TEXT = ""
        const val TRACK_VALUE = "track"
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}