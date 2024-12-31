plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id ("kotlin-parcelize")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.droidbox.clta"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.droidbox.clta"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.storage.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.glide)
    ksp(libs.compiler)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.swiperefreshlayout)
    implementation (libs.jsoup)
    implementation(libs.androidx.cardview)
    implementation (libs.lottie)
    implementation (libs.recyclerview)
    implementation (libs.shapeimageview)
    implementation (libs.viewpager2)
    implementation(project(":cardstackview"))
    implementation(libs.google.firebase.auth)
    implementation(libs.google.firebase.database)
    implementation (libs.google.firebase.analytics)
    implementation(platform(libs.firebase.bom))
    implementation (libs.play.services.auth)
    implementation (libs.firebase.firestore.ktx)


}

apply(plugin = "com.google.gms.google-services")