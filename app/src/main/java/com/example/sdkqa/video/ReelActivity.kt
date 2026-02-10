package com.example.sdkqa.video

import am.mediastre.mediastreamplatformsdkandroid.MediastreamMiniPlayerConfig
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayer
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerCallback
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerConfig
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerConfig.FlagStatus
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.ui.PlayerView
import com.google.ads.interactivemedia.v3.api.AdError
import com.google.ads.interactivemedia.v3.api.AdEvent
import org.json.JSONObject

/**
 * Reels: mismo enfoque que MediastreamSampleApp ReelActivity.
 * Config: id, playerId, type VOD, DEV, trackEnable false, pauseOnScreenClick DISABLE, showDismissButton, autoplay.
 */
class ReelActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SDK-QA"
    }

    private var player: MediastreamPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainMediaFrame = FrameLayout(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.BLACK)
            keepScreenOn = true
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        setContentView(mainMediaFrame)
        if (Build.VERSION.SDK_INT >= 35) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            applyWindowInsetsToRoot(mainMediaFrame)
        }
        setupPlayer(mainMediaFrame)
    }

    private fun setupPlayer(mainMediaFrame: FrameLayout) {
        val config = MediastreamPlayerConfig().apply {
            id = "6927c03148be9c0e38442dc8"
            playerId = "68c46249cf666688cb586a06"
            type = MediastreamPlayerConfig.VideoTypes.VOD
            isDebug = true
            //environment = MediastreamPlayerConfig.Environment.DEV
            trackEnable = false
            pauseOnScreenClick = FlagStatus.DISABLE
            showDismissButton = true
            autoplay = true
            appHandlesWindowInsets = true
        }

        player = MediastreamPlayer(
            this,
            config,
            mainMediaFrame,
            mainMediaFrame,
            supportFragmentManager
        )

        player?.addPlayerCallback(createPlayerCallback())
    }

    private fun createPlayerCallback(): MediastreamPlayerCallback {
        return object : MediastreamPlayerCallback {
            override fun playerViewReady(msplayerView: PlayerView?) {
                Log.d(TAG, "playerViewReady")
            }

            override fun onPlay() {
                Log.d(TAG, "onPlay")
            }

            override fun onPause() {
                Log.d(TAG, "onPause")
            }

            override fun onReady() {
                Log.d(TAG, "onReady")
            }

            override fun onEnd() {
                Log.d(TAG, "onEnd")
            }

            override fun onBuffering() {
                Log.d(TAG, "onBuffering")
            }

            override fun onError(error: String?) {
                Log.e(TAG, "onError: $error")
            }

            override fun onDismissButton() {
                Log.d(TAG, "onDismissButton")
            }

            override fun onPlayerClosed() {
                Log.d(TAG, "onPlayerClosed")
                finish()
            }

            override fun onPlayerReload() {
                Log.d(TAG, "onPlayerReload")
            }

            override fun onNext() {}
            override fun onPrevious() {}
            override fun onFullscreen() {
                Log.d(TAG, "onFullscreen")
            }

            override fun offFullscreen() {
                Log.d(TAG, "offFullscreen")
                reapplyWindowInsetsToRoot()
            }

            override fun onNewSourceAdded(config: MediastreamPlayerConfig) {}
            override fun onLocalSourceAdded() {}

            override fun onAdEvents(type: AdEvent.AdEventType) {
                Log.d(TAG, "onAdEvents: ${type.name}")
            }

            override fun onAdErrorEvent(error: AdError) {
                Log.e(TAG, "onAdErrorEvent: ${error.message}")
            }

            override fun onConfigChange(config: MediastreamMiniPlayerConfig?) {}
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
            override fun onPlaybackErrors(error: JSONObject?) {
                Log.e(TAG, "onPlaybackErrors: $error")
            }

            override fun onEmbedErrors(error: JSONObject?) {
                Log.e(TAG, "onEmbedErrors: $error")
            }

            override fun onLiveAudioCurrentSongChanged(data: JSONObject?) {}

            override fun nextEpisodeIncoming(nextEpisodeId: String) {
                Log.d(TAG, "nextEpisodeIncoming: $nextEpisodeId")
            }
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
