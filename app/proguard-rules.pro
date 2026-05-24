# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ==================== 为该项目添加的保护规则 ====================

# 保留 Secrets 插件生成的 BuildConfig 和 API Key
-keep class com.example.BuildConfig { *; }
-keepclassmembers class com.example.BuildConfig {
    public static <fields>;
}

# Compose 相关
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }

# Moshi + Retrofit + OkHttp
-keep class com.squareup.moshi.** { *; }
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers,allowobfuscation class * {
    @com.squareup.moshi.JsonClass <methods>;
}

# Room
-keep class androidx.room.** { *; }

# 保留所有 Application 和 Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity

# 防止混淆后导致的问题
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
