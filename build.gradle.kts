// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
}

ext {
    set("vCode", 1)
    set("vName", "1.0.0")
    set("targetVersion", 35)
    set("minVersion", 21)
    set("kotlinJvmVersion", "21")
    set("jvmVersion", JavaVersion.VERSION_21)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
