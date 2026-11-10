///////////////////////////////////////////////////////////////////////////////
//  GRADLE CONFIGURATION
///////////////////////////////////////////////////////////////////////////////

buildscript {
  // Version of the Kotlin compiler used by the Kotlin support built into the Android plugin
  dependencies { classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21") }
}

plugins {
  id("com.diffplug.spotless") version "8.2.1"
  id("org.jetbrains.dokka") version "2.2.0" apply false
  id("com.android.library") version "9.4.1" apply false
}

spotless {
  kotlinGradle {
    target("**/*.kts")
    ktfmt()
  }
}
