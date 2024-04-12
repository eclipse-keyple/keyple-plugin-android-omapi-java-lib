///////////////////////////////////////////////////////////////////////////////
//  GRADLE CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("org.jetbrains.dokka")
    id("com.diffplug.spotless")
}

///////////////////////////////////////////////////////////////////////////////
//  APP CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
val kotlinVersion: String by project
val archivesBaseName: String by project
android {
    compileSdk = 33

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    val javaSourceLevel: String by project
    val javaTargetLevel: String by project
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(javaSourceLevel)
        targetCompatibility = JavaVersion.toVersion(javaTargetLevel)
    }

    testOptions {
        unitTests.apply {
            isReturnDefaultValues = true // mock Log Android object
            isIncludeAndroidResources = true
        }
    }

    lint {
        lint.abortOnError = false
    }

    kotlinOptions {
        jvmTarget = javaTargetLevel
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("debug").java.srcDirs("src/debug/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin")
    }
}

dependencies {

    compileOnly(files("libs/org.simalliance.openmobileapi.jar"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    //keyple
    implementation("org.eclipse.keyple:keyple-common-java-api:2.0.1")
    implementation("org.eclipse.keyple:keyple-plugin-java-api:2.3.1")
    implementation("org.eclipse.keyple:keyple-util-java-lib:2.4.0")

    //android
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    //logging
    implementation("org.slf4j:slf4j-api:1.7.32")
}

///////////////////////////////////////////////////////////////////////////////
//  TASKS CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
tasks {
    dokkaHtml.configure {
        dokkaSourceSets {
            named("main") {
                noAndroidSdkLink.set(false)
                includeNonPublic.set(false)
                includes.from(files("src/main/kdoc/overview.md"))
            }
        }
    }
}
apply(plugin = "org.eclipse.keyple") // To do last
