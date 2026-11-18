plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.pokesample"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.pokesample"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
// 🔽 ここから下の5つを、閉じカッコ「}」のすぐ上に追加！ 🔽
    // 1. 音声再生（ExoPlayer）
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    // 2. コルーチン（非同期処理）
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    // 3. API通信（OkHttp）
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // 4. JSON変換（Gson）
    implementation("com.google.code.gson:gson:2.11.0")
    // 5. 画像表示（Coil）
    implementation("io.coil-kt:coil:2.7.0")
    // 🔼 追加ここまで 🔼
}