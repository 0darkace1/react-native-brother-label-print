# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Brother SDK classes
-keep class com.brother.** { *; }
-dontwarn com.brother.**

# Keep React Native classes
-keep class com.facebook.react.** { *; }
-keep class com.reactnativebrotherlabelprintmodule.** { *; } 