plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Agregar dependencia Grade para Google Services
    // id("com.google.gms.google-services")
    // Add the App Distribution Gradle plugin
    // id("com.google.firebase.appdistribution")
    // id ("com.google.firebase.crashlytics")
    id ("kotlin-parcelize")
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("${projectDir}/rayo_cr.jks")
            storePassword = "RayoCR_2.026"
            keyAlias = "rayo_cr"
            keyPassword = "RayoCR_2.026"
        }
        //create("release") {
        //    storeFile = file("${projectDir}/rayo-app-keystore.jks")
        //    storePassword = "1|WsE@~g37s<4~2j7S"
        //    keyAlias = "rayo-app-keystore"
        //    keyPassword = "1|WsE@~g37s<4~2j7S"
        //}
    }
    namespace = "com.rayo.rayoxml"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rayo.cr"
        minSdk = 24
        targetSdk = 35
        versionCode = 11010
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "version"
    productFlavors {
        create("qa") {
            dimension = "version"
            buildConfigField("String", "API_URL", "\"https://rayo.com.co/API/\"")
            buildConfigField("String", "CR_BASE_URL_AUTH", "\"https://rayocr.my.salesforce.com/services/\"")
            buildConfigField("String", "CR_BASE_URL", "\"https://rayocr.my.salesforce.com/services/apexrest/\"")
            buildConfigField("String", "CR_USERNAME_TOKEN", "\"admin@instantloanscr.com\"")
            buildConfigField("String", "CR_PASSWORD_TOKEN", "\"CompanyRayo2025*#WH3jCcRJAGcfWwWBMyXFIZSuW\"")
            buildConfigField("String", "CR_CLIENT_ID_TOKEN", "\"3MVG9xOCXq4ID1uFE9DjJL8SWOYDt26ODxlaVr3qYsPEiHKoYvlL3_5DF_6hQvBbSfT5rs3LbHg_gTQU0fkbg\"")
            buildConfigField("String", "CR_CLIENT_SECRET_TOKEN", "\"1576732546EDADC27994DA302B0B78A0E6A0CEEDC60FEA5079A2D750C4CB0ACD\"")
            buildConfigField("String", "CR_PAYMENT_URL", "\"https://www.recaudoenlinea.co.cr/VirtualPay?p=sWz+6tl7RGsQgQU1a74ruDxhitFxQUtwsK+g5kD+a44=\"")
        }
        create("dev") {
            dimension = "version"
            buildConfigField("String", "API_URL", "\"https://co.rayocredit.mx/API/\"")
            buildConfigField("String", "CR_BASE_URL_AUTH", "\"https://rayocr--partialqa.sandbox.my.salesforce.com/services/\"")
            buildConfigField("String", "CR_BASE_URL", "\"https://rayocr--partialqa.sandbox.my.salesforce.com/services/apexrest/\"")
            buildConfigField("String", "CR_USERNAME_TOKEN", "\"admin@instantloanscr.com.partialqa\"")
            buildConfigField("String", "CR_PASSWORD_TOKEN", "\"Incompany2022c4NoY8Z7xVFgPBeMg2MuPgJp\"")
            buildConfigField("String", "CR_CLIENT_ID_TOKEN", "\"3MVG9zZht._ZaMul1Lx9yQsWBSlG2FGD27L7cF3zzKWcZoQyFYiqbrjhdku.rliUv3.7hGAsz5YeNMjdCP5FT\"")
            buildConfigField("String", "CR_CLIENT_SECRET_TOKEN", "\"D287925822BCEFC7B7F4F1D076C74BC2FC6EA7B7EFEA8580CEB865F0BD146990\"")
            buildConfigField("String", "CR_PAYMENT_URL", "\"https://www.recaudoenlinea.co.cr/VirtualPay?p=sWz+6tl7RGsQgQU1a74ruDxhitFxQUtwsK+g5kD+a44=\"")
        }

    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            // firebaseAppDistribution {
            //     serviceCredentialsFile = "app/rayo-dev-66180-158c24e11c28.json"
            //     releaseNotesFile = "app/release-notes.txt"
            //     groups = "rayo-dev-team"
            // }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_15
        targetCompatibility = JavaVersion.VERSION_15
    }
    kotlinOptions {
        jvmTarget = "15"
        freeCompilerArgs += "-Xparcelize"
    }
    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    // NavComponent
    val navVersion = "2.8.4"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("com.tbuonomo:dotsindicator:5.1.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Import the Firebase BoM
    // implementation(platform("com.google.firebase:firebase-bom:33.8.0"))

    // TODO: Add the dependencies for Firebase products you want to use
    // See https://firebase.google.com/docs/android/setup#available-libraries
    // For example, add the dependencies for Firebase Authentication and Cloud Firestore
    // implementation("com.google.firebase:firebase-config")
    // implementation(libs.firebase.crashlytics.ktx)

    implementation("com.google.code.gson:gson:2.10.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Logger
    //implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    // QR
    implementation("com.google.zxing:core:3.5.1")  // ZXing Core Library
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")  // For BarcodeEncoder

    implementation("androidx.profileinstaller:profileinstaller:1.3.1")

}