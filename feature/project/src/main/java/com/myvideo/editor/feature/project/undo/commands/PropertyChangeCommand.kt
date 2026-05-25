package com.myvideo.editor.feature.project.undo.commands

import com.myvideo.editor.feature.project.ClipData
import com.myvideo.editor.feature.project.undo.Command

class PropertyChangeCommand(private val clip: ClipData, private val property: String, private val newValue: Any, private val oldValue: Any) : Command {
    override fun execute() { applyValue(newValue) }
    override fun undo() { applyValue(oldValue) }
    private fun applyValue(v: Any) { when (property) { "speed" -> clip.speed = v as Float; "volume" -> clip.volume = v as Float } }
}
