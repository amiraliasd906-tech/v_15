# Keep serialization classes used for the WebView JS bridge and the
# optional direct Anthropic API call
-keepattributes *Annotation*
-keepclassmembers class com.divarsmartsearch.app.data.webview.** { *; }
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
