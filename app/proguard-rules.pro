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
# R8 (AGP 9.5) miscompiles Ktor's UDP socket classes: DatagramSendChannel fails ART verification
# ("cannot access instance field ... Datagram.address"), so binding the discovery socket throws a
# VerifyError and no printer is ever found in release builds. Keep them out of optimization.
-keep class io.ktor.network.sockets.** { *; }
