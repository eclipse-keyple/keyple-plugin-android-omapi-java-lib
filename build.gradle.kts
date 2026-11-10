///////////////////////////////////////////////////////////////////////////////
//  GRADLE CONFIGURATION
///////////////////////////////////////////////////////////////////////////////

plugins {
  id("com.diffplug.spotless") version "8.2.1"
  id("org.jetbrains.dokka") version "2.1.0" apply false
  id("com.android.library") version "8.13.2" apply false
  kotlin("android") version "2.3.0" apply false
}

spotless {
  kotlinGradle {
    target("**/*.kts")
    ktfmt()
  }
}
