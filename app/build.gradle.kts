plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace="com.tesaduf.app"
    compileSdk=36

    defaultConfig {
        applicationId="com.tesaduf.app"
        minSdk=26
        targetSdk=36
        versionCode=1
        versionName="0.1.0"

        buildConfigField("String","SUPABASE_URL","\"https://wihagxhijxccxnaktnel.supabase.co\"")
        buildConfigField("String","SUPABASE_KEY","\"sb_publishable_3ivcpoyZlbB2kTBvLQpPBQ_9LgMw7u5\"")
    }

    buildFeatures { compose=true; buildConfig=true }
    compileOptions {
        sourceCompatibility=JavaVersion.VERSION_17
        targetCompatibility=JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget="17" }
}

dependencies {
    val composeBom=platform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.navigation:navigation-compose:2.9.4")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation(platform("io.github.jan-tennert.supabase:bom:3.7.0"))
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:functions-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.ktor:ktor-client-android:3.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
}
