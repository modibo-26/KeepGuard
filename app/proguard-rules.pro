# Keep ALL app classes + annotations + interfaces
-keep class com.modibo.keepguard.** { *; }
-keep interface com.modibo.keepguard.** { *; }
-keep @interface com.modibo.keepguard.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class dagger.** { *; }
-keep interface dagger.** { *; }
-keep @interface dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class androidx.hilt.** { *; }
-keep @dagger.internal.DaggerGenerated class * { *; }
-keep @dagger.internal.KeepFieldType class * { *; }
-keep @dagger.internal.IdentifierNameString class * { *; }
-keepclassmembers class * {
    @dagger.internal.KeepFieldType *;
}
-keepclassmembers class * {
    @dagger.internal.IdentifierNameString *;
}

# Firebase DTOs
-keep class com.modibo.keepguard.data.remote.dto.** { *; }

# Domain models (enums used in Firestore valueOf)
-keep class com.modibo.keepguard.domain.model.** { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers @kotlinx.serialization.Serializable class com.modibo.keepguard.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class com.modibo.keepguard.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.modibo.keepguard.**$$serializer { *; }
-keep class kotlinx.serialization.** { *; }

# Suppress missing class warnings
-dontwarn com.squareup.okhttp.**
-dontwarn java.lang.management.**
-dontwarn java.lang.reflect.AnnotatedType
-dontwarn io.grpc.**
-dontwarn io.ktor.**
-dontwarn com.google.common.**

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
