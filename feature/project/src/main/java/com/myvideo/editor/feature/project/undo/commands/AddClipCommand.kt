package com.myvideo.editor.feature.project.undo.commands

import com.myvideo.editor.feature.project.ClipData
import com.myvideo.editor.feature.project.TrackData
import com.myvideo.editor.feature.project.undo.Command

class AddClipCommand(private val track: TrackData, private val clip: ClipData) : Command {
    override fun execute() { track.clips.add(clip); track.clips.sortBy { it.startMs } }
    override fun undo() { track.clips.remove(clip) }
}
