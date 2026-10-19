plugins {
    id("com.android.application")
}
android {
    namespace = "pe.tesla.familias"
    compileSdk = 36
    compileSdkExtension = 19
    buildToolsVersion = "36.0.0"
    defaultConfig {
        applicationId = "pe.tesla.familias"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
