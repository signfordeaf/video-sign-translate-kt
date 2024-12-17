package com.weaccess.wesignproject

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.weaccess.wesign.WeAccessConfig
import com.weaccess.wesign.WeSign


class MainActivity : ComponentActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer
    private lateinit var weSign: WeSign

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WeAccessConfig.initialize("YOUR-API-KEY")
        setContentView(R.layout.activity_main)
        weSign = findViewById(R.id.weSign)
        initWeSign()
        playerView.setFullscreenButtonClickListener { playerView.useController = true }
        player.prepare()
        player.play()
        }

    private fun initWeSign() {
        playerView = PlayerView(this)
        player = ExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4")
        player.setMediaItem(mediaItem)
        playerView.player = player
        weSign.setPlayer(playerView)
    }
}

