package com.videoeditor.engine

import android.content.Context
import android.graphics.SurfaceTexture
import android.net.Uri
import android.view.Surface
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.videoeditor.data.model.Project
import com.videoeditor.data.model.VideoClip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreviewEngine(private val context: Context) {

    private var player: ExoPlayer? = null
    private var surface: Surface? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPositionUs = MutableStateFlow(0L)
    val currentPositionUs: StateFlow<Long> = _currentPositionUs

    private val _durationUs = MutableStateFlow(0L)
    val durationUs: StateFlow<Long> = _durationUs

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentClipIndex = 0
    private var clips: List<VideoClip> = emptyList()

    fun initialize(surfaceTexture: SurfaceTexture) {
        release()
        this.surface = Surface(surfaceTexture)

        player = ExoPlayer.Builder(context).build().apply {
            setSurface(this@PreviewEngine.surface)
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    _isReady.value = playbackState == Player.STATE_READY
                    if (playbackState == Player.STATE_ENDED) {
                        _isPlaying.value = false
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    _error.value = error.message
                }
            })
        }
    }

    fun loadProject(project: Project) {
        clips = project.videoTracks.flatMap { it.clips }.sortedBy { it.startUs }
        _durationUs.value = project.totalDurationUs()
        if (clips.isNotEmpty()) {
            loadClip(0)
        }
    }

    private fun loadClip(index: Int) {
        if (index !in clips.indices) return
        currentClipIndex = index
        val clip = clips[index]

        val mediaItem = MediaItem.fromUri(Uri.parse(clip.sourcePath))
        player?.apply {
            setMediaItem(mediaItem)
            prepare()
            seekTo(clip.trimStartUs / 1000)
        }
    }

    fun play() {
        player?.play()
    }

    fun pause() {
        player?.pause()
    }

    fun togglePlayPause() {
        if (_isPlaying.value) pause() else play()
    }

    fun seekTo(positionUs: Long) {
        // Find which clip this position falls into
        var accumulatedUs = 0L
        for ((index, clip) in clips.withIndex()) {
            val clipDurationUs = clip.trimEndUs - clip.trimStartUs
            if (positionUs < accumulatedUs + clipDurationUs) {
                val offsetInClip = positionUs - accumulatedUs
                val seekPosition = clip.trimStartUs + offsetInClip
                currentClipIndex = index
                player?.seekTo(index, seekPosition / 1000)
                break
            }
            accumulatedUs += clipDurationUs
        }
    }

    fun getCurrentPositionMs(): Long {
        return player?.currentPosition ?: 0L
    }

    fun release() {
        player?.release()
        player = null
        surface?.release()
        surface = null
        _isPlaying.value = false
        _isReady.value = false
    }
}
