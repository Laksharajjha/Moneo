# Moneo ProGuard rules
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Hilt
-keepclasseswithmembernames class * { @dagger.hilt.* <methods>; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }
