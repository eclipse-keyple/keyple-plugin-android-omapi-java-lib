///////////////////////////////////////////////////////////////////////////////
//  GRADLE CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

///////////////////////////////////////////////////////////////////////////////
//  APP CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
val kotlinVersion: String by project
val archivesBaseName: String by project
android {
    compileSdkVersion(29)
    buildToolsVersion("30.0.2")

    defaultConfig {
        minSdkVersion(19)
        targetSdkVersion(29)
        versionCode(1)
        versionName("1.0")

        testInstrumentationRunner("android.support.test.runner.AndroidJUnitRunner")
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            minifyEnabled(false)
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

    lintOptions {
        isAbortOnError = false
    }

    // generate output aar with a qualified name : with version number
    libraryVariants.all {
        outputs.forEach { output ->
            if (output is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                output.outputFileName = "${archivesBaseName}-${project.version}.${output.outputFile.extension}"
            }
        }
    }

    //create a task to generate javadoc for each variants
    libraryVariants.forEach { variant ->
        task("generate${variant.name.capitalize()}Javadoc", Javadoc::class) {
            description = "Generates Javadoc for variant ${variant.name.capitalize()}"
            //println "Create Javadoc Task for variant ${variant.name.capitalize()}"

            source = variant.javaCompile.source
            (options as StandardJavadocDocletOptions).links("http://docs.oracle.com/javase/6/docs/api/")
            (options as StandardJavadocDocletOptions).links("http://d.android.com/reference/")

            //println 'classpath : ' + classpath.getFiles()
            //println 'options links : ' + options.links
            //println 'source : ' + source.getFiles()

            // First add all of your dependencies to the classpath, then add the android jars
            doFirst {
                //doFirst is needed else we get the error "Cannot create variant 'android-lint' after configuration" with gradle 4.4+
                classpath = files(variant.javaCompile.classpath.files, project.android.bootClasspath)
            }
            classpath += files(android.bootClasspath)

            // We're excluding these generated files
            exclude("**/BuildConfig.java")
            exclude("**/R.java")
            isFailOnError = false
            setDestinationDir(file("${project.buildDir}/docs/javadoc"))
        }
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
apply(plugin = "org.eclipse.keyple")

dependencies {

    compileOnly(files("libs/org.simalliance.openmobileapi.jar"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    //keyple
    implementation("org.eclipse.keyple:keyple-common-java-api:2.0-SNAPSHOT")
    implementation("org.eclipse.keyple:keyple-plugin-java-api:2.0-SNAPSHOT")
    implementation("org.eclipse.keyple:keyple-util-java-lib:2.0.0-SNAPSHOT")

    //android
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.appcompat:appcompat:1.1.0")

    //logging
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("com.jakewharton.timber:timber:4.7.1") //Android
    implementation("com.arcao:slf4j-timber:3.1@aar") //SLF4J binding for Timber

    /** Test **/
    testImplementation("androidx.test:core-ktx:1.3.0")
    testImplementation("junit:junit:4.12")
    testImplementation("io.mockk:mockk:1.9")
    testImplementation("org.robolectric:robolectric:4.3.1")

    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.2")
}
