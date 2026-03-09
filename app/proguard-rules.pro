# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Firebase model classes
-keepclassmembers class com.example.project.** {
  *** get*();
  *** set*(***);
  public <init>();
}

# Alternatively, keep all classes in the package if they are models
# -keep class com.example.project.** { *; }
