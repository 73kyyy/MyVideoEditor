package com.myvideo.editor.feature.project.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ProjectDatabase(context: Context) : SQLiteOpenHelper(context, "nexclip_projects.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE projects (id TEXT PRIMARY KEY, name TEXT, width INTEGER, height INTEGER, fps INTEGER, created_at INTEGER, updated_at INTEGER, duration INTEGER, thumbnail TEXT)")
    }
    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) { db.execSQL("DROP TABLE IF EXISTS projects"); onCreate(db) }

    fun insert(id: String, name: String, w: Int, h: Int, fps: Int) {
        writableDatabase.insert("projects", null, ContentValues().apply {
            put("id", id); put("name", name); put("width", w); put("height", h); put("fps", fps)
            put("created_at", System.currentTimeMillis()); put("updated_at", System.currentTimeMillis()); put("duration", 0)
        })
    }
    fun getAll(): List<Triple<String, String, Long>> {
        val result = mutableListOf<Triple<String, String, Long>>()
        readableDatabase.rawQuery("SELECT id, name, updated_at FROM projects ORDER BY updated_at DESC", null).use {
            while (it.moveToNext()) result.add(Triple(it.getString(0), it.getString(1), it.getLong(2)))
        }
        return result
    }
    fun delete(id: String) { writableDatabase.delete("projects", "id=?", arrayOf(id)) }
}
