package com.myvideo.editor.core.video.color

import com.myvideo.editor.core.video.color.model.HSLAdjustment

class HSLSecondaryProcessor {
    fun apply(pixels: IntArray, adjustments: List<HSLAdjustment>): IntArray {
        if (adjustments.isEmpty()) return pixels
        return IntArray(pixels.size) { i ->
            val p = pixels[i]
            var r = (p shr 16 and 0xFF) / 255f
            var g = (p shr 8 and 0xFF) / 255f
            var b = (p and 0xFF) / 255f
            val max = maxOf(r, g, b); val min = minOf(r, g, b)
            val h = getHue(r, g, b, max, min)
            val s = if (max == min) 0f else if ((max+min)/2f > 0.5f) (max-min)/(2f-max-min) else (max-min)/(max+min)
            val l = (max + min) / 2f
            var dh = 0f; var ds = 0f; var dl = 0f
            adjustments.forEach { adj ->
                if (h in adj.range.hueMin..adj.range.hueMax || (adj.range.hueMin > adj.range.hueMax && (h >= adj.range.hueMin || h <= adj.range.hueMax))) {
                    if (s in adj.range.satMin..adj.range.satMax && l in adj.range.lightMin..adj.range.lightMax) {
                        dh += adj.hueShift; ds += adj.saturation; dl += adj.lightness
                    }
                }
            }
            if (dh == 0f && ds == 0f && dl == 0f) p
            else hslToPixel((h + dh + 360f) % 360f, (s + ds).coerceIn(0f, 1f), (l + dl).coerceIn(0f, 1f))
        }
    }

    private fun getHue(r: Float, g: Float, b: Float, max: Float, min: Float): Float {
        if (max == min) return 0f
        val d = max - min
        return when (max) { r -> ((g-b)/d+(if(g<b)6 else 0))*60f; g -> ((b-r)/d+2)*60f; else -> ((r-g)/d+4)*60f }
    }

    private fun hslToPixel(h: Float, s: Float, l: Float): Int {
        if (s == 0f) { val v = (l*255).toInt().coerceIn(0,255); return 0xFF000000.toInt() or (v shl 16) or (v shl 8) or v }
        val q = if (l < 0.5f) l*(1f+s) else l+s-l*s; val p = 2f*l-q
        fun h2(p:Float,q:Float,t:Float):Float { var tt=t; if(tt<0f)tt+=1f; if(tt>1f)tt-=1f; return when{tt<1f/6f->p+(q-p)*6f*tt;tt<0.5f->q;tt<2f/3f->p+(q-p)*(2f/3f-tt)*6f;else->p} }
        val r = (h2(p,q,h/360f+1f/3f)*255).toInt().coerceIn(0,255)
        val g = (h2(p,q,h/360f)*255).toInt().coerceIn(0,255)
        val b = (h2(p,q,h/360f-1f/3f)*255).toInt().coerceIn(0,255)
        return 0xFF000000.toInt() or (r shl 16) or (g shl 8) or b
    }
}
