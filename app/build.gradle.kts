plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.oneblue3.unicasetest"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.oneblue3.unicasetest"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 添加测试参数
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
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
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        
        // UI测试配置
        animationsDisabled = true
        
        // 暂时禁用orchestrator以解决执行问题
        // execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // OkHttp for network operations
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // 核心测试依赖
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    
    // Mock框架
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito:mockito-android:5.5.0")
    
    // Android单元测试
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    
    // UI测试框架
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    
    // UI测试录制工具
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    
    // 架构组件测试
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    
    // 协程测试
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // 断言库
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("com.google.truth:truth:1.1.5")
    
    // 网络测试
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    
    // RecyclerView测试
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
}