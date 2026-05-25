package com.myvideo.editor.feature.subtitle

class SubtitleTemplateApplier {

    data class Template(
        val name: String, val style: SubtitleStyle,
        val animIn: SubtitleAnimationEngine.AnimationConfig = SubtitleAnimationEngine.AnimationConfig(SubtitleAnimationEngine.AnimationType.None),
        val animOut: SubtitleAnimationEngine.AnimationConfig = SubtitleAnimationEngine.AnimationConfig(SubtitleAnimationEngine.AnimationType.None)
    )

    companion object {
        val TEMPLATES = listOf(
            Template("标准", SubtitleStyle()),
            Template("白字黑边", SubtitleStyle(fontSize = 28, fontColor = 0xFFFFFFFF, outlineColor = 0xFF000000, outlineWidth = 2f)),
            Template("黄字阴影", SubtitleStyle(fontSize = 26, fontColor = 0xFFFFFF00, shadowColor = 0x80000000, shadowDx = 3f, shadowDy = 3f)),
            Template("霓虹", SubtitleStyle(fontSize = 30, fontColor = 0xFFFF00FF, outlineColor = 0xFF00FFFF, outlineWidth = 1.5f, isBold = true)),
            Template("新闻", SubtitleStyle(fontSize = 22, fontColor = 0xFFFFFFFF, bgColor = 0xCC000000, isBold = true)),
            Template("卡拉OK", SubtitleStyle(fontSize = 32, fontColor = 0xFFFF6600, isBold = true, shadowColor = 0xFF000000, shadowDx = 2f, shadowDy = 2f)),
            Template("手写体", SubtitleStyle(fontSize = 28, fontColor = 0xFF333333, fontFamily = "cursive", isItalic = true)),
            Template("渐入渐出", SubtitleStyle(),                SubtitleAnimationEngine.AnimationConfig(SubtitleAnimationEngine.AnimationType.FadeIn, 500),
                SubtitleAnimationEngine.AnimationConfig(SubtitleAnimationEngine.AnimationType.FadeOut, 500)),
            Template("打字机", SubtitleStyle(fontSize = 20, fontColor = 0xFF00FF00),
                SubtitleAnimationEngine.AnimationConfig(SubtitleAnimationEngine.AnimationType.Typewriter, 1000)),
            Template("弹入", SubtitleStyle(fontSize = 26),
                SubtitleAnimationEngine.AnimationConfig(SubtitleAnimationEngine.AnimationType.Scale, 400))
        )
    }

    fun getTemplate(name: String): Template? = TEMPLATES.find { it.name == name }
    fun applyTemplate(manager: SubtitleManager, templateName: String) {
        val template = getTemplate(templateName) ?: return
        manager.getAll().forEach { manager.updateStyle(it.id, template.style) }
    }
}
