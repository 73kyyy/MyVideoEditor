# NexClip ProGuard Rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep class com.myvideo.editor.** { *; }
-keepclassmembers class * { native <methods>; }
