# ProGuard Rules for Taxi App
# This file contains ProGuard configurations for release builds

#-------------------------------------------------------------------------------------------------
# GENERAL ANDROID RULES
#-------------------------------------------------------------------------------------------------

# Keep line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Keep annotations
-keepattributes *Annotation*

# Preserve the special static methods that are required in all enumeration classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#-------------------------------------------------------------------------------------------------
# FIREBASE RULES
#-------------------------------------------------------------------------------------------------

# Firebase Authentication
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.internal.** { *; }

# Firebase Realtime Database
-keep class com.google.firebase.database.** { *; }
-keepclassmembers class com.google.firebase.database.** { *; }

# Firebase Analytics
-keep class com.google.firebase.analytics.** { *; }

# Firebase Crashlytics
-keep class com.google.firebase.crashlytics.** { *; }
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Firebase Performance
-keep class com.google.firebase.perf.** { *; }

# Firebase Common
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

#-------------------------------------------------------------------------------------------------
# GOOGLE MAPS RULES
#-------------------------------------------------------------------------------------------------

-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }
-keep class com.google.maps.android.** { *; }

#-------------------------------------------------------------------------------------------------
# RETROFIT & NETWORKING RULES
#-------------------------------------------------------------------------------------------------

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

#-------------------------------------------------------------------------------------------------
# KOTLIN COROUTINES
#-------------------------------------------------------------------------------------------------

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

#-------------------------------------------------------------------------------------------------
# JETPACK COMPOSE
#-------------------------------------------------------------------------------------------------

-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.compose.**

#-------------------------------------------------------------------------------------------------
# HILT/DAGGER
#-------------------------------------------------------------------------------------------------

-dontwarn com.google.errorprone.annotations.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keepclassmembers class * {
    @javax.inject.Inject <fields>;
    @javax.inject.Inject <init>(...);
}

#-------------------------------------------------------------------------------------------------
# YOUR APP'S DATA MODELS
#-------------------------------------------------------------------------------------------------

# Keep all model classes - Update package name after changing from com.tecvo.taxi
-keep class com.tecvo.taxi.models.** { *; }
-keep class com.tecvo.taxi.data.** { *; }

# Keep Firebase Database models
-keepclassmembers class com.tecvo.taxi.models.** {
    *;
}

# Keep classes used with Gson
-keep class com.tecvo.taxi.services.geocoding.** { *; }

#-------------------------------------------------------------------------------------------------
# DATASTORE
#-------------------------------------------------------------------------------------------------

-keep class androidx.datastore.** { *; }
-keep class * extends androidx.datastore.core.Serializer { *; }

#-------------------------------------------------------------------------------------------------
# ACCOMPANIST
#-------------------------------------------------------------------------------------------------

-keep class com.google.accompanist.** { *; }

#-------------------------------------------------------------------------------------------------
# COIL IMAGE LOADING
#-------------------------------------------------------------------------------------------------

-keep class coil.** { *; }

#-------------------------------------------------------------------------------------------------
# MATERIAL DESIGN
#-------------------------------------------------------------------------------------------------

-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

#-------------------------------------------------------------------------------------------------
# PHONE NUMBER LIBRARY
#-------------------------------------------------------------------------------------------------

-keep class com.google.i18n.phonenumbers.** { *; }
-keep class io.michaelrocks.libphonenumber.android.** { *; }

#-------------------------------------------------------------------------------------------------
# TIMBER LOGGING
#-------------------------------------------------------------------------------------------------

-dontwarn org.jetbrains.annotations.**
-keep class timber.log.** { *; }

#-------------------------------------------------------------------------------------------------
# REMOVE LOGGING IN RELEASE BUILDS
#-------------------------------------------------------------------------------------------------

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

-assumenosideeffects class timber.log.Timber* {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}

#-------------------------------------------------------------------------------------------------
# BUILD CONFIG
#-------------------------------------------------------------------------------------------------

# SECURE BuildConfig - Keep only essential fields, allow API keys to be obfuscated
-keepclassmembers class com.tecvo.taxi.BuildConfig {
    public static final boolean DEBUG;
    public static final String APPLICATION_ID;
    public static final String BUILD_TYPE;
    public static final String BUILD_TIME;
    public static final String GIT_COMMIT;
    public static final int VERSION_CODE;
    public static final String VERSION_NAME;
    # Note: API keys (MAPS_API_KEY, GEOCODING_API_KEY, etc.) intentionally NOT kept
    # This makes them much harder to extract via reverse engineering while preserving functionality
}

#-------------------------------------------------------------------------------------------------
# JAVASCRIPT INTERFACE (if using WebView)
#-------------------------------------------------------------------------------------------------

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

#-------------------------------------------------------------------------------------------------
# NATIVE METHODS
#-------------------------------------------------------------------------------------------------

-keepclasseswithmembernames class * {
    native <methods>;
}

#-------------------------------------------------------------------------------------------------
# PARCELABLE
#-------------------------------------------------------------------------------------------------

-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

#-------------------------------------------------------------------------------------------------
# R8 FULL MODE RULES
#-------------------------------------------------------------------------------------------------

# R8 full mode strips generic signatures by default
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep generic type information for Gson
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type

#-------------------------------------------------------------------------------------------------
# WARNINGS TO IGNORE
#-------------------------------------------------------------------------------------------------

-dontwarn java.lang.invoke.**
-dontwarn org.slf4j.**

#-------------------------------------------------------------------------------------------------
# IMPORTANT NOTES:
#-------------------------------------------------------------------------------------------------

# After changing your package name from com.tecvo.taxi, update these lines:
# Line 117: -keep class com.tecvo.taxi.models.** { *; }
# Line 118: -keep class com.tecvo.taxi.data.** { *; }
# Line 121: -keepclassmembers class com.tecvo.taxi.models.** {
# Line 126: -keep class com.tecvo.taxi.services.geocoding.** { *; }
# Line 200: -keep class com.tecvo.taxi.BuildConfig { *; }

# Test your release build thoroughly after applying these rules!
# If you encounter issues, check logcat for missing classes and add keep rules as needed.