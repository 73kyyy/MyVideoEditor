package com.myvideo.editor.feature.project.undo

class CommandManager(private val maxSteps: Int = 50) {
    private val undoStack = mutableListOf<Command>()
    private val redoStack = mutableListOf<Command>()

    fun execute(cmd: Command) { cmd.execute(); undoStack.add(cmd); if (undoStack.size > maxSteps) undoStack.removeAt(0); redoStack.clear() }
    fun undo() { val cmd = undoStack.removeLastOrNull(); cmd?.undo(); if (cmd != null) redoStack.add(cmd) }
    fun redo() { val cmd = redoStack.removeLastOrNull(); cmd?.redo(); if (cmd != null) undoStack.add(cmd) }
    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()
    fun clear() { undoStack.clear(); redoStack.clear() }
    fun getUndoCount(): Int = undoStack.size
    fun getRedoCount(): Int = redoStack.size
}
