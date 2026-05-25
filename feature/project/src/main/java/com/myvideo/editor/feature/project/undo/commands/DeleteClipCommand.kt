package com.myvideo.editor.feature.project.undo.commands

import com.myvideo.editor.feature.project.ClipData
import com.myvideo.editor.feature.project.TrackData
import com.myvideo.editor.feature.project.undo.Command

class DeleteClipCommand(private val track: TrackData, private val clip: ClipData) : Command {
    private var index = -1
    override fun execute() { index = track.clips.indexOf(clip); track.clips.remove(clip) }
    override fun undo() { if (index >= 0) track.clips.add(index, clip) else track.clips.add(clip) }
}
