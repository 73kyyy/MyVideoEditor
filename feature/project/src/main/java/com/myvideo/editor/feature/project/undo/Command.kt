package com.myvideo.editor.feature.project.undo

interface Command {
    fun execute()
    fun undo()
    fun redo() { execute() }
}
