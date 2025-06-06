plugins {
    //trick: To define a KSP version
    id("com.google.devtools.ksp") version "1.9.23-1.0.19" apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.cocoapods) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.android.gradle.plugin)
    }
}

// Use the newer task registration syntax
tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

// Custom wrapper task to handle permissions
tasks.register("setupWrapper") {
    doLast {
        val wrapperDir = file("gradle/wrapper")
        wrapperDir.mkdirs()
        
        // Create gradle-wrapper.properties
        file("gradle/wrapper/gradle-wrapper.properties").writeText("""
            distributionBase=GRADLE_USER_HOME
            distributionPath=wrapper/dists
            distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-bin.zip
            networkTimeout=10000
            validateDistributionUrl=true
            zipStoreBase=GRADLE_USER_HOME
            zipStorePath=wrapper/dists
        """.trimIndent())
        
        // Download the wrapper jar if it doesn't exist
        val wrapperJar = file("gradle/wrapper/gradle-wrapper.jar")
        if (!wrapperJar.exists()) {
            val wrapperJarUrl = "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar"
            wrapperJar.outputStream().use { output ->
                java.net.URL(wrapperJarUrl).openStream().use { input ->
                    input.copyTo(output)
                }
            }
        }
    }
} 