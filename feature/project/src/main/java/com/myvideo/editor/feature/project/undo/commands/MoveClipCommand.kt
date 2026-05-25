package com.myvideo.editor.feature.project.undo.commands

import com.myvideo.editor.feature.project.ClipData
import com.myvideo.editor.feature.project.undo.Command

class MoveClipCommand(private val clip: ClipData, private val newStartMs: Long) : Command {
    private val oldStartMs = clip.startMs; private val oldEndMs = clip.endMs; private val duration = clip.durationMs
    override fun execute() { clip.startMs = newStartMs; clip.endMs = newStartMs + duration }
    override fun undo() { clip.startMs = oldStartMs; clip.endMs = oldEndMs }
}
