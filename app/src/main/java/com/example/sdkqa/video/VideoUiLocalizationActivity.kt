package com.example.sdkqa.video

import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayer
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerCallback
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerConfig
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.ui.PlayerView
import com.example.sdkqa.R
import com.google.ads.interactivemedia.v3.api.AdError
import com.google.ads.interactivemedia.v3.api.AdEvent
import org.json.JSONObject

class VideoUiLocalizationActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SDK-QA"
    }

    private lateinit var container: FrameLayout
    private lateinit var spinnerLanguage: Spinner
    private var player: MediastreamPlayer? = null

    private var currentLanguage = "English"
    private val languages = arrayOf("Spanish", "English", "Portuguese")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_ui_localization)
        if (Build.VERSION.SDK_INT >= 35) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            (findViewById<View>(android.R.id.content) as? ViewGroup)?.getChildAt(0)?.let { applyWindowInsetsToRoot(it) }
        }
        initializeViews()
        setupSpinner()
        initializePlayer()
    }

    private fun initializeViews() {
        container = findViewById(R.id.main_media_frame)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, R.layout.spinner_item, languages)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = languages[position]
                if (selected != currentLanguage) {
                    switchToLanguage(selected)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun initializePlayer() {
        val config = createConfigForLanguage("English")
        player = MediastreamPlayer(this, config, container, container, supportFragmentManager)
        player?.addPlayerCallback(createPlayerCallback())
    }

    private fun switchToLanguage(language: String) {
        Log.d(TAG, "Switching UI language to: $language")
        currentLanguage = language
        val newConfig = createConfigForLanguage(language)
        Log.d(TAG, "reloadPlayer with config.language=${newConfig.language} (code=${newConfig.language.code})")
        player?.reloadPlayer(newConfig)
        Log.d(TAG, "reloadPlayer called")
    }

    private fun createConfigForLanguage(language: String): MediastreamPlayerConfig {
        val lang = when (language) {
            "Spanish" -> MediastreamPlayerConfig.Language.SPANISH
            "Portuguese" -> MediastreamPlayerConfig.Language.PORTUGUESE
            else -> MediastreamPlayerConfig.Language.ENGLISH
        }
        return MediastreamPlayerConfig().apply {
            id = "685be889d76b0da57e68620e"
            type = MediastreamPlayerConfig.VideoTypes.VOD
            showControls = true
            appHandlesWindowInsets = true
            this.language = lang
            // Uncomment to use development environment
            // environment = MediastreamPlayerConfig.Environment.DEV
        }
    }

    private fun createPlayerCallback(): MediastreamPlayerCallback {
        return object : MediastreamPlayerCallback {
            override fun playerViewReady(msplayerView: PlayerView?) { Log.d(TAG, "playerViewReady") }
            override fun onPlay() { Log.d(TAG, "onPlay") }
            override fun onPause() { Log.d(TAG, "onPause") }
            override fun onReady() { Log.d(TAG, "onReady") }
            override fun onEnd() { Log.d(TAG, "onEnd") }
            override fun onBuffering() { Log.d(TAG, "onBuffering") }
            override fun onError(error: String?) { Log.e(TAG, "onError: $error") }
            override fun onDismissButton() { Log.d(TAG, "onDismissButton") }
            override fun onPlayerClosed() { Log.d(TAG, "onPlayerClosed") }
            override fun onPlayerReload() { Log.d(TAG, "onPlayerReload") }
            override fun onNext() {}
            override fun onPrevious() {}
            override fun onFullscreen() { Log.d(TAG, "onFullscreen") }
            override fun offFullscreen() { reapplyWindowInsetsToRoot() }
            override fun onNewSourceAdded(config: MediastreamPlayerConfig) {}
            override fun onLocalSourceAdded() {}
            override fun onAdEvents(type: AdEvent.AdEventType) { Log.d(TAG, "onAdEvents: ${type.name}") }
            override fun onAdErrorEvent(error: AdError) { Log.e(TAG, "onAdErrorEvent: ${error.message}") }
            override fun onConfigChange(config: am.mediastre.mediastreamplatformsdkandroid.MediastreamMiniPlayerConfig?) {}
            override fun onCastAvailable(state: Boolean?) {}
            override fun onCastSessionStarting() {}
            override fun onCastSessionStarted() {}
            override fun onCastSessionStartFailed() {}
            override fun onCastSessionEnding() {}
            override fun onCastSessionEnded() {}
            override fun onCastSessionResuming() {}
            override fun onCastSessionResumed() {}
            override fun onCastSessionResumeFailed() {}
            override fun onCastSessionSuspended() {}
            override fun onPlaybackErrors(error: JSONObject?) { Log.e(TAG, "onPlaybackErrors: $error") }
            override fun onEmbedErrors(error: JSONObject?) { Log.e(TAG, "onEmbedErrors: $error") }
            override fun onLiveAudioCurrentSongChanged(data: JSONObject?) {}
            override fun nextEpisodeIncoming(nextEpisodeId: String) { Log.d(TAG, "nextEpisodeIncoming: $nextEpisodeId") }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (player?.isOnFullscreen == true) player?.exitFullscreen()
        super.onConfigurationChanged(newConfig)
        reapplyWindowInsetsToRoot()
    }

    private fun reapplyWindowInsetsToRoot() {
        if (Build.VERSION.SDK_INT >= 35) {
            (findViewById<View>(android.R.id.content) as? ViewGroup)?.getChildAt(0)?.let { applyWindowInsetsToRoot(it) }
        }
    }

    private fun applyWindowInsetsToRoot(root: View) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(root)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.releasePlayer()
    }
}
