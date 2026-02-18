package com.example.sdkqa.video

import am.mediastre.mediastreamplatformsdkandroid.MediastreamMiniPlayerConfig
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayer
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerCallback
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerConfig
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
import com.example.sdkqa.audio.AudioMixedActivity
import com.example.sdkqa.audio.AudioMixedActivity.Companion
import com.google.ads.interactivemedia.v3.api.AdError
import com.google.ads.interactivemedia.v3.api.AdEvent
import org.json.JSONObject

class VideoLiveActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SDK-QA"
        private const val TAG_NET = "SDK-QA-URL"
    }

    private var player: MediastreamPlayer? = null

    /** Guardamos config actual para incluir en logs de error (ruta/contenido que se intenta consumir) */
    private var currentConfigId: String? = null
    private var currentConfigDrmLicenseUrl: String? = null
    private var currentConfigVideoFormat: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainMediaFrame = FrameLayout(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.BLACK)
            keepScreenOn = true
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val playerContainer = FrameLayout(this).apply {
            id = View.generateViewId()
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        mainMediaFrame.addView(playerContainer)
        setContentView(mainMediaFrame)
        if (Build.VERSION.SDK_INT >= 35) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            applyWindowInsetsToRoot(mainMediaFrame)
        }
        setupPlayer(mainMediaFrame)
    }

    private fun setupPlayer(mainMediaFrame: FrameLayout) {
        val drmLicenseUrl = "https://af718b38.drm-widevine-licensing.axprod.net/AcquireLicense"
        val drmHeaders = hashMapOf(
            "X-AxDRM-Message" to "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2ZXJzaW9uIjoxLCJjb21fa2V5X2lkIjoiMjRjZmYwZmMtNGZlYy00NGYyLWFiZTktYjM2OTAxNjViNmFkIiwibWVzc2FnZSI6eyJ0eXBlIjoiZW50aXRsZW1lbnRfbWVzc2FnZSIsInZlcnNpb24iOjEsImV4cGlyYXRpb25fZGF0ZSI6IjIwMjYtMDItMTNUMTk6NTU6NDEuODU5WiIsImtleXMiOlt7ImlkIjoiRDNEMkRENjctQTYxNC00QTk5LThENzItNTMzODU0RTE5MjE2In1dfX0.bNQTH8mj4p9ZUZe-mfbBUJtTxWLcbyEL9JBBl2nFbQY"
        )
        val contentId = "69690d0f662b3e30d33f69c8"
        val config = MediastreamPlayerConfig().apply {
            id = contentId
            type = MediastreamPlayerConfig.VideoTypes.LIVE
            appHandlesWindowInsets = true
            videoFormat = MediastreamPlayerConfig.AudioVideoFormat.DASH
            isDebug = true
            drmData = MediastreamPlayerConfig.DrmData(drmLicenseUrl, drmHeaders)
            //Uncomment to use development environment
            //environment = MediastreamPlayerConfig.Environment.DEV
        }

        currentConfigId = contentId
        currentConfigDrmLicenseUrl = drmLicenseUrl
        currentConfigVideoFormat = "DASH"

        Log.d(TAG, "🔗 [CONFIG] contentId=$contentId | type=LIVE | videoFormat=DASH | drmLicenseUrl=$drmLicenseUrl")
        Log.d(TAG_NET, "🔗 Reproducción LIVE DASH. El manifiesto y segmentos .ts los resuelve el SDK a partir del contentId (embed/stream). Para ver URLs exactas hace falta logging dentro del SDK o un proxy (Charles/Fiddler).")

        player = MediastreamPlayer(
            this,
            config,
            mainMediaFrame,
            mainMediaFrame,
            supportFragmentManager
        )

        player?.addPlayerCallback(createPlayerCallback())
    }

    private fun debugContext(): String {
        return "contentId=${currentConfigId ?: "?"} | format=${currentConfigVideoFormat ?: "?"} | drmLicenseUrl=${currentConfigDrmLicenseUrl ?: "?"}"
    }

    private fun createPlayerCallback(): MediastreamPlayerCallback {
        return object : MediastreamPlayerCallback {
            override fun playerViewReady(msplayerView: PlayerView?) {
                Log.d(TAG, "playerViewReady | ${this@VideoLiveActivity.debugContext()}")
            }

            override fun onPlay() {
                Log.d(TAG, "onPlay | ${this@VideoLiveActivity.debugContext()}")
            }

            override fun onPause() {
                Log.d(TAG, "onPause | ${this@VideoLiveActivity.debugContext()}")
            }

            override fun onReady() {
                Log.d(TAG, "onReady | reproducción lista | ${this@VideoLiveActivity.debugContext()}")
            }

            override fun onEnd() {
                Log.d(TAG, "onEnd | ${this@VideoLiveActivity.debugContext()}")
            }

            override fun onBuffering() {
                Log.d(TAG, "onBuffering | ${this@VideoLiveActivity.debugContext()}")
            }

            override fun onError(error: String?) {
                Log.e(TAG, "onError: $error | ${this@VideoLiveActivity.debugContext()}")
                Log.e(TAG_NET, "Si el error es de manifiesto DASH (.mpd), el SDK está intentando cargar el MPD del contentId. Revisa que el MPD no tenga entidades XML sin cerrar (ej. 'urn:mpeg:cenc:2013')")
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
            }

            override fun onNewSourceAdded(config: MediastreamPlayerConfig) {
                currentConfigId = config.id
                currentConfigVideoFormat = config.videoFormat?.name
                Log.d(TAG_NET, "onNewSourceAdded | id=${config.id} | type=${config.type?.name} | videoFormat=${config.videoFormat?.name} | ${this@VideoLiveActivity.debugContext()}")
            }
            override fun onLocalSourceAdded() {
                Log.d(TAG, "onLocalSourceAdded")
            }

            override fun onAdEvents(type: AdEvent.AdEventType) {
                Log.d(TAG, "onAdEvents: ${type.name} | ${this@VideoLiveActivity.debugContext()}")
                if (type == AdEvent.AdEventType.AD_BREAK_FETCH_ERROR) {
                    Log.e(TAG_NET, "AD_BREAK_FETCH_ERROR: falló la petición de VAST/ads; el contenido principal puede seguir por otra ruta (manifiesto/segmentos)")
                }
            }

            override fun onAdErrorEvent(error: AdError) {
                Log.e(TAG, "onAdErrorEvent: message=${error.message} | errorCode=${error.errorCode} | ${this@VideoLiveActivity.debugContext()}")
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
                val jsonStr = error?.toString() ?: "null"
                Log.e(TAG, "onPlaybackErrors: $jsonStr | ${this@VideoLiveActivity.debugContext()}")
                error?.keys()?.asSequence()?.forEach { key ->
                    Log.e(TAG, "  onPlaybackErrors[$key]=${error.opt(key)}")
                }
                Log.e(TAG_NET, "Playback error (manifiesto/segmentos). ERROR_CODE_PARSING_MANIFEST_MALFORMED suele ser MPD con XML mal formado (entidad sin cerrar). ContentId usado: $currentConfigId")
            }

            override fun onEmbedErrors(error: JSONObject?) {
                val jsonStr = error?.toString() ?: "null"
                Log.e(TAG, "onEmbedErrors: $jsonStr | ${this@VideoLiveActivity.debugContext()}")
                error?.keys()?.asSequence()?.forEach { key ->
                    Log.e(TAG, "  onEmbedErrors[$key]=${error.opt(key)}")
                }
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