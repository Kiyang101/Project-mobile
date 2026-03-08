# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Firebase model classes
-keepclassmembers class com.example.project.ProductImage {
    public <init>();
    *** get*();
    *** set*();
}
-keepclassmembers class com.example.project.Product {
    public <init>();
    *** get*();
    *** set*();
}
-keepclassmembers class com.example.project.Cart {
    public <init>();
    *** get*();
    *** set*();
}
-keepclassmembers class com.example.project.CartItem {
    public <init>();
    *** get*();
    *** set*();
}
-keepclassmembers class com.example.project.History {
    public <init>();
    *** get*();
    *** set*();
}
-keepclassmembers class com.example.project.Favorite {
    public <init>();
    *** get*();
    *** set*();
}

# Alternatively, keep all classes in the package if they are models
# -keep class com.example.project.** { *; }
