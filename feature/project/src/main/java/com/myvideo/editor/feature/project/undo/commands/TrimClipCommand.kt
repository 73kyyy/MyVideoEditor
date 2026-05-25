package com.myvideo.editor.feature.project.undo.commands

import com.myvideo.editor.feature.project.ClipData
import com.myvideo.editor.feature.project.undo.Command

class TrimClipCommand(private val clip: ClipData, private val newStart: Long, private val newEnd: Long) : Command {
    private val oldStart = clip.startMs; private val oldEnd = clip.endMs
    override fun execute() { clip.startMs = newStart; clip.endMs = newEnd }
    override fun undo() { clip.startMs = oldStart; clip.endMs = oldEnd }
}
