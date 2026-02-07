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
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.ui.PlayerView
import com.example.sdkqa.R
import com.google.ads.interactivemedia.v3.api.AdError
import com.google.ads.interactivemedia.v3.api.AdEvent
import org.json.JSONObject

class VideoSmallContainerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SDK-QA"
    }

    private lateinit var container: FrameLayout
    private var player: MediastreamPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_small_container)
        if (Build.VERSION.SDK_INT >= 35) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            (findViewById<View>(android.R.id.content) as? ViewGroup)?.getChildAt(0)?.let { applyWindowInsetsToRoot(it) }
        }
        container = findViewById(R.id.main_media_frame)
        setupPlayer()
    }

    private fun setupPlayer() {
        val config = MediastreamPlayerConfig().apply {
            id = "685be889d76b0da57e68620e"
            type = MediastreamPlayerConfig.VideoTypes.VOD
            showControls = true
            appHandlesWindowInsets = true
            // Uncomment to use development environment
            // environment = MediastreamPlayerConfig.Environment.DEV
        }

        player = MediastreamPlayer(
            this,
            config,
            container,
            container,
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
                // El SDK puede reañadir el contenedor al final del parent; en nuestro layout debe ir arriba (índice 0).
                ensurePlayerContainerOnTop()
                container.requestLayout()
                (findViewById<View>(android.R.id.content) as? ViewGroup)?.getChildAt(0)?.requestLayout()
            }

            override fun onNewSourceAdded(config: MediastreamPlayerConfig) {}
            override fun onLocalSourceAdded() {}

            override fun onAdEvents(type: AdEvent.AdEventType) {
                Log.d(TAG, "onAdEvents: ${type.name}")
            }

            override fun onAdErrorEvent(error: AdError) {
                Log.e(TAG, "onAdErrorEvent: ${error.message}")
            }

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

    /**
     * Si el contenedor del player quedó debajo del contenido (p. ej. tras salir de fullscreen),
     * lo reordena para que sea el primer hijo del root y vuelva a mostrarse arriba.
     */
    private fun ensurePlayerContainerOnTop() {
        val root = (findViewById<View>(android.R.id.content) as? ViewGroup)?.getChildAt(0) as? ViewGroup ?: return
        if (container.parent != root) return
        val index = root.indexOfChild(container)
        if (index > 0) {
            val params = container.layoutParams
            root.removeView(container)
            root.addView(container, 0, params)
        }
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
