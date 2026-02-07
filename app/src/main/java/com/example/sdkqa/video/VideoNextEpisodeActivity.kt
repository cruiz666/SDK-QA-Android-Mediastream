package com.example.sdkqa.video

import am.mediastre.mediastreamplatformsdkandroid.MediastreamMiniPlayerConfig
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayer
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerCallback
import am.mediastre.mediastreamplatformsdkandroid.MediastreamPlayerConfig
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.media3.ui.PlayerView
import com.google.ads.interactivemedia.v3.api.AdError
import com.google.ads.interactivemedia.v3.api.AdEvent
import org.json.JSONObject

class VideoNextEpisodeActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SDK-QA"
    }

    private var player: MediastreamPlayer? = null
    private lateinit var mainMediaFrame: FrameLayout

    // Botones de navegación
    private lateinit var btnNextEpisode: Button
    private lateinit var btnNextEpisodeCustom: Button

    // Arreglo de IDs para el siguiente episodio custom
    private val nextEpisodeIds = listOf("6892591911582875cc48b239", "689f6396ef81e4c28ba9644b")
    private var currentEpisodeIndex = 0

    private var currentMode = "NEXT_EPISODE"

    // Single callback instance to avoid duplicate registrations
    private val playerCallback by lazy { createPlayerCallback() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crear layout programáticamente
        val rootLayout = createLayout()
        setContentView(rootLayout)
        if (Build.VERSION.SDK_INT >= 35) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            applyWindowInsetsToRoot(rootLayout)
        }
        setupButtons()
        initializePlayer()
    }

    private fun createLayout(): ConstraintLayout {
        val rootLayout = ConstraintLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Contenedor del player (no focusable para que el focus quede en los botones del footer en TV)
        mainMediaFrame = FrameLayout(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.BLACK)
            keepScreenOn = true
            isFocusable = false
        }

        // Crear el footer con botones
        val footerLayout = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#1a1a1a"))
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
        }

        // Crear botón Next Episode (focusable para TV / D-pad)
        btnNextEpisode = Button(this).apply {
            id = View.generateViewId()
            text = "Next Episode"
            textSize = 12f
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.parseColor("#97D700"))
            isFocusable = true
            isFocusableInTouchMode = true
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginEnd = dpToPx(8)
            }
        }

        // Crear botón Next Episode Custom (focusable para TV / D-pad)
        btnNextEpisodeCustom = Button(this).apply {
            id = View.generateViewId()
            text = "Next Episode Custom"
            textSize = 12f
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.parseColor("#97D700"))
            isFocusable = true
            isFocusableInTouchMode = true
            nextFocusLeftId = btnNextEpisode.id
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = dpToPx(8)
            }
        }

        btnNextEpisode.nextFocusRightId = btnNextEpisodeCustom.id

        footerLayout.addView(btnNextEpisode)
        footerLayout.addView(btnNextEpisodeCustom)

        rootLayout.addView(mainMediaFrame)
        rootLayout.addView(footerLayout)

        // Aplicar constraints
        val constraintSet = ConstraintSet()
        constraintSet.clone(rootLayout)

        // Footer constraints
        constraintSet.connect(footerLayout.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(footerLayout.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(footerLayout.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraintSet.constrainWidth(footerLayout.id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(footerLayout.id, ConstraintSet.WRAP_CONTENT)

        // Player container constraints
        constraintSet.connect(mainMediaFrame.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(mainMediaFrame.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(mainMediaFrame.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraintSet.connect(mainMediaFrame.id, ConstraintSet.BOTTOM, footerLayout.id, ConstraintSet.TOP)
        constraintSet.constrainWidth(mainMediaFrame.id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(mainMediaFrame.id, ConstraintSet.MATCH_CONSTRAINT)

        constraintSet.applyTo(rootLayout)

        return rootLayout
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun setupButtons() {
        btnNextEpisode.setOnClickListener { switchToMode("NEXT_EPISODE") }
        btnNextEpisodeCustom.setOnClickListener { switchToMode("NEXT_EPISODE_CUSTOM") }

        // Indicador de focus para TV (D-pad): borde visible cuando el control tiene focus
        setupFocusIndicator(btnNextEpisode)
        setupFocusIndicator(btnNextEpisodeCustom)

        // Marcar Next Episode como seleccionado por defecto
        updateButtonStates("NEXT_EPISODE")

        // En TV, dar focus al primer botón para que se pueda navegar con D-pad de entrada
        btnNextEpisode.post { btnNextEpisode.requestFocus() }
    }

    /**
     * Añade un borde de focus visible para navegación con mando (TV / D-pad).
     */
    private fun setupFocusIndicator(button: Button) {
        val focusBorderColor = Color.parseColor("#00D9FF") // accent
        val borderWidthPx = dpToPx(3)
        button.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val fillColor = (button.background as? ColorDrawable)?.color ?: Color.parseColor("#97D700")
                val drawable = GradientDrawable().apply {
                    setColor(fillColor)
                    setStroke(borderWidthPx, focusBorderColor)
                    cornerRadius = dpToPx(4).toFloat()
                }
                button.background = drawable
            } else {
                updateButtonStates(currentMode)
            }
        }
    }

    private fun initializePlayer() {
        val config = createConfigForMode("NEXT_EPISODE")
        player = MediastreamPlayer(this, config, mainMediaFrame, mainMediaFrame, supportFragmentManager)
        player?.addPlayerCallback(playerCallback)
    }

    private fun switchToMode(mode: String) {
        if (currentMode == mode) return

        currentMode = mode
        updateButtonStates(mode)

        // Resetear el índice cuando se cambia de modo
        if (mode == "NEXT_EPISODE_CUSTOM") {
            currentEpisodeIndex = 0
        }

        val newConfig = createConfigForMode(mode)
        player?.reloadPlayer(newConfig)
    }

    private fun updateButtonStates(selectedMode: String) {
        if (selectedMode == "NEXT_EPISODE") {
            btnNextEpisode.setBackgroundColor(Color.parseColor("#0066CC"))
            btnNextEpisode.setTextColor(Color.WHITE)
            btnNextEpisodeCustom.setBackgroundColor(Color.parseColor("#97D700"))
            btnNextEpisodeCustom.setTextColor(Color.BLACK)
        } else {
            btnNextEpisode.setBackgroundColor(Color.parseColor("#97D700"))
            btnNextEpisode.setTextColor(Color.BLACK)
            btnNextEpisodeCustom.setBackgroundColor(Color.parseColor("#0066CC"))
            btnNextEpisodeCustom.setTextColor(Color.WHITE)
        }
    }

    private fun createConfigForMode(mode: String): MediastreamPlayerConfig {
        return when (mode) {
            "NEXT_EPISODE" -> {
                MediastreamPlayerConfig().apply {
                    id = "6839b2d6a4149963bfe295e0"
                    environment = MediastreamPlayerConfig.Environment.DEV
                    type = MediastreamPlayerConfig.VideoTypes.EPISODE
                    isDebug = true
                    appHandlesWindowInsets = true
                }
            }
            "NEXT_EPISODE_CUSTOM" -> {
                MediastreamPlayerConfig().apply {
                    id = "68891e8d1856d6378f5d81fa"
                    environment = MediastreamPlayerConfig.Environment.PRODUCTION
                    type = MediastreamPlayerConfig.VideoTypes.VOD
                    nextEpisodeId = nextEpisodeIds.firstOrNull()
                    isDebug = true
                    appHandlesWindowInsets = true
                }
            }
            else -> MediastreamPlayerConfig()
        }
    }

    private fun createPlayerCallback(): MediastreamPlayerCallback {
        return object : MediastreamPlayerCallback {
            override fun playerViewReady(msplayerView: PlayerView?) {}

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

            override fun onPlayerClosed() {}

            override fun onBuffering() {}

            override fun onError(error: String?) {
                Log.e(TAG, "onError: $error")
            }

            override fun onNext() {
                Log.d(TAG, "onNext")
            }

            override fun onPrevious() {}

            override fun nextEpisodeIncoming(nextEpisodeId: String) {
                Log.d(TAG, "nextEpisodeIncoming: $nextEpisodeId")

                if (currentMode == "NEXT_EPISODE") {
                    Log.d(TAG, "nextEpisodeIncoming (default): $nextEpisodeId")
                } else if (currentMode == "NEXT_EPISODE_CUSTOM") {
                    Log.d(TAG, "nextCustomEpisodeIncoming: $nextEpisodeId")

                    // Usar el ID que pasa el SDK como fuente de verdad (evita desincronización
                    // si el callback se invoca más de una vez por transición)
                    val indexInList = nextEpisodeIds.indexOf(nextEpisodeId)
                    val indexToUse = if (indexInList >= 0) indexInList else currentEpisodeIndex

                    if (indexToUse < nextEpisodeIds.size) {
                        val idToLoad = nextEpisodeIds[indexToUse]
                        val nextConfig = MediastreamPlayerConfig().apply {
                            id = idToLoad
                            type = MediastreamPlayerConfig.VideoTypes.VOD
                            environment = MediastreamPlayerConfig.Environment.PRODUCTION
                            isDebug = true
                            val nextIndex = indexToUse + 1
                            if (nextIndex < nextEpisodeIds.size) {
                                this.nextEpisodeId = nextEpisodeIds[nextIndex]
                            }
                        }
                        currentEpisodeIndex = indexToUse + 1
                        Log.d(TAG, "NEXT ID: ${nextConfig?.nextEpisodeId}")
                        player?.updateNextEpisode(nextConfig)
                    }
                }
            }

            override fun onFullscreen() {}

            override fun offFullscreen() {
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

            override fun onPlaybackErrors(error: JSONObject?) {}

            override fun onEmbedErrors(error: JSONObject?) {}

            override fun onLiveAudioCurrentSongChanged(data: JSONObject?) {}

            override fun onDismissButton() {}

            override fun onPlayerReload() {}
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
