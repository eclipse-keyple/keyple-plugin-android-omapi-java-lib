///////////////////////////////////////////////////////////////////////////////
//  GRADLE CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
plugins {
    `java`
    id("com.diffplug.spotless") version "5.10.2"
    id("org.sonarqube") version "3.1"
    jacoco
}
buildscript {
    val kotlinVersion by extra("1.4.20")
    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
        classpath ("com.diffplug.spotless:spotless-plugin-gradle:3.27.1")
        classpath ("org.jetbrains.dokka:dokka-gradle-plugin:0.10.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.eclipse.keyple:keyple-gradle:0.2.+")
    }
}
apply(plugin = "org.eclipse.keyple")

///////////////////////////////////////////////////////////////////////////////
//  APP CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
allprojects{
    group = "org.eclipse.keyple"
    apply (plugin = "org.jetbrains.dokka")
    apply (plugin = "com.diffplug.spotless")
    project.version = "1.0.1"

    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        google()
        jcenter()
    }
}

dependencies {
    implementation("org.slf4j:slf4j-api:1.7.30")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.assertj:assertj-core:3.15.0")
}

val javaSourceLevel: String by project
val javaTargetLevel: String by project
java {
    sourceCompatibility = JavaVersion.toVersion(javaSourceLevel)
    targetCompatibility = JavaVersion.toVersion(javaTargetLevel)
    println("Compiling Java $sourceCompatibility to Java $targetCompatibility.")
    withJavadocJar()
    withSourcesJar()
}

///////////////////////////////////////////////////////////////////////////////
//  TASKS CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
tasks {
    spotless {
        kotlin{
            target("**/*.kt")
            ktlint()
            licenseHeaderFile("gradle/license_header.txt")
        }
        java {
            target("src/**/*.java")
            licenseHeaderFile("gradle/license_header.txt")
            importOrder("java", "javax", "org", "com", "")
            removeUnusedImports()
            googleJavaFormat()
        }
    }
    test {
        testLogging {
            events("passed", "skipped", "failed")
        }
        finalizedBy("jacocoTestReport")
    }
    jacocoTestReport {
        dependsOn("test")
        reports {
            xml.isEnabled = true
            csv.isEnabled = false
            html.isEnabled = true
        }
    }
    sonarqube {
        properties {
            property("sonar.projectKey", "eclipse_" + project.name)
            property("sonar.organization", "eclipse")
            property("sonar.host.url", "https://sonarcloud.io")
            property("sonar.login", System.getenv("SONAR_LOGIN"))
            System.getenv("BRANCH_NAME")?.let {
                if (!"main".equals(it)) {
                    property("sonar.branch.name", it)
                }
            }
        }
    }
}
