package com.weaccess.wesign

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.weaccess.wesign.model.SignModel
import com.weaccess.wesign.model.VideoSignModel
import com.weaccess.wesign.service.ApiService

class WeSign @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val apiService = ApiService()
    private val gifImageView: ImageView
    private  val buttonImageView: ImageView
    private lateinit var outPlayer: PlayerView
    private var isZoomed = false
    private val handler = Handler(Looper.getMainLooper())
    private val checkRunnable = object : Runnable {
        override fun run() {
            val model = getSavedModel(context, "videoBundleId")
            if (model != null) {
                signModel = model.data
            } else {
                Log.d("DEVOPS-WA", "Model henüz bulunamadı.")
            }
            handler.postDelayed(this, 5000) // 5 saniye aralıklarla kontrol et
        }
    }
    val handlerDuration = Handler(Looper.getMainLooper())
    private val updateRunnable: Runnable
    private var currentGifIndex = -1
    private var signModel: List<SignModel>? = null


    init {
        Log.d("DEVOPS-WA", "WeSign init ${context.packageName}")
        val view = LayoutInflater.from(context).inflate(R.layout.wesign_frame, this, true)
        gifImageView = view.findViewById(R.id.gif_image)
        buttonImageView = view.findViewById(R.id.controller_button)
        loadGif(R.drawable.sign_example)
        buttonImageView.setOnClickListener(){
            visibleSign()
        }
        updateRunnable = object : Runnable {
            override fun run() {
                val duration = outPlayer.player?.currentPosition
                if (duration != null && duration != C.TIME_UNSET) {
                    if (signModel != null) {
                        getGifImage(signModel, duration / 1000, gifImageView)
                    }
                }
                handlerDuration.postDelayed(this, 1000)
            }
        }

    }

    fun setPlayer(player: PlayerView) {
        player.parent?.let {
            (it as ViewGroup).removeView(player)
        }
        val container = findViewById<FrameLayout>(R.id.video_container)
        container.removeAllViews()
        container.addView(player)
        outPlayer = player
        if (outPlayer.player != null) {
            outPlayer.player?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        handlerDuration.post(updateRunnable)
                    } else {
                        handlerDuration.removeCallbacks(updateRunnable)
                    }
                }
            })
        }
//        apiService.videoUpload(File("video.mp4"), "videoBundleId", context)
        Log.d("DEVOPS-WA", "VideoBundleId: ${context.packageName}.${player.player?.mediaMetadata?.title}")
        dragAndMoveSignGIF(player)
        visibleSignTranslateButton(player)
        startChecking()

    }

    private fun loadGif(gifResource: Int) {
        gifImageView.visibility = View.VISIBLE
        Glide.with(context)
            .asGif()
            .load(gifResource)
            .into(gifImageView)
        buttonImageView.visibility = View.VISIBLE
        Glide.with(context)
            .asDrawable()
            .load(R.drawable.engelsizceviri)
            .into(buttonImageView)
    }

    fun getSavedModel(context: Context, videoBundleId: String): VideoSignModel? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("VideoSignModelPrefs", Context.MODE_PRIVATE)
        val jsonModel = sharedPreferences.getString(videoBundleId, null)
        return if (jsonModel != null) {
            val gson = Gson()
            gson.fromJson(jsonModel, VideoSignModel::class.java)
        } else {
            null
        }
    }

    private fun getGifImage(sm: List<SignModel>?, duration: Long?, imageView: ImageView) {
        if (sm != null && duration != null) {
            for (item in sm) {
                val startTimeMs = item.st ?: 0.0

                if (item.q != null &&
                    item.q!! > currentGifIndex &&
                    duration >= startTimeMs) {
                    currentGifIndex = item.q!!
                    Handler(Looper.getMainLooper()).post {
                        item.vu?.let { gifUrl ->
                            Glide.with(imageView.context)
                                .asGif()
                                .load(gifUrl)
                                .into(imageView)
                        }
                    }
                    break
                }
            }
        }
    }

    private fun startChecking() {
        handler.post(checkRunnable)
    }
    private fun stopChecking() {
        handler.removeCallbacks(checkRunnable)
    }

    private fun dpToPx(dp: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun toggleGifSize() {
        isZoomed = !isZoomed
        val newSizeDp = if (isZoomed) 200 else 100
        val newSizePx = dpToPx(newSizeDp, gifImageView.context)
        // Normal boyuta geri dön
        gifImageView.layoutParams.width = newSizePx // Normal genişlik
        gifImageView.layoutParams.height = newSizePx // Normal yükseklik
        gifImageView.requestLayout() // Değişiklikleri uygula
    }

    private fun visibleSignTranslateButton(player: PlayerView) {
        val controllerVisibilityListener = object : PlayerView.ControllerVisibilityListener {
            fun onVisibilityChange(visibility: Int) {
                if (visibility == View.VISIBLE) {
                    buttonImageView.visibility = View.VISIBLE
                } else {
                    buttonImageView.visibility = View.GONE
                }
            }
            override fun onVisibilityChanged(visibility: Int) {
                if (visibility == View.VISIBLE) {
                    buttonImageView.visibility = View.VISIBLE
                } else {
                    buttonImageView.visibility = View.GONE
                }
            }
        }
        player.setControllerVisibilityListener(controllerVisibilityListener)
    }

    private  fun visibleSign(){
        if (gifImageView.visibility == View.VISIBLE) {
            gifImageView.visibility = View.GONE
        } else {
            gifImageView.visibility = View.VISIBLE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun dragAndMoveSignGIF(playerView: PlayerView) {
        gifImageView.setOnTouchListener(object : OnTouchListener {
            var dX = 0f
            var dY = 0f
            var parentWidth = 0
            var parentHeight = 0
            var startX = 0f
            var startY = 0f
            var startTime = 0L
            val CLICK_THRESHOLD = 10
            val TIME_THRESHOLD = 200

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.rawX
                        startY = event.rawY
                        startTime = System.currentTimeMillis()
                        dX = view.x - event.rawX
                        dY = view.y - event.rawY
                        parentWidth = playerView.width
                        parentHeight = playerView.height
                    }
                    MotionEvent.ACTION_MOVE -> {
                        var newX = event.rawX + dX
                        var newY = event.rawY + dY
                        val gifWidth = view.width
                        val gifHeight = view.height
                        newX = when {
                            newX < 0 -> 0f
                            newX + gifWidth > parentWidth -> (parentWidth - gifWidth).toFloat()
                            else -> newX
                        }
                        newY = when {
                            newY < 0 -> 0f
                            newY + gifHeight > parentHeight -> (parentHeight - gifHeight).toFloat()
                            else -> newY
                        }
                        view.animate()
                            .x(newX)
                            .y(newY)
                            .setDuration(0)
                            .start()
                    }
                    MotionEvent.ACTION_UP -> {
                        val distanceX = Math.abs(event.rawX - startX)
                        val distanceY = Math.abs(event.rawY - startY)
                        val elapsedTime = System.currentTimeMillis() - startTime
                        if (distanceX < CLICK_THRESHOLD && distanceY < CLICK_THRESHOLD && elapsedTime < TIME_THRESHOLD) {
                            view.performClick()
                            gifImageView.setOnClickListener {
                                toggleGifSize()
                            }
                        }
                    }
                }
                return true
            }
        })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopChecking()
        apiService.cancelRequest()
    }
}
