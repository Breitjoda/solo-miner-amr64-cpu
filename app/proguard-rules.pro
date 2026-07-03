-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

-keep class com.google.gson.** { *; }
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keep class sun.misc.Unsafe { *; }

-keep class com.breitjoda.mineram64.** { *; }
-keepclassmembers class com.breitjoda.mineram64.** { *; }

-keep class kotlin.** { *; }
-keepclassmembers class kotlin.** { *; }
-keep class org.bouncycastle.** { *; }
