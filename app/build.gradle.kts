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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    testOptions {
        unitTests {
            // 允许Robolectric访问Android资源
            isIncludeAndroidResources = true
        }
        
        // 禁用UI测试动画，提高稳定性
        animationsDisabled = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // 网络请求库
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // ===========================================
    // 工具层测试依赖 (30%) - 无依赖工具测试
    // ===========================================
    // JUnit 4 核心测试框架
    testImplementation("junit:junit:4.13.2")
    // Google Truth 流式断言库
    testImplementation("com.google.truth:truth:1.1.5")
    // Hamcrest 匹配器库
    testImplementation("org.hamcrest:hamcrest:2.2")
    
    // Android依赖工具测试
    testImplementation("org.robolectric:robolectric:4.15")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    
    // ===========================================
    // 逻辑层测试依赖 (50%) - 业务逻辑验证
    // ===========================================
    // Mockito 对象模拟框架
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito:mockito-android:5.5.0")
    
    // 网络测试模拟
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    
    // 架构组件测试支持
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    
    // ===========================================
    // UI层测试依赖 (20%) - 界面交互验证
    // ===========================================
    // Espresso 核心UI测试框架
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    
    // UI测试运行器和规则
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    
    // UI Automator 跨应用测试
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
}