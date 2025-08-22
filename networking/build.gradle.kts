
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.ianschoenrock.networking"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures{
        buildConfig = true
    }

    buildTypes {
        debug{
            buildConfigField("String", "FLICKR_BASE_URL", "\"${project.findProperty("flickr.base.url")}\"")
            buildConfigField("String", "FLICKR_API_KEY", "\"${project.findProperty("flickr.api.key")}\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "FLICKR_BASE_URL", "\"${project.findProperty("flickr.base.url")}\"")
            buildConfigField("String", "FLICKR_API_KEY", "\"${project.findProperty("flickr.api.key")}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.retrofit)
    implementation(libs.moshi)
    implementation(libs.moshi.converter)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}