# kotlinx.serialization keeps the generated serializers referenced only reflectively.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class eu.kutscheid.elegoomonitor.** {
    *** Companion;
}
-keepclasseswithmembers class eu.kutscheid.elegoomonitor.** {
    kotlinx.serialization.KSerializer serializer(...);
}
