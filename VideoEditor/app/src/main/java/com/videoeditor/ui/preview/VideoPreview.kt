package com.videoeditor.ui.preview

import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.videoeditor.data.model.Project
import com.videoeditor.engine.PreviewEngine

@Composable
fun VideoPreview(
    project: Project,
    isPlaying: Boolean,
    currentPositionUs: Long,
    onPositionChange: (Long) -> Unit,
    onPlayPauseToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val previewEngine = remember { PreviewEngine(androidx.compose.ui.platform.LocalContext.current) }

    Box(
        modifier = modifier.background(Color.Black)
    ) {
        AndroidView(
            factory = { context ->
                TextureView(context).apply {
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
                            previewEngine.initialize(surface)
                            if (project.videoTracks.isNotEmpty()) {
                                previewEngine.loadProject(project)
                            }
                        }

                        override fun onSurfaceTextureSizeChanged(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {}
                        override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean = true
                        override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) {}
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    // Control playback state
    LaunchedEffect(isPlaying) {
        if (isPlaying) previewEngine.play() else previewEngine.pause()
    }

    // Load project changes
    LaunchedEffect(project) {
        previewEngine.loadProject(project)
    }

    // Seek position
    LaunchedEffect(currentPositionUs) {
        previewEngine.seekTo(currentPositionUs)
    }

    DisposableEffect(Unit) {
        onDispose { previewEngine.release() }
    }
}
