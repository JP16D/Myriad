if (JavaVersion.current() < JavaVersion.VERSION_17) {
    throw GradleException("JDK 17 is a required minimum version. Yours: ${System.getProperty("java.version")}")
}

rootProject.name = "Myriad"

// Acari   - runtime tool engine, also shipped as an installable mod
// Papilio - Mindustry UI extension library on top of Arc scene
include(":myriad-acari", ":myriad-papilio")
