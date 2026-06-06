package com.videoeditor.data.model

import java.util.UUID

data class Transition(
    val id: String = UUID.randomUUID().toString(),
    val type: TransitionType = TransitionType.NONE,
    val durationUs: Long = 500_000L, // 0.5s default
    val clipOutId: String = "",
    val clipInId: String = ""
)

enum class TransitionType(val displayName: String) {
    NONE("无"),
    FADE("淡入淡出"),
    DISSOLVE("溶解"),
    SLIDE_LEFT("左滑"),
    SLIDE_RIGHT("右滑"),
    SLIDE_UP("上滑"),
    SLIDE_DOWN("下滑"),
    ZOOM_IN("放大"),
    ZOOM_OUT("缩小"),
    WIPE("擦除"),
    CIRCLE("圆形")
}
