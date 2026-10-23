# ProGuard / R8 optimization rules for Kenz Almuslim

# Preserve line numbers and source files for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep general attributes needed for JSON parsing and Reflection
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep WorkManager & WorkDatabase (Fixes "Failed to create an instance of androidx.work.impl.WorkDatabase")
-dontwarn androidx.room.**
-keep class androidx.work.impl.WorkDatabase { *; }
-keep class androidx.work.impl.WorkDatabase_Impl {
    public <init>();
    *;
}
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Keep AndroidX App Startup Initializers
-keep class * implements androidx.startup.Initializer {
    public <init>();
}

# Keep DataBinding
-keep class * extends androidx.databinding.ViewDataBinding {
    public <init>(...);
}
-keep class **.DataBinderMapperImpl { *; }

# Keep Custom Views
-keepclasseswithmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep app models and entities
-keep class com.kenz.almuslim.data.model.** { *; }
-keepclassmembers class com.kenz.almuslim.data.model.** { *; }
-keep class com.kenz.almuslim.data.local.model.** { *; }
-keepclassmembers class com.kenz.almuslim.data.local.model.** { *; }
-keep class com.benkkstudio.bsjson.** { *; }

# Keep Gson SerializedName fields
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn com.google.gson.**

# Keep SQLiteAssetHelper
-keep class com.readystatesoftware.sqliteasset.** { *; }

# Keep Fast-Android-Networking & Loopj
-dontwarn com.androidnetworking.**
-dontwarn com.loopj.android.http.**
-dontwarn cz.msebera.android.httpclient.**
-dontwarn org.conscrypt.**

# Keep RxJava annotations / dontwarn
-dontwarn io.reactivex.**

# Keep Glide modules
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-dontwarn com.bumptech.glide.**

# Keep Picasso
-dontwarn com.squareup.picasso.**

# Keep Google Play Services Ads & Firebase
-dontwarn com.google.android.gms.**
-dontwarn com.google.firebase.**

# Keep Lottie
-dontwarn com.airbnb.lottie.**

# Ignore annotations / tools warnings
-dontwarn org.checkerframework.**
-dontwarn org.jspecify.**
-dontwarn com.google.errorprone.**
